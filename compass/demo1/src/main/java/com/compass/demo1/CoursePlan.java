package com.compass.demo1;

import java.time.LocalDate;
import java.time.LocalTime;

public class CoursePlan extends Plan{
    public CoursePlan(String planId, String title, LocalDate date,
                      LocalTime startTime, LocalTime endTime) {
        super(planId, title, date, startTime, endTime, "Course Plan");
    }
}
