package com.musala.gateway.management.model;

public enum DeviceStatus {

    ONLINE("Online"), OFFLINE("Offline");

    private final String value;

    DeviceStatus(String value) {
        this.value = value;
    }

    @SuppressWarnings("unused")
    public String getValue() {
        return value;
    }

    @SuppressWarnings("unused")
    public DeviceStatus getStatus(String value){
        if(value.equalsIgnoreCase("online")){
            return DeviceStatus.ONLINE;
        }else if(value.equalsIgnoreCase("offline")){
            return DeviceStatus.OFFLINE;
        }
        throw new IllegalArgumentException("Status not defined");
    }
}
