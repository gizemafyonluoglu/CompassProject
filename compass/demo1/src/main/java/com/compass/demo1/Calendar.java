package com.compass.demo1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Calendar {
    private String calendarId;
    private List<Plan> plans;
    private List<Activity> activities;

    public Calendar() {
        this.plans = new ArrayList<>();
        this.activities = new ArrayList<>();
    }

    public Calendar(String calendarId) {
        this.calendarId = calendarId;
        this.plans = new ArrayList<>();
        this.activities = new ArrayList<>();
    }

    public void addPlan(Plan plan){
        if (plan != null && !plans.contains(plan)) {
            plans.add(plan);
        }
    }

    public void removePlan(Plan plan) {
        plans.remove(plan);
    }

    public void addActivity(Activity activity) {
        if (activity != null && !activities.contains(activity)) {
            activities.add(activity);
        }
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public boolean checkConflict(Activity activity) {
        if (activity == null){ 
            return false;
        }

        for (Plan plan : plans) {
            if (plan.overlapsWith(activity)) {
                return true;
            }
        }

        for (Activity existingActivity : activities) {
            if (existingActivity.getDate().equals(activity.getDate()) && existingActivity.getTime().equals(activity.getTime())) {
                return true;
            }
        }

        return false;
    }

    public boolean getAvailability(LocalDate date, LocalTime time) {
        for (Plan plan : plans) {
            if (plan.getDate().equals(date) && !time.isBefore(plan.getStartTime()) && time.isBefore(plan.getEndTime())) {
                return false;
            }
        }

        for (Activity activity : activities) {
            if (activity.getDate().equals(date) && activity.getTime().equals(time)) {
                return false;
            }
        }

        return true;
    }

    public String getCalendarId() { 
        return calendarId; 
    }

    public void setCalendarId(String calendarId) { 
        this.calendarId = calendarId; 
    }
 
    public List<Plan> getPlans() { 
        return new ArrayList<>(plans); 
    }

    public void setPlans(List<Plan> plans) { 
        this.plans = new ArrayList<>(plans); 
    }

    public List<Activity> getActivities() {
        removeExpiredActivities();
        return new ArrayList<>(activities);
    }

    public void setActivities(List<Activity> activities) { 
        this.activities = new ArrayList<>(activities); 
    }
    public void removeExpiredActivities() {
        List<Activity> activeActivities = new ArrayList<>();

        for (Activity activity : activities) {
            if (activity != null && !activity.isExpired() && !activity.isCancelled()) {
                activeActivities.add(activity);
            }
        }

        activities = activeActivities;
    }
}
