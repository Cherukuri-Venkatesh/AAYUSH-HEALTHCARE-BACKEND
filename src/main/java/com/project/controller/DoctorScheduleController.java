package com.project.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.dto.DoctorScheduleDTO;
import com.project.service.DoctorScheduleService;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService scheduleService;

    @PostMapping("/add")
    public DoctorScheduleDTO addSchedule(
            @RequestParam Long doctorId,
            @RequestParam String scheduleDate,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam Integer slotDuration) {

        return scheduleService.addSchedule(
                doctorId,
                LocalDate.parse(scheduleDate),
                LocalTime.parse(start),
                LocalTime.parse(end),
                slotDuration
        );
    }

    @GetMapping("/{doctorId}")
    public List<DoctorScheduleDTO> getSchedule(@PathVariable Long doctorId) {
        return scheduleService.getDoctorSchedule(doctorId);
    }

    @GetMapping("/available-slots")
    public List<LocalTime> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam String date) {

        return scheduleService.getAvailableSlots(
                doctorId,
                LocalDate.parse(date)
        );
    }

    @PutMapping("/update/{scheduleId}")
    public DoctorScheduleDTO updateSchedule(
            @PathVariable Long scheduleId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam Integer slotDuration) {

        return scheduleService.updateSchedule(
                scheduleId,
                LocalTime.parse(startTime),
                LocalTime.parse(endTime),
                slotDuration
        );
    }

    // NEW
    @DeleteMapping("/delete/{scheduleId}")
    public String deleteSchedule(@PathVariable Long scheduleId) {
        return scheduleService.deleteSchedule(scheduleId);
    }
}