package com.musala.gateway.management.repository;

import com.musala.gateway.management.model.Gateway;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface GatewayRepository extends CrudRepository<Gateway, Long> {
    @Query("Select g from Gateway g where g.serialNumber=?1")
    public Optional<Gateway> findBySerialNumber(String serialNumber);
}
