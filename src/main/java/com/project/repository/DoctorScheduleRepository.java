package com.project.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.project.entity.DoctorSchedule;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
List<DoctorSchedule> findByDoctor_Id(Long doctorId);
List<DoctorSchedule> findByDoctor_IdAndScheduleDate(Long doctorId, LocalDate scheduleDate);
boolean existsByDoctor_IdAndScheduleDate(Long doctorId, LocalDate scheduleDate);
@Transactional
void deleteByDoctor_Id(Long doctorId);
@Query("""
	    SELECT DAYNAME(s.scheduleDate), COUNT(s)
	    FROM DoctorSchedule s
	    WHERE s.doctor.id = :doctorId
	    GROUP BY DAYNAME(s.scheduleDate)
	""")
	List<Object[]> getWeeklySchedules(@Param("doctorId") Long doctorId);
}