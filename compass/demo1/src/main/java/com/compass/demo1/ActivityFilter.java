package com.compass.demo1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityFilter {
    private LocalDate date;
    private LocalTime time;
    private String location;
    private String activityType;

    public ActivityFilter(LocalDate date, LocalTime time, String location, String activityType) {
        this.date = date;
        this.time = time;
        this.location = location;
        this.activityType = activityType;
    }
    public List<Activity> applyFilter(List<Activity> activities){
        List<Activity> filteredActivities = new ArrayList<>();
        for(Activity activity : activities){
            boolean matches = true;
            if(date != null && !date.equals(activity.getDate())){
                matches = false;
            }
            if(time != null && !time.equals(activity.getTime())){
                matches = false;
            }
            if(location != null && !location.equals(activity.getPlace())){
                matches = false;
            }
            if(activityType != null && !activityType.equals(activity.getActivityType())){
                matches = false;
            }
            if(matches){
                filteredActivities.add(activity);
            }
        }
        return filteredActivities;
    }
}