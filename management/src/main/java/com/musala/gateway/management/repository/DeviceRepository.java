package com.musala.gateway.management.repository;

import com.musala.gateway.management.model.Device;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DeviceRepository extends CrudRepository<Device,Long> {
    @Query("Select d from Device d where d.uid=?1")
    public Optional<Device> findByUID(long uid);
}
