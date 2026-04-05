package com.project.dto;

import lombok.Data;

@Data
public class RescheduleAppointmentRequest {
    private String date;
    private String time;
}
