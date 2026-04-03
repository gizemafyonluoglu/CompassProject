package com.compass.demo1;

import java.time.LocalDate;
import java.time.LocalTime;

public class OneTimePlan extends Plan{
    public OneTimePlan(String planId, String title, LocalDate date,
                       LocalTime startTime, LocalTime endTime) {
        super(planId, title, date, startTime, endTime, "One-Time Plan");
    }
}
