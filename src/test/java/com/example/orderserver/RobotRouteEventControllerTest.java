package com.example.orderserver;

import com.example.orderserver.repository.RobotLocationRepository;
import com.example.orderserver.repository.RobotRouteEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RobotRouteEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RobotRouteEventRepository robotRouteEventRepository;

    @Autowired
    private RobotLocationRepository robotLocationRepository;

    @BeforeEach
    void setUp() {
        robotRouteEventRepository.deleteAll();
        robotLocationRepository.deleteAll();
    }

    @Test
    void robotShouldPostWaypointRouteEvent() throws Exception {
        mockMvc.perform(post("/api/robot/route-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "commandId": "route-20260519-001",
                                  "status": "WAYPOINT_REACHED",
                                  "currentWaypointSequence": 2,
                                  "latitude": 37.374806,
                                  "longitude": 126.633435,
                                  "message": "Reached waypoint 2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").exists())
                .andExpect(jsonPath("$.commandId").value("route-20260519-001"))
                .andExpect(jsonPath("$.status").value("WAYPOINT_REACHED"))
                .andExpect(jsonPath("$.currentWaypointSequence").value(2))
                .andExpect(jsonPath("$.latitude").value(37.374806))
                .andExpect(jsonPath("$.longitude").value(126.633435))
                .andExpect(jsonPath("$.receivedAt").exists());

        assertEquals(1, robotRouteEventRepository.count());

        mockMvc.perform(get("/api/robot/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(37.374806))
                .andExpect(jsonPath("$.longitude").value(126.633435));
    }

    @Test
    void clientShouldFetchRouteEventsByCommandIdInReceivedOrder() throws Exception {
        postRouteEvent("route-20260519-001", "MOVING", 1, 37.374528, 126.633170);
        postRouteEvent("route-20260519-001", "WAYPOINT_REACHED", 2, 37.374806, 126.633435);
        postRouteEvent("route-other", "MOVING", 1, 37.375226, 126.633868);

        mockMvc.perform(get("/api/robot/route-events")
                        .param("commandId", "route-20260519-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("MOVING"))
                .andExpect(jsonPath("$[0].currentWaypointSequence").value(1))
                .andExpect(jsonPath("$[1].status").value("WAYPOINT_REACHED"))
                .andExpect(jsonPath("$[1].currentWaypointSequence").value(2));
    }

    @Test
    void invalidWaypointSequenceShouldFailValidation() throws Exception {
        mockMvc.perform(post("/api/robot/route-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "commandId": "route-20260519-001",
                                  "status": "MOVING",
                                  "currentWaypointSequence": 0,
                                  "latitude": 37.374528,
                                  "longitude": 126.633170
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.currentWaypointSequence").value("Current waypoint sequence must be positive."));
    }

    private void postRouteEvent(
            String commandId,
            String status,
            int currentWaypointSequence,
            double latitude,
            double longitude
    ) throws Exception {
        mockMvc.perform(post("/api/robot/route-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "commandId": "%s",
                                  "status": "%s",
                                  "currentWaypointSequence": %d,
                                  "latitude": %s,
                                  "longitude": %s
                                }
                                """.formatted(commandId, status, currentWaypointSequence, latitude, longitude)))
                .andExpect(status().isOk());
    }
}
