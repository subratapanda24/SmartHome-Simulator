package model;

public class Fan extends Appliance {

    private int speed;

    public Fan(String deviceId, String deviceName, String roomId, double powerConsumption) {
        super(deviceId, deviceName, roomId, powerConsumption, "FAN");
        this.speed = 1;
    }

    public int getSpeed() { return speed; }

    public void setSpeed(int speed) {
        if (speed < 1 || speed > 5)
            throw new IllegalArgumentException("Speed must be 1–5.");
        this.speed = speed;
    }

    @Override
    public String getDetails() { return "Speed: " + speed + "/5"; }
}
