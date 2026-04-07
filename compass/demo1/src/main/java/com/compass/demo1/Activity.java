package com.compass.demo1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Activity {
    private String activityId;
    private String activityName;
    private String description;
    private String category;
    private String place;
    private LocalDate date;
    private LocalTime time;
    private int quota;
    private String visibility;
    private String activityType;
    private boolean isCancelled;
    private List<User> joinedUsers;
    private String profilePhotoBase64;


    public Activity(String activityId, String activityName, String description, String category, String place, 
                    LocalDate date, LocalTime time, int quota, String visibility, String activityType) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.description = description;
        this.category = category;
        this.place = place;
        this.date = date;
        this.time = time;
        this.quota = quota;
        this.visibility = visibility;
        this.activityType = activityType;
        this.isCancelled = false;
        this.joinedUsers = new ArrayList<>();
    }

     public void addParticipant(User user) {
        if (user != null && !isFull() && !joinedUsers.contains(user)) {
            joinedUsers.add(user);
        }
    }

    public void removeParticipant(User user) {
        joinedUsers.remove(user);
    }

    public boolean isFull() {
        return joinedUsers.size() >= quota;
    }

    public void cancelActivity() {
        this.isCancelled = true;
    }

    public String getActivityId() { 
        return activityId; 
    }

    public void setActivityId(String activityId) { 
        this.activityId = activityId; 
    }
 
    public String getActivityName() { 
        return activityName; 
    }

    public void setActivityName(String activityName) { 
        this.activityName = activityName; 
    }
 
    public String getDescription() { 
        return description; 
    }

    public void setDescription(String description) { 
        this.description = description; 
    }
 
    public String getCategory() { 
        return category; 
    }

    public void setCategory(String category) { 
        this.category = category; 
    }
 
    public String getPlace() { 
        return place; 
    }

    public String getProfilePhotoBase64() {
        return profilePhotoBase64;
    }

    public void setPlace(String place) {
        this.place = place; 
    }
 
    public LocalDate getDate() { 
        return date; 
    }

    public void setDate(LocalDate date) { 
        this.date = date; 
    }
 
    public LocalTime getTime() { 
        return time; 
    }

    public void setTime(LocalTime time) { 
        this.time = time; 
    }
 
    public int getQuota() { 
        return quota; 
    }

    public void setQuota(int quota) { 
        if(quota<0){
            this.quota = 1;
        }
        this.quota = quota; 
    }
 
    public String getVisibility() { 
        return visibility; 
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility; 
        }
 
    public String getActivityType() { 
        return activityType; 
    }

    public void setActivityType(String activityType) { 
        this.activityType = activityType; 
    }
 
    public boolean isCancelled() { 
        return isCancelled; 
    }
 
    public List<User> getJoinedUsers() { 
        return new ArrayList<>(joinedUsers); 
    }
    public boolean isExpired() {
        if (date == null || time == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isBefore(today)) {
            return true;
        }

        if (date.equals(today) && time.isBefore(now)) {
            return true;
        }

        return false;
    }

    public void setProfilePhotoBase64(String profilePhotoBase64) {
        this.profilePhotoBase64 = profilePhotoBase64;
    }
}
