package com.vectoros.fleet.service;

import com.vectoros.fleet.dto.RobotRequest;
import com.vectoros.fleet.dto.RobotResponse;
import com.vectoros.fleet.dto.RobotUpdateRequest;
import com.vectoros.fleet.entity.Robot;
import com.vectoros.fleet.entity.RobotStatus;
import com.vectoros.fleet.exception.DuplicateRobotException;
import com.vectoros.fleet.exception.RobotNotFoundException;
import com.vectoros.fleet.mapper.RobotMapper;
import com.vectoros.fleet.repository.RobotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RobotServiceTest {

    @Mock
    RobotRepository robotRepository;

    RobotMapper robotMapper = new RobotMapper();

    RobotService robotService;

    @BeforeEach
    void setUp() {
        robotService = new RobotService(robotRepository, robotMapper);
    }

    @Test
    void registerRobot_createsRobot_whenNameIsUnique() throws Exception {
        RobotRequest request = new RobotRequest("Robot-01");
        when(robotRepository.existsByName("Robot-01")).thenReturn(false);

        Robot saved = Robot.createNew("Robot-01");
        setId(saved, 1L);
        when(robotRepository.save(any(Robot.class))).thenReturn(saved);

        RobotResponse response = robotService.registerRobot(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Robot-01", response.getName());
        assertEquals(RobotStatus.IDLE, response.getStatus());
        assertEquals(100, response.getBatteryLevel());
        verify(robotRepository).save(any(Robot.class));
    }

    @Test
    void registerRobot_throwsDuplicateRobotException_whenNameAlreadyExists() {
        RobotRequest request = new RobotRequest("Robot-01");
        when(robotRepository.existsByName("Robot-01")).thenReturn(true);

        assertThrows(DuplicateRobotException.class, () -> robotService.registerRobot(request));
        verify(robotRepository, never()).save(any());
    }

    @Test
    void getRobot_returnsRobot_whenIdExists() throws Exception {
        Robot robot = Robot.createNew("Robot-01");
        setId(robot, 1L);

        when(robotRepository.findById(1L)).thenReturn(Optional.of(robot));

        RobotResponse response = robotService.getRobot(1L);
        assertEquals(1L, response.getId());
        assertEquals("Robot-01", response.getName());
    }

    @Test
    void getRobot_throwsRobotNotFoundException_whenIdDoesNotExist() {
        when(robotRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RobotNotFoundException.class, () -> robotService.getRobot(1L));
    }

    @Test
    void updateRobot_updatesStatus_whenIdExists() throws Exception {
        Robot robot = Robot.createNew("Robot-01");
        setId(robot, 1L);
        when(robotRepository.findById(1L)).thenReturn(Optional.of(robot));
        when(robotRepository.save(any(Robot.class))).thenAnswer(inv -> inv.getArgument(0));

        RobotResponse response = robotService.updateRobot(1L, new RobotUpdateRequest(RobotStatus.WORKING));

        assertEquals(RobotStatus.WORKING, response.getStatus());

        ArgumentCaptor<Robot> captor = ArgumentCaptor.forClass(Robot.class);
        verify(robotRepository).save(captor.capture());
        assertEquals(RobotStatus.WORKING, captor.getValue().getStatus());
    }

    @Test
    void deleteRobot_deletesRobot_whenIdExists() throws Exception {
        Robot robot = Robot.createNew("Robot-01");
        setId(robot, 1L);

        when(robotRepository.findById(1L)).thenReturn(Optional.of(robot));

        robotService.deleteRobot(1L);

        verify(robotRepository).delete(robot);
    }

    private void setId(Robot robot, Long id) throws Exception {
        Field idField = Robot.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(robot, id);
    }
}

