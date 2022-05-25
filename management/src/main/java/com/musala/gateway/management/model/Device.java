package com.musala.gateway.management.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "device")
@Validated
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "uid", nullable = false, unique = true)
    @NotNull(message = "UID most be specified")
    private long uid;//Assumed that the uid is a unique identifier for the device
    @Column(name = "vendor")
    private String vendor;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DeviceStatus deviceStatus = DeviceStatus.ONLINE;
    /**
     * Device is marked as the owning side of the one-to-many relationship
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gateway_id")
    @JsonIgnore
    private Gateway gateway;

    public Device() {
        createdAt = Calendar.getInstance().getTime();
    }

    public Device(long uid, String vendor, Date createdAt, DeviceStatus deviceStatus) {
        this.uid = uid;
        this.vendor = vendor;
        this.createdAt = createdAt;
        this.deviceStatus = deviceStatus;
    }

    public Device(long uid, String vendor, Date createdAt, DeviceStatus deviceStatus, Gateway gateway) {
        this.uid = uid;
        this.vendor = vendor;
        this.createdAt = createdAt;
        this.deviceStatus = deviceStatus;
        this.gateway = gateway;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public DeviceStatus getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }
}


