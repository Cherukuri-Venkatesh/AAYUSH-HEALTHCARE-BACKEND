package com.project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LoginUserCleanupTriggerInitializer {

    private static final Logger logger = LoggerFactory.getLogger(LoginUserCleanupTriggerInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private boolean triggerCreationSkipped;

    public LoginUserCleanupTriggerInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureDeleteTriggers() {
        int removedDoctorOrphans = jdbcTemplate.update(
            "DELETE lu FROM login_users lu " +
                "LEFT JOIN doctors d ON LOWER(lu.email) = LOWER(d.email) " +
                "WHERE lu.role = 'DOCTOR' AND d.id IS NULL"
        );

        int removedPatientOrphans = jdbcTemplate.update(
            "DELETE lu FROM login_users lu " +
                "LEFT JOIN patients p ON LOWER(lu.email) = LOWER(p.email) " +
                "WHERE lu.role = 'PATIENT' AND p.id IS NULL"
        );

        if (removedDoctorOrphans > 0 || removedPatientOrphans > 0) {
            logger.info(
                "Removed orphan login_users rows on startup: doctors={}, patients={}",
                removedDoctorOrphans,
                removedPatientOrphans
            );
        }

        createTriggerIfMissing(
                "trg_doctors_after_delete_login_users_cleanup",
                "doctors",
                "DOCTOR"
        );

        createTriggerIfMissing(
                "trg_patients_after_delete_login_users_cleanup",
                "patients",
                "PATIENT"
        );
    }

    private void createTriggerIfMissing(String triggerName, String tableName, String role) {
        if (triggerCreationSkipped) {
            return;
        }

        Integer triggerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TRIGGERS " +
                        "WHERE TRIGGER_SCHEMA = DATABASE() AND TRIGGER_NAME = ?",
                Integer.class,
                triggerName
        );

        if (triggerCount != null && triggerCount > 0) {
            return;
        }

        String createTriggerSql = "CREATE TRIGGER " + triggerName +
                " AFTER DELETE ON " + tableName +
                " FOR EACH ROW DELETE FROM login_users " +
                "WHERE LOWER(email) = LOWER(OLD.email) AND role = '" + role + "'";

        try {
            jdbcTemplate.execute(createTriggerSql);
            logger.info("Created DB trigger {} for {} delete cleanup", triggerName, tableName);
        } catch (DataAccessException ex) {
            if (isTriggerPrivilegeError(ex)) {
                triggerCreationSkipped = true;
                logger.warn(
                    "Skipping DB trigger creation due to insufficient DB privileges. " +
                        "Startup cleanup still runs, but deletes in {} will not auto-clean login_users.",
                    tableName
                );
                return;
            }
            throw ex;
        }
    }

    private boolean isTriggerPrivilegeError(DataAccessException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof java.sql.SQLException sqlEx) {
                if (sqlEx.getErrorCode() == 1419) {
                    return true;
                }

                String message = sqlEx.getMessage();
                if (message != null && message.contains("SUPER privilege")) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
