package com.project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.dto.DoctorDTO;
import com.project.entity.Admindoctor;
import com.project.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/doctor/add")
    public ResponseEntity<DoctorDTO> addDoctor(@RequestBody Map<String, Object> data) {

        DoctorDTO saved = adminService.saveDoctor(
                (String) data.get("name"),
                (String) data.get("email"),
                (String) data.get("password"),
                (String) data.get("specialization"),
                Integer.parseInt(data.get("consultingFees").toString()),
                (String) data.get("degree"),
                (String) data.get("experience"),
                (String) data.get("addressLine1"),
                (String) data.get("aboutDoctor"), // ✅ removed addressLine2
                (String) data.get("whatsappNumber"),
                Long.parseLong(data.get("hospitalId").toString())
        );

        return ResponseEntity.ok(saved);
    }
    @GetMapping("/doctor/all")
    public List<DoctorDTO> getAllDoctors() {
        return adminService.getAllDoctors();
    }

    @GetMapping("/doctor/{id}")
    public DoctorDTO getDoctor(@PathVariable Long id) {
        return adminService.getDoctorById(id);
    }

    @PutMapping("/doctor/update/{id}")
    public DoctorDTO updateDoctor(@PathVariable Long id, @RequestBody Admindoctor doctor) {
        return adminService.updateDoctor(id, doctor);
    }

    @DeleteMapping("/doctor/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        adminService.deleteDoctor(id);
        return "Doctor Deleted Successfully";
    }

    @DeleteMapping("/doctor/delete-by-email/{email}")
    public String deleteByEmail(@PathVariable String email) {
        adminService.deleteDoctorByEmail(email);
        return "Doctor Deleted By Email";
    }

    @GetMapping("/doctor/specialization/{spec}")
    public List<DoctorDTO> getBySpecialization(@PathVariable String spec) {
        return adminService.getDoctorsBySpecialization(spec);
    }

    @GetMapping("/doctor/name/{name}")
    public DoctorDTO getByName(@PathVariable String name) {
        return adminService.getDoctorByName(name);
    }

    @GetMapping("/doctor/filter")
    public List<DoctorDTO> getBySpecAndFees(@RequestParam String spec, @RequestParam Integer fees) {
        return adminService.getDoctorsBySpecAndFees(spec, fees);
    }

    @GetMapping("/doctor/spec-or-degree")
    public List<DoctorDTO> getBySpecOrDegree(@RequestParam String spec, @RequestParam String degree) {
        return adminService.getDoctorsBySpecOrDegree(spec, degree);
    }

    @GetMapping("/doctor/fees-range")
    public List<DoctorDTO> getByFeesRange(@RequestParam Integer min, @RequestParam Integer max) {
        return adminService.getDoctorsByFeesRange(min, max);
    }

    @GetMapping("/doctor/search")
    public List<DoctorDTO> searchDoctors(@RequestParam String keyword) {
        return adminService.searchDoctorsByName(keyword);
    }

    @GetMapping("/doctor/fees-greater-than/{fees}")
    public List<DoctorDTO> getByFeesGreaterThan(@PathVariable Integer fees) {
        return adminService.getDoctorsByFeesGreaterThan(fees);
    }

    @GetMapping("/doctor/sort/fees")
    public List<DoctorDTO> sortByFees() {
        return adminService.sortDoctorsByFees();
    }

    @GetMapping("/doctor/count/{spec}")
    public long countBySpecialization(@PathVariable String spec) {
        return adminService.countDoctorsBySpecialization(spec);
    }

    @GetMapping("/doctor/exists/{email}")
    public boolean checkEmailExists(@PathVariable String email) {
        return adminService.isEmailExists(email);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        return adminService.getAdminDashboard();
    }
}