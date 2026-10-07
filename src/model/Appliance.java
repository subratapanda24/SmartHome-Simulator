package model;

import exception.InvalidDeviceException;

public abstract class Appliance implements Controllable {

    private String deviceId;
    private String deviceName;
    private String roomId;
    private boolean on;
    private double powerConsumption;
    private String type;

    public Appliance(String deviceId, String deviceName, String roomId,
                     double powerConsumption, String type) {
        if (deviceId == null || deviceId.trim().isEmpty())
            throw new InvalidDeviceException("Device ID cannot be empty.");
        if (powerConsumption <= 0)
            throw new InvalidDeviceException("Power consumption must be positive.");
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.roomId = roomId;
        this.powerConsumption = powerConsumption;
        this.type = type;
        this.on = false;
    }

    @Override
    public void turnOn()  { this.on = true; }

    @Override
    public void turnOff() { this.on = false; }

    @Override
    public boolean isOn() { return on; }

    @Override
    public String getStatus() { return on ? "ON" : "OFF"; }

    public abstract String getDetails();

    public String getDeviceId()        { return deviceId; }
    public String getDeviceName()      { return deviceName; }
    public void   setDeviceName(String n) { this.deviceName = n; }
    public String getRoomId()          { return roomId; }
    public void   setRoomId(String r)  { this.roomId = r; }
    public double getPowerConsumption(){ return powerConsumption; }
    public String getType()            { return type; }

    @Override
    public String toString() {
        return String.format("[%s] %s | Room: %s | %s | %.1f W | %s",
                type, deviceName, roomId, getDetails(), powerConsumption, getStatus());
    }
}
