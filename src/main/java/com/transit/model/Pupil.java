package com.transit.model;

public class Pupil {
    private final String pupilId;
    private String name;
    private String emergencyContact;
    private final String schoolId;
    private String routeId;
    private int version = 0; // used for optimistic locking (Phase B, iteration 3)

    public Pupil(String pupilId, String name, String emergencyContact, String schoolId, String routeId) {
        this.pupilId = pupilId;
        this.name = name;
        this.emergencyContact = emergencyContact;
        this.schoolId = schoolId;
        this.routeId = routeId;
    }

    public String getPupilId() { return pupilId; }
    public String getName() { return name; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getSchoolId() { return schoolId; }
    public String getRouteId() { return routeId; }
    public int getVersion() { return version; }

    public void setRouteId(String routeId) { this.routeId = routeId; }

    public void applyDetails(PupilDetails details) {
        if (details.name() != null) this.name = details.name();
        if (details.emergencyContact() != null) this.emergencyContact = details.emergencyContact();
        this.version++;
    }
}
