package com.example.orderserver;

import com.example.orderserver.repository.RobotLocationRepository;
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
class RobotLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RobotLocationRepository robotLocationRepository;

    @BeforeEach
    void setUp() {
        robotLocationRepository.deleteAll();
    }

    @Test
    void robotShouldPostAndAppShouldFetchLatestLocation() throws Exception {
        mockMvc.perform(post("/api/robot/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "latitude": 37.375,
                                  "longitude": 126.632
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(37.375))
                .andExpect(jsonPath("$.longitude").value(126.632))
                .andExpect(jsonPath("$.updatedAt").exists());

        mockMvc.perform(get("/api/robot/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(37.375))
                .andExpect(jsonPath("$.longitude").value(126.632))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void latestPostShouldReplacePreviousLocationForSingleRobot() throws Exception {
        postLocation(37.375, 126.632);
        postLocation(37.5, 126.75);

        assertEquals(1, robotLocationRepository.count());

        mockMvc.perform(get("/api/robot/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(37.5))
                .andExpect(jsonPath("$.longitude").value(126.75));
    }

    @Test
    void invalidRobotLocationShouldFailValidation() throws Exception {
        mockMvc.perform(post("/api/robot/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "latitude": 91.0,
                                  "longitude": 126.632
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void latestLocationShouldReturnNotFoundBeforeFirstPost() throws Exception {
        mockMvc.perform(get("/api/robot/location"))
                .andExpect(status().isNotFound());
    }

    private void postLocation(double latitude, double longitude) throws Exception {
        mockMvc.perform(post("/api/robot/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "latitude": %s,
                                  "longitude": %s
                                }
                                """.formatted(latitude, longitude)))
                .andExpect(status().isOk());
    }
}
