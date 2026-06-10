package com.example.orderserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CampusMapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clientShouldFetchCampusMapNodesAndEdges() throws Exception {
        mockMvc.perform(get("/api/campus-map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes.length()").value(8))
                .andExpect(jsonPath("$.edges.length()").value(8))
                .andExpect(jsonPath("$.nodes[?(@.id == 'social_science_front')].name").value("사과대 앞"))
                .andExpect(jsonPath("$.edges[?(@.id == 'social_convention_intersection_social_science_front')].bidirectional").value(true));
    }

    @Test
    void clientShouldFindShortestCampusRoute() throws Exception {
        mockMvc.perform(get("/api/campus-map/routes")
                        .param("from", "info_a")
                        .param("to", "natural_science"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromNodeId").value("info_a"))
                .andExpect(jsonPath("$.toNodeId").value("natural_science"))
                .andExpect(jsonPath("$.nodeIds[0]").value("info_a"))
                .andExpect(jsonPath("$.nodeIds[1]").value("info_b"))
                .andExpect(jsonPath("$.nodeIds[2]").value("library_front"))
                .andExpect(jsonPath("$.nodeIds[3]").value("natural_science"))
                .andExpect(jsonPath("$.legs.length()").value(3))
                .andExpect(jsonPath("$.waypoints.length()").value(4))
                .andExpect(jsonPath("$.totalDistanceM").value(142.5));
    }

    @Test
    void clientShouldUseAddedSocialScienceEdge() throws Exception {
        mockMvc.perform(get("/api/campus-map/routes")
                        .param("from", "social_convention_intersection")
                        .param("to", "social_science_front"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeIds[0]").value("social_convention_intersection"))
                .andExpect(jsonPath("$.nodeIds[1]").value("social_science_front"))
                .andExpect(jsonPath("$.legs[0].edgeId").value("social_convention_intersection_social_science_front"))
                .andExpect(jsonPath("$.totalDistanceM").value(81.7));
    }

    @Test
    void unknownCampusNodeShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/campus-map/routes")
                        .param("from", "info_a")
                        .param("to", "missing_node"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Campus map node not found"));
    }
}
