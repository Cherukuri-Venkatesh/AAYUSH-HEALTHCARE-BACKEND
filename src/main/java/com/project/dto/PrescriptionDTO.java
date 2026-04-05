package com.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDTO {

    private Long id;

    private Long appointmentId;

    private Long doctorId;

    private String doctorName;

    private Long patientId;

    private String filePath;

    private String uploadedAt;
}