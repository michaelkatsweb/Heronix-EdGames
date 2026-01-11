package com.heronix.edu.server.repository;

import com.heronix.edu.common.model.Device;
import com.heronix.edu.server.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Device entities.
 * Provides database access methods for device registration and management.
 */
@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {

    Optional<DeviceEntity> findByDeviceId(String deviceId);

    Optional<DeviceEntity> findByDeviceIdAndStudentId(String deviceId, String studentId);

    List<DeviceEntity> findByStatus(Device.DeviceStatus status);

    List<DeviceEntity> findByStudentId(String studentId);

    List<DeviceEntity> findByActiveTrueAndStatus(Device.DeviceStatus status);

    List<DeviceEntity> findByStatusAndActiveTrue(Device.DeviceStatus status);

    long countByStatus(Device.DeviceStatus status);

    long countByStatusAndActiveTrue(Device.DeviceStatus status);
}
