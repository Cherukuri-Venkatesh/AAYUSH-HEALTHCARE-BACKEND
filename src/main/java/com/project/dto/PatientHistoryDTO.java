package com.project.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientHistoryDTO {

    private Long patientId;
    private String patientName;
    private String email;
    private String phone;

    private List<AppointmentHistoryDTO> appointments;
}