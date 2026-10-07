package app;

import exception.PowerLimitExceededException;
import model.AirConditioner;
import model.Fan;
import model.Light;
import model.Room;
import service.ApplianceManager;
import ui.SmartHomeGUI;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        ApplianceManager manager = new ApplianceManager();

        manager.addRoom(new Room("R001", "Living Room"));
        manager.addRoom(new Room("R002", "Bedroom"));
        manager.addRoom(new Room("R003", "Kitchen"));

        Light        l1 = new Light("D001", "Living Room Light", "R001", 60.0);
        Light        l2 = new Light("D002", "Bedroom Light",     "R002", 40.0);
        Fan          f1 = new Fan  ("D003", "Living Room Fan",   "R001", 75.0);
        Fan          f2 = new Fan  ("D004", "Bedroom Fan",       "R002", 55.0);
        AirConditioner a1 = new AirConditioner("D005", "Living Room AC", "R001", 1500.0);
        AirConditioner a2 = new AirConditioner("D006", "Kitchen AC",     "R003", 1200.0);

        manager.addAppliance(l1);
        manager.addAppliance(l2);
        manager.addAppliance(f1);
        manager.addAppliance(f2);
        manager.addAppliance(a1);
        manager.addAppliance(a2);

        try {
            manager.turnOnAppliance("D001");
            manager.turnOnAppliance("D003");
            manager.turnOnAppliance("D005");
        } catch (PowerLimitExceededException e) {
            System.out.println("Warning: " + e.getMessage());
        }

        System.out.println("Smart Home Simulator starting...");
        System.out.println("Devices loaded: " + manager.getAllAppliances().size());

        SwingUtilities.invokeLater(() -> new SmartHomeGUI(manager));
    }
}
