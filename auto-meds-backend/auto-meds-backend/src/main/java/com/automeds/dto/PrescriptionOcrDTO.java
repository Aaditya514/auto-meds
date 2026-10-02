package com.automeds.dto;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionOcrDTO {
    private String fileName;
    private String rawExtractedText;
    private String doctorName;
    private String doctorRegNumber;
    private String clinicOrHospital;
    private String prescriptionDate;
    private Double confidenceOverall;
    private String status; // PROCESSED, PARTIALLY_MATCHED, MANUAL_REVIEW_REQUIRED
    private List<PrescriptionOcrCandidateDTO> candidates = new ArrayList<>();

    public PrescriptionOcrDTO() {
        this.status = "PROCESSED";
        this.confidenceOverall = 0.0;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRawExtractedText() {
        return rawExtractedText;
    }

    public void setRawExtractedText(String rawExtractedText) {
        this.rawExtractedText = rawExtractedText;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorRegNumber() {
        return doctorRegNumber;
    }

    public void setDoctorRegNumber(String doctorRegNumber) {
        this.doctorRegNumber = doctorRegNumber;
    }

    public String getClinicOrHospital() {
        return clinicOrHospital;
    }

    public void setClinicOrHospital(String clinicOrHospital) {
        this.clinicOrHospital = clinicOrHospital;
    }

    public String getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(String prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public Double getConfidenceOverall() {
        return confidenceOverall;
    }

    public void setConfidenceOverall(Double confidenceOverall) {
        this.confidenceOverall = confidenceOverall;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PrescriptionOcrCandidateDTO> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<PrescriptionOcrCandidateDTO> candidates) {
        this.candidates = candidates;
    }
}
