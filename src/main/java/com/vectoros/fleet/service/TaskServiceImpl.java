package com.vectoros.fleet.service;

import com.vectoros.fleet.dto.TaskAssignmentRequest;
import com.vectoros.fleet.dto.TaskRequest;
import com.vectoros.fleet.dto.TaskResponse;
import com.vectoros.fleet.dto.TaskUpdateRequest;
import com.vectoros.fleet.entity.Robot;
import com.vectoros.fleet.entity.RobotStatus;
import com.vectoros.fleet.entity.Task;
import com.vectoros.fleet.entity.TaskStatus;
import com.vectoros.fleet.exception.InvalidTaskStateException;
import com.vectoros.fleet.exception.RobotNotFoundException;
import com.vectoros.fleet.exception.RobotUnavailableException;
import com.vectoros.fleet.exception.TaskAssignmentException;
import com.vectoros.fleet.exception.TaskNotFoundException;
import com.vectoros.fleet.mapper.TaskMapper;
import com.vectoros.fleet.mqtt.events.TaskAssignedEvent;
import com.vectoros.fleet.mqtt.events.TaskCancelledEvent;
import com.vectoros.fleet.mqtt.events.TaskStatusUpdatedEvent;
import com.vectoros.fleet.mqtt.publisher.RobotCommandPublisher;
import com.vectoros.fleet.repository.RobotRepository;
import com.vectoros.fleet.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final RobotRepository robotRepository;
    private final TaskMapper taskMapper;
    private final RobotCommandPublisher robotCommandPublisher;

    public TaskServiceImpl(TaskRepository taskRepository,
                           RobotRepository robotRepository,
                           TaskMapper taskMapper,
                           RobotCommandPublisher robotCommandPublisher) {
        this.taskRepository = taskRepository;
        this.robotRepository = robotRepository;
        this.taskMapper = taskMapper;
        this.robotCommandPublisher = robotCommandPublisher;
    }

    /**
     * Creates a new warehouse task and assigns it an auto-generated task number.
     * Validates that pickup and dropoff are not identical.
     *
     * @param request task creation payload
     * @return created task response
     */
    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        validateLocations(request.getPickupLocation(), request.getDropoffLocation());

        String taskNumber = generateTaskNumber();
        Task task = taskMapper.toEntity(taskNumber, request);
        Task saved = taskRepository.save(task);

        log.info("Task created: taskNumber={} priority={}", taskNumber, request.getPriority());
        return taskMapper.toResponse(saved);
    }

    /**
     * Returns a single task by id.
     *
     * @param taskId task identifier
     * @return task response
     */
    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        return taskMapper.toResponse(findTaskById(taskId));
    }

    /**
     * Returns all tasks.
     *
     * @return list of task responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    /**
     * Updates an existing task (currently status only).
     * Enforces state-transition rules for terminal and cancelled tasks.
     *
     * @param taskId  task identifier
     * @param request update payload
     * @return updated task response
     */
    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request) {
        return applyStatusUpdate(taskId, request);
    }

    /**
     * Updates the status of an existing task.
     *
     * @param taskId  task identifier
     * @param request update payload
     * @return updated task response
     */
    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, TaskUpdateRequest request) {
        return applyStatusUpdate(taskId, request);
    }

    /**
     * Assigns a robot to a task and publishes a TaskAssignedEvent.
     * The task must be in NEW or PENDING status to accept assignment.
     * Robots with ERROR status cannot be assigned.
     *
     * @param taskId  task identifier
     * @param request assignment payload containing the robot id
     * @return updated task response
     */
    @Override
    @Transactional
    public TaskResponse assignRobot(Long taskId, TaskAssignmentRequest request) {
        Task task = findTaskById(taskId);

        if (task.getStatus() != TaskStatus.NEW && task.getStatus() != TaskStatus.PENDING) {
            log.warn("Assignment failed: taskNumber={} status={}", task.getTaskNumber(), task.getStatus());
            throw new TaskAssignmentException(
                    "Task cannot be assigned in status: " + task.getStatus()
                            + ". Task must be NEW or PENDING.");
        }

        Robot robot = robotRepository.findById(request.getRobotId())
                .orElseThrow(() -> new RobotNotFoundException(request.getRobotId()));

        if (robot.getStatus() == RobotStatus.ERROR) {
            log.warn("Assignment failed: robotId={} status=ERROR", robot.getId());
            throw new RobotUnavailableException(robot.getId());
        }

        task.assignRobot(robot);
        Task saved = taskRepository.save(task);

        robotCommandPublisher.publishTaskAssigned(TaskAssignedEvent.builder()
                .taskId(saved.getId())
                .taskNumber(saved.getTaskNumber())
                .robotId(robot.getId())
                .pickupLocation(saved.getPickupLocation())
                .dropoffLocation(saved.getDropoffLocation())
                .priority(saved.getPriority())
                .timestamp(Instant.now())
                .build());

        log.info("Task assigned: taskNumber={} robotId={}", task.getTaskNumber(), robot.getId());
        return taskMapper.toResponse(saved);
    }

    /**
     * Deletes a task. Only COMPLETED, FAILED, or CANCELLED tasks may be deleted.
     *
     * @param taskId task identifier
     */
    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = findTaskById(taskId);

        if (task.getStatus() != TaskStatus.COMPLETED
                && task.getStatus() != TaskStatus.FAILED
                && task.getStatus() != TaskStatus.CANCELLED) {
            throw new InvalidTaskStateException(
                    "Only COMPLETED, FAILED, or CANCELLED tasks can be deleted. Current status: " + task.getStatus());
        }

        taskRepository.delete(task);
        log.info("Task deleted: taskNumber={}", task.getTaskNumber());
    }

    private TaskResponse applyStatusUpdate(Long taskId, TaskUpdateRequest request) {
        Task task = findTaskById(taskId);
        TaskStatus currentStatus = task.getStatus();
        TaskStatus newStatus = request.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        task.updateStatus(newStatus);
        Task saved = taskRepository.save(task);

        publishStatusEvents(saved, newStatus);

        if (newStatus == TaskStatus.COMPLETED) {
            log.info("Task completed: taskNumber={}", task.getTaskNumber());
        } else if (newStatus == TaskStatus.CANCELLED) {
            log.info("Task cancelled: taskNumber={}", task.getTaskNumber());
        } else if (newStatus == TaskStatus.FAILED) {
            log.info("Task failed: taskNumber={}", task.getTaskNumber());
        } else {
            log.info("Task status updated: taskNumber={} {} -> {}", task.getTaskNumber(), currentStatus, newStatus);
        }

        return taskMapper.toResponse(saved);
    }

    private void publishStatusEvents(Task task, TaskStatus newStatus) {
        Instant now = Instant.now();

        robotCommandPublisher.publishTaskStatusUpdated(TaskStatusUpdatedEvent.builder()
                .taskId(task.getId())
                .status(newStatus)
                .timestamp(now)
                .build());

        if (newStatus == TaskStatus.CANCELLED) {
            Long robotId = task.getAssignedRobot() != null ? task.getAssignedRobot().getId() : null;
            robotCommandPublisher.publishTaskCancelled(TaskCancelledEvent.builder()
                    .taskId(task.getId())
                    .taskNumber(task.getTaskNumber())
                    .robotId(robotId)
                    .timestamp(now)
                    .build());
        }
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private void validateLocations(String pickup, String dropoff) {
        if (pickup.equalsIgnoreCase(dropoff)) {
            throw new InvalidTaskStateException(
                    "Pickup and dropoff locations cannot be identical: " + pickup);
        }
    }

    private void validateStatusTransition(TaskStatus current, TaskStatus next) {
        if (current == TaskStatus.COMPLETED || current == TaskStatus.FAILED) {
            throw new InvalidTaskStateException(
                    "Cannot modify a task in terminal status: " + current);
        }
        if (current == TaskStatus.CANCELLED && next == TaskStatus.IN_PROGRESS) {
            throw new InvalidTaskStateException(
                    "A cancelled task cannot transition to IN_PROGRESS.");
        }
    }

    /**
     * Generates a unique task number in the format TASK-000001.
     * Pads the numeric suffix to six digits using the current task count.
     */
    private String generateTaskNumber() {
        long count = taskRepository.count() + 1;
        return String.format("TASK-%06d", count);
    }
}
