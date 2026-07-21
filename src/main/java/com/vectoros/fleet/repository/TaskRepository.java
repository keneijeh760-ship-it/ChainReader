package com.vectoros.fleet.repository;

import com.vectoros.fleet.entity.Priority;
import com.vectoros.fleet.entity.Robot;
import com.vectoros.fleet.entity.Task;
import com.vectoros.fleet.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(Priority priority);

    List<Task> findByAssignedRobot(Robot robot);

    Optional<Task> findByTaskNumber(String taskNumber);

    boolean existsByTaskNumber(String taskNumber);

    boolean existsByAssignedRobot_IdAndStatusIn(Long robotId, Collection<TaskStatus> statuses);
}
