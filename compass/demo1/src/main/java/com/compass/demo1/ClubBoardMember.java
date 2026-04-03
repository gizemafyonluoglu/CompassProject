package com.compass.demo1;

import java.util.List;

public class ClubBoardMember extends User{
    private String clubName;
    private boolean boardStatusApproved;
    
    public ClubBoardMember(String userId, String name, String surname, String email, String password, String clubName) {
        super(userId, name, surname, email, password);
        this.clubName = clubName;
        this.boardStatusApproved = false;
    }

    public void createClubActivity(ClubActivity activity) {
        if (activity != null && boardStatusApproved) {
            List<Activity> activities = getCreatedActivities();
            activities.add(activity);
            setCreatedActivities(activities);        
        }
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public boolean isBoardStatusApproved() {
        return boardStatusApproved;
    }

    public void setBoardStatusApproved(boolean boardStatusApproved) {
        this.boardStatusApproved = boardStatusApproved;
    }
}
