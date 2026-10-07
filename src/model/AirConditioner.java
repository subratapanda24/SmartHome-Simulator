package model;

public class AirConditioner extends Appliance {

    private int temperature;
    private String mode;

    public AirConditioner(String deviceId, String deviceName, String roomId, double powerConsumption) {
        super(deviceId, deviceName, roomId, powerConsumption, "AC");
        this.temperature = 24;
        this.mode = "COOL";
    }

    public int    getTemperature() { return temperature; }
    public String getMode()        { return mode; }

    public void setTemperature(int temperature) {
        if (temperature < 16 || temperature > 30)
            throw new IllegalArgumentException("Temperature must be 16–30 °C.");
        this.temperature = temperature;
    }

    public void setMode(String mode) {
        if (!mode.equals("COOL") && !mode.equals("HEAT") && !mode.equals("AUTO"))
            throw new IllegalArgumentException("Mode must be COOL, HEAT, or AUTO.");
        this.mode = mode;
    }

    @Override
    public String getDetails() { return "Temp: " + temperature + "°C | Mode: " + mode; }
}
