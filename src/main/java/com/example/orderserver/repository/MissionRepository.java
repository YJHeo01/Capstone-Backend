package com.example.orderserver.repository;

import com.example.orderserver.domain.Mission;
import com.example.orderserver.domain.MissionStatus;
import com.example.orderserver.domain.MissionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionRepository extends JpaRepository<Mission, UUID> {

    List<Mission> findAllByOrderIdOrderByCreatedAtAsc(UUID orderId);

    Optional<Mission> findFirstByOrderIdAndTypeOrderByCreatedAtDesc(UUID orderId, MissionType type);

    boolean existsByOrderIdAndType(UUID orderId, MissionType type);

    List<Mission> findAllByStatusInOrderByCreatedAtAsc(List<MissionStatus> statuses);
}
