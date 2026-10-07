package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PowerLog {

    private String        deviceId;
    private String        deviceName;
    private String        event;
    private double        powerWatts;
    private LocalDateTime timestamp;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public PowerLog(String deviceId, String deviceName, String event, double powerWatts) {
        this.deviceId   = deviceId;
        this.deviceName = deviceName;
        this.event      = event;
        this.powerWatts = powerWatts;
        this.timestamp  = LocalDateTime.now();
    }

    public String        getDeviceId()   { return deviceId; }
    public String        getDeviceName() { return deviceName; }
    public String        getEvent()      { return event; }
    public double        getPowerWatts() { return powerWatts; }
    public LocalDateTime getTimestamp()  { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) turned %s — %.1f W",
                timestamp.format(FMT), deviceName, deviceId, event, powerWatts);
    }
}
