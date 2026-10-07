package simulation;

import model.Appliance;
import service.ApplianceManager;
import java.util.Random;

public class DeviceSimulator implements Runnable {

    private final Appliance        appliance;
    private final int              cycles;
    private final ApplianceManager manager;
    private final Random           random = new Random();

    public DeviceSimulator(Appliance appliance, int cycles, ApplianceManager manager) {
        this.appliance = appliance;
        this.cycles    = cycles;
        this.manager   = manager;
    }

    @Override
    public void run() {
        System.out.println("[SIM] Starting simulation for: " + appliance.getDeviceName());
        for (int i = 1; i <= cycles; i++) {
            try {
                Thread.sleep(1500);
                if (random.nextBoolean()) {
                    appliance.turnOn();
                    manager.logEventPublic(appliance.getDeviceId(), "ON");
                    System.out.println("[SIM] Cycle " + i + " → " + appliance.getDeviceName() + " ON");
                } else {
                    appliance.turnOff();
                    manager.logEventPublic(appliance.getDeviceId(), "OFF");
                    System.out.println("[SIM] Cycle " + i + " → " + appliance.getDeviceName() + " OFF");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[SIM] Simulation interrupted for: " + appliance.getDeviceName());
                return;
            }
        }
        System.out.println("[SIM] Simulation complete for: " + appliance.getDeviceName());
    }
}
