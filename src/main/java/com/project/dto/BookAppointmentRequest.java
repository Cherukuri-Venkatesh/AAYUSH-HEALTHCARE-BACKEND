package com.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookAppointmentRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotBlank(message = "Date is required")
    private String date;

    @NotBlank(message = "Time is required")
    private String time;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be greater than 0")
    private Integer age;

    @NotNull(message = "Weight is required")
    @Min(value = 1, message = "Weight must be greater than 0")
    private Double weight;

    @NotBlank(message = "Issue is required")
    private String issue;

    @NotBlank(message = "Payment type is required")
    private String paymentType;
}
