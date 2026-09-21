package com.automeds.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: Prescription
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class Prescription {

    // Primary Key identifier field
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_prescriptions")
    @SequenceGenerator(name = "seq_prescriptions", sequenceName = "SEQ_PRESCRIPTIONS", allocationSize = 1)
    private Long id;

    // Defines a Many-to-One relational database mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    // Maps this field to a database table column
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    // Maps this field to a database table column
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    // Maps this field to a database table column
    @Column(name = "upload_date", updatable = false)
    private LocalDateTime uploadDate;

    // Maps this field to a database table column
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "doctor_visit_date")
    private LocalDateTime doctorVisitDate;

    // Maps this field to a database table column
    @Column(nullable = false, length = 30)
    private String status; // "PENDING", "APPROVED", "REJECTED", "CLARIFICATION_REQUIRED", "EXPIRED"

    private static final String PENDING = "PENDING";

    public Prescription() {
        this.uploadDate = LocalDateTime.now();
        this.status = PENDING;
    }

    public Prescription(Long id, User patient, String fileName, String filePath, LocalDateTime uploadDate, LocalDateTime expiryDate, String status) {
        this.id = id;
        this.patient = patient;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadDate = uploadDate != null ? uploadDate : LocalDateTime.now();
        this.expiryDate = expiryDate;
        this.status = status != null ? status : PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (this.uploadDate == null) {
            this.uploadDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDoctorVisitDate() {
        return doctorVisitDate;
    }

    public void setDoctorVisitDate(LocalDateTime doctorVisitDate) {
        this.doctorVisitDate = doctorVisitDate;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", patientId=" + (patient != null ? patient.getId() : null) +
                ", fileName='" + fileName + '\'' +
                ", uploadDate=" + uploadDate +
                ", expiryDate=" + expiryDate +
                ", status='" + status + '\'' +
                '}';
    }
}
