package service;

import exception.InvalidDeviceException;
import exception.PowerLimitExceededException;
import model.Appliance;
import model.PowerLog;
import model.Room;
import java.util.*;

public class ApplianceManager {

    private ArrayList<Appliance>             applianceList  = new ArrayList<>();
    private LinkedList<PowerLog>             powerHistory   = new LinkedList<>();
    private HashMap<String, Appliance>       applianceMap   = new HashMap<>();
    private HashMap<String, Room>            roomMap        = new HashMap<>();
    private TreeMap<Double, List<String>>    sortedByPower  = new TreeMap<>();

    private static final double POWER_LIMIT = 5000.0;

    public void addRoom(Room room) {
        roomMap.put(room.getRoomId(), room);
    }

    public Room getRoom(String roomId) {
        Room r = roomMap.get(roomId);
        if (r == null) throw new InvalidDeviceException("Room not found: " + roomId);
        return r;
    }

    public Collection<Room> getAllRooms() { return roomMap.values(); }

    public void addAppliance(Appliance a) {
        if (applianceMap.containsKey(a.getDeviceId()))
            throw new InvalidDeviceException("Duplicate device ID: " + a.getDeviceId());
        if (!roomMap.containsKey(a.getRoomId()))
            throw new InvalidDeviceException("Room not found: " + a.getRoomId());

        applianceList.add(a);
        applianceMap.put(a.getDeviceId(), a);
        roomMap.get(a.getRoomId()).addAppliance(a.getDeviceId());
        sortedByPower.computeIfAbsent(a.getPowerConsumption(), k -> new ArrayList<>())
                     .add(a.getDeviceId());
    }

    public Appliance getAppliance(String deviceId) {
        Appliance a = applianceMap.get(deviceId);
        if (a == null) throw new InvalidDeviceException("Device not found: " + deviceId);
        return a;
    }

    public List<Appliance> getAllAppliances() { return Collections.unmodifiableList(applianceList); }

    public void updateApplianceName(String deviceId, String newName) {
        getAppliance(deviceId).setDeviceName(newName);
    }

    public void removeAppliance(String deviceId) {
        Appliance a = getAppliance(deviceId);
        applianceList.remove(a);
        applianceMap.remove(deviceId);
        roomMap.get(a.getRoomId()).removeAppliance(deviceId);
        List<String> ids = sortedByPower.get(a.getPowerConsumption());
        if (ids != null) { ids.remove(deviceId); if (ids.isEmpty()) sortedByPower.remove(a.getPowerConsumption()); }
    }

    public void turnOnAppliance(String deviceId) throws PowerLimitExceededException {
        Appliance a = getAppliance(deviceId);
        if (!a.isOn() && getCurrentPowerUsage() + a.getPowerConsumption() > POWER_LIMIT)
            throw new PowerLimitExceededException(
                "Cannot turn on " + a.getDeviceName() + ". Power limit (5000 W) would be exceeded.");
        a.turnOn();
        logEvent(deviceId, "ON");
    }

    public void turnOffAppliance(String deviceId) {
        getAppliance(deviceId).turnOff();
        logEvent(deviceId, "OFF");
    }

    public double getCurrentPowerUsage() {
        return applianceList.stream()
               .filter(Appliance::isOn)
               .mapToDouble(Appliance::getPowerConsumption)
               .sum();
    }

    public double getPowerLimit() { return POWER_LIMIT; }

    private void logEvent(String deviceId, String event) {
        Appliance a = applianceMap.get(deviceId);
        powerHistory.addLast(new PowerLog(deviceId, a.getDeviceName(), event, a.getPowerConsumption()));
        if (powerHistory.size() > 50) powerHistory.removeFirst();
    }

    public void logEventPublic(String deviceId, String event) { logEvent(deviceId, event); }

    public LinkedList<PowerLog> getPowerHistory() { return powerHistory; }

    public List<Appliance> searchByRoom(String roomId) {
        List<Appliance> result = new ArrayList<>();
        for (Appliance a : applianceList)
            if (a.getRoomId().equalsIgnoreCase(roomId)) result.add(a);
        return result;
    }

    public List<Appliance> searchByType(String type) {
        List<Appliance> result = new ArrayList<>();
        for (Appliance a : applianceList)
            if (a.getType().equalsIgnoreCase(type)) result.add(a);
        return result;
    }

    public List<Appliance> searchByName(String keyword) {
        List<Appliance> result = new ArrayList<>();
        for (Appliance a : applianceList)
            if (a.getDeviceName().toLowerCase().contains(keyword.toLowerCase())) result.add(a);
        return result;
    }

    public List<Appliance> searchByStatus(boolean isOn) {
        List<Appliance> result = new ArrayList<>();
        for (Appliance a : applianceList)
            if (a.isOn() == isOn) result.add(a);
        return result;
    }

    public List<Appliance> sortedByPowerDesc() {
        List<Appliance> result = new ArrayList<>();
        for (Double power : sortedByPower.descendingKeySet())
            for (String id : sortedByPower.get(power))
                if (applianceMap.containsKey(id)) result.add(applianceMap.get(id));
        return result;
    }

    public List<Appliance> sortedByName() {
        List<Appliance> copy = new ArrayList<>(applianceList);
        copy.sort(Comparator.comparing(Appliance::getDeviceName));
        return copy;
    }

    public List<Appliance> sortedByStatus() {
        List<Appliance> copy = new ArrayList<>(applianceList);
        copy.sort((a, b) -> Boolean.compare(b.isOn(), a.isOn()));
        return copy;
    }

    public String generateReport() {
        long onCount = applianceList.stream().filter(Appliance::isOn).count();
        StringBuilder sb = new StringBuilder();
        sb.append("========== SMART HOME USAGE REPORT ==========\n");
        sb.append("Total Devices   : ").append(applianceList.size()).append("\n");
        sb.append("Devices ON      : ").append(onCount).append("\n");
        sb.append("Devices OFF     : ").append(applianceList.size() - onCount).append("\n");
        sb.append(String.format("Current Usage   : %.1f W%n", getCurrentPowerUsage()));
        sb.append(String.format("Power Limit     : %.1f W%n", POWER_LIMIT));
        sb.append("\n--- Appliances by Power (High→Low) ---\n");
        for (Appliance a : sortedByPowerDesc())
            sb.append(String.format("  %-20s  %.1f W  [%s]\n",
                    a.getDeviceName(), a.getPowerConsumption(), a.getStatus()));
        sb.append("\n--- Last 10 Power Events ---\n");
        int start = Math.max(0, powerHistory.size() - 10);
        for (int i = start; i < powerHistory.size(); i++)
            sb.append("  ").append(powerHistory.get(i)).append("\n");
        sb.append("==============================================\n");
        return sb.toString();
    }
}
