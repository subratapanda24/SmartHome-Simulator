package model;

import java.util.ArrayList;

public class Room {

    private String roomId;
    private String roomName;
    private ArrayList<String> applianceIds;

    public Room(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.applianceIds = new ArrayList<>();
    }

    public void addAppliance(String deviceId)    { applianceIds.add(deviceId); }
    public void removeAppliance(String deviceId) { applianceIds.remove(deviceId); }

    public String          getRoomId()       { return roomId; }
    public String          getRoomName()     { return roomName; }
    public ArrayList<String> getApplianceIds() { return applianceIds; }

    @Override
    public String toString() {
        return roomId + " - " + roomName + " (" + applianceIds.size() + " appliances)";
    }
}
