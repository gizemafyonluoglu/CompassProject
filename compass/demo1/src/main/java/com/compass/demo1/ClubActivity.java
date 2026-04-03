package com.compass.demo1;

public class ClubActivity extends Activity {
 
    private String clubName;
    private boolean isOfficialClubEvent;
 
    public ClubActivity(String activityId, String activityName, String description, String category, String place, java.time.LocalDate date, 
                        java.time.LocalTime time, int quota, String visibility, String activityType, String clubName, boolean isOfficialClubEvent) {
        super(activityId, activityName, description, category, place, date, time, quota, visibility, activityType);
        this.clubName = clubName;
        this.isOfficialClubEvent = isOfficialClubEvent;
    }
 
    public String getClubName() { 
        return clubName; 
    }
    
    public void setClubName(String clubName) { 
        this.clubName = clubName; 
    }
 
    public boolean isOfficialClubEvent() { 
        return isOfficialClubEvent; 
    }

    public void setOfficialClubEvent(boolean officialClubEvent) { 
        isOfficialClubEvent = officialClubEvent; 
    }
}
 