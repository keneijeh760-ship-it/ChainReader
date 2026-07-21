package com.vectoros.fleet.service;

import com.vectoros.fleet.dto.TaskAssignmentRequest;
import com.vectoros.fleet.dto.TaskRequest;
import com.vectoros.fleet.dto.TaskResponse;
import com.vectoros.fleet.dto.TaskUpdateRequest;
import com.vectoros.fleet.entity.Priority;
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
import com.vectoros.fleet.repository.RobotRepository;
import com.vectoros.fleet.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    RobotRepository robotRepository;

    TaskMapper taskMapper = new TaskMapper();
    TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository, robotRepository, taskMapper);
    }

    @Test
    void createTask_succeeds_whenLocationsAreDifferent() throws Exception {
        TaskRequest request = new TaskRequest("A12", "C18", Priority.HIGH);
        when(taskRepository.count()).thenReturn(0L);
        Task saved = buildTask(1L, "TASK-000001", TaskStatus.NEW);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("TASK-000001", response.getTaskNumber());
        assertEquals(TaskStatus.NEW, response.getStatus());
    }

    @Test
    void createTask_throws_whenLocationsAreIdentical() {
        TaskRequest request = new TaskRequest("A12", "A12", Priority.HIGH);

        assertThrows(InvalidTaskStateException.class, () -> taskService.createTask(request));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getTask_returnsTask_whenIdExists() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.NEW);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTask(1L);
        assertEquals(1L, response.getId());
        assertEquals("TASK-000001", response.getTaskNumber());
    }

    @Test
    void getTask_throws_whenIdDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTask(99L));
    }

    @Test
    void updateTaskStatus_updatesStatus_whenTransitionIsValid() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.NEW);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.updateTaskStatus(1L, new TaskUpdateRequest(TaskStatus.PENDING));
        assertEquals(TaskStatus.PENDING, response.getStatus());
    }

    @Test
    void updateTaskStatus_throws_whenTaskIsCompleted() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(InvalidTaskStateException.class,
                () -> taskService.updateTaskStatus(1L, new TaskUpdateRequest(TaskStatus.IN_PROGRESS)));
    }

    @Test
    void updateTaskStatus_throws_whenCancelledTaskMovesToInProgress() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.CANCELLED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(InvalidTaskStateException.class,
                () -> taskService.updateTaskStatus(1L, new TaskUpdateRequest(TaskStatus.IN_PROGRESS)));
    }

    @Test
    void assignRobot_assigns_whenTaskIsNewAndRobotIsIdle() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.NEW);
        Robot robot = buildRobot(3L, "Robot-03", RobotStatus.IDLE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(robotRepository.findById(3L)).thenReturn(Optional.of(robot));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.assignRobot(1L, new TaskAssignmentRequest(3L));

        assertEquals(TaskStatus.ASSIGNED, response.getStatus());
        assertEquals(3L, response.getAssignedRobotId());
    }

    @Test
    void assignRobot_throws_whenRobotHasErrorStatus() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.NEW);
        Robot robot = buildRobot(3L, "Robot-03", RobotStatus.ERROR);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(robotRepository.findById(3L)).thenReturn(Optional.of(robot));

        assertThrows(RobotUnavailableException.class,
                () -> taskService.assignRobot(1L, new TaskAssignmentRequest(3L)));
    }

    @Test
    void assignRobot_throws_whenRobotDoesNotExist() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.NEW);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(robotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RobotNotFoundException.class,
                () -> taskService.assignRobot(1L, new TaskAssignmentRequest(99L)));
    }

    @Test
    void assignRobot_throws_whenTaskIsAlreadyCompleted() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(TaskAssignmentException.class,
                () -> taskService.assignRobot(1L, new TaskAssignmentRequest(3L)));
    }

    @Test
    void deleteTask_succeeds_whenTaskIsCompleted() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_throws_whenTaskIsActive() throws Exception {
        Task task = buildTask(1L, "TASK-000001", TaskStatus.IN_PROGRESS);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(InvalidTaskStateException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository, never()).delete(any());
    }

    // --- helpers ---

    private Task buildTask(Long id, String taskNumber, TaskStatus status) throws Exception {
        Task task = Task.createNew(taskNumber, "A12", "C18", Priority.HIGH);
        setField(task, "id", id);
        if (status != TaskStatus.NEW) {
            task.updateStatus(status);
        }
        return task;
    }

    private Robot buildRobot(Long id, String name, RobotStatus status) throws Exception {
        Robot robot = Robot.createNew(name);
        setField(robot, "id", id);
        if (status != RobotStatus.IDLE) {
            robot.updateStatus(status);
        }
        return robot;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
