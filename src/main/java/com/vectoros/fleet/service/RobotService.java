package com.vectoros.fleet.service;

import com.vectoros.fleet.dto.RobotRequest;
import com.vectoros.fleet.dto.RobotResponse;
import com.vectoros.fleet.dto.RobotUpdateRequest;
import com.vectoros.fleet.entity.Robot;
import com.vectoros.fleet.exception.DuplicateRobotException;
import com.vectoros.fleet.exception.InvalidTaskStateException;
import com.vectoros.fleet.exception.RobotNotFoundException;
import com.vectoros.fleet.mapper.RobotMapper;
import com.vectoros.fleet.repository.RobotRepository;
import com.vectoros.fleet.repository.TaskRepository;
import com.vectoros.fleet.entity.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class RobotService {

    private static final Logger log = LoggerFactory.getLogger(RobotService.class);

    private static final Set<TaskStatus> ACTIVE_TASK_STATUSES = Set.of(
            TaskStatus.NEW,
            TaskStatus.PENDING,
            TaskStatus.ASSIGNED,
            TaskStatus.IN_PROGRESS
    );

    private final RobotRepository robotRepository;
    private final TaskRepository taskRepository;
    private final RobotMapper robotMapper;

    public RobotService(RobotRepository robotRepository,
                        TaskRepository taskRepository,
                        RobotMapper robotMapper) {
        this.robotRepository = robotRepository;
        this.taskRepository = taskRepository;
        this.robotMapper = robotMapper;
    }

    /**
     * Registers a new robot in the fleet.
     *
     * @param request request payload
     * @return created robot response
     */
    @Transactional
    public RobotResponse registerRobot(RobotRequest request) {
        String name = request.getName();
        if (robotRepository.existsByName(name)) {
            throw new DuplicateRobotException(name);
        }

        Robot robot = robotMapper.toEntity(request);
        try {
            Robot saved = robotRepository.save(robot);
            log.info("Robot registered: name={}", name);
            return robotMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // Handles rare race conditions where uniqueness is enforced at DB level.
            throw new DuplicateRobotException(name);
        }
    }

    /**
     * Returns a robot by id.
     *
     * @param robotId robot identifier
     * @return robot response
     */
    @Transactional(readOnly = true)
    public RobotResponse getRobot(Long robotId) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new RobotNotFoundException(robotId));
        return robotMapper.toResponse(robot);
    }

    /**
     * Returns all robots.
     *
     * @return list of robots
     */
    @Transactional(readOnly = true)
    public List<RobotResponse> getAllRobots() {
        return robotRepository.findAll().stream()
                .map(robotMapper::toResponse)
                .toList();
    }

    /**
     * Updates robot information (currently status only).
     *
     * @param robotId robot identifier
     * @param request update payload
     * @return updated robot response
     */
    @Transactional
    public RobotResponse updateRobot(Long robotId, RobotUpdateRequest request) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new RobotNotFoundException(robotId));

        robotMapper.applyUpdate(request, robot);
        Robot saved = robotRepository.save(robot);
        log.info("Robot updated: id={} status={}", robotId, saved.getStatus());
        return robotMapper.toResponse(saved);
    }

    /**
     * Deletes a robot by id.
     * Deletion is blocked when the robot has active tasks assigned.
     *
     * @param robotId robot identifier
     */
    @Transactional
    public void deleteRobot(Long robotId) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new RobotNotFoundException(robotId));

        if (taskRepository.existsByAssignedRobot_IdAndStatusIn(robotId, ACTIVE_TASK_STATUSES)) {
            throw new InvalidTaskStateException(
                    "Cannot delete robot with active tasks assigned: id=" + robotId);
        }

        robotRepository.delete(robot);
        log.info("Robot deleted: id={}", robotId);
    }
}

