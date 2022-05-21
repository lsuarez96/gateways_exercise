package com.musala.gateway.management.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.musala.gateway.management.annotation.IPConstraint;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Entity
@Table(name = "gateway")
public class Gateway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "serial_number", unique = true)
    @NotBlank(message = "Serial number is required")
    private String serialNumber;
    private String name;
    @Column(name = "ip_address", nullable = false)
    @NotBlank(message = "IP Address is required")
    @IPConstraint
    private String ipAddress;
    /**
     * This mapping is merely to follow the logic that a gateway has many devices, although the device is the owner
     * of the one-to-many relationship. Therefore, this attribute is not really mapped in the database, only the
     * reference to Gateway in Device.
     */
    @OneToMany(mappedBy = "gateway")
    private List<Device> devices;

    public Gateway() {
        devices = new ArrayList<>();
    }

    public Gateway(String serialNumber, String name, String ipAddress) {
        this();
        this.serialNumber = serialNumber;
        this.name = name;
        this.ipAddress = ipAddress;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<Device> getDevices() {
        return devices;
    }
}
