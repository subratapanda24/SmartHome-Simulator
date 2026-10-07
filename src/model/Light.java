package model;

public class Light extends Appliance {

    private int brightness;

    public Light(String deviceId, String deviceName, String roomId, double powerConsumption) {
        super(deviceId, deviceName, roomId, powerConsumption, "LIGHT");
        this.brightness = 50;
    }
        
    public int getBrightness() { return brightness; }

    public void setBrightness(int brightness) {
        if (brightness < 1 || brightness > 100)
            throw new IllegalArgumentException("Brightness must be 1–100.");
        this.brightness = brightness;
    }

    @Override
    public String getDetails() { return "Brightness: " + brightness + "%"; }
}
