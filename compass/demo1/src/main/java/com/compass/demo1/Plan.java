package com.compass.demo1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public abstract class Plan {
    
    private String planId;
    private String ownerUserId;
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String planType;

    public Plan(String planId, String title, LocalDate date,
                LocalTime startTime, LocalTime endTime, String planType) {
        this.planId = planId;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.planType = planType;
    }

    public boolean overlapsWith(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (!Objects.equals(this.date, activity.getDate())) {
            return false;
        }
        LocalTime activityTime = activity.getTime();
        return !activityTime.isBefore(startTime) && !activityTime.isAfter(endTime);
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }
}