package com.automeds.dto;

public class DrugInteractionAlertDTO {

    private String severity; // HIGH, MODERATE, LOW
    private String alertType; // DRUG_DRUG, ALLERGY_CONFLICT, DOSAGE_WARNING
    private String drugA;
    private String drugB;
    private String clinicalEffect;
    private String recommendedAction;

    public DrugInteractionAlertDTO() {
    }

    public DrugInteractionAlertDTO(String severity, String alertType, String drugA, String drugB, 
                                  String clinicalEffect, String recommendedAction) {
        this.severity = severity;
        this.alertType = alertType;
        this.drugA = drugA;
        this.drugB = drugB;
        this.clinicalEffect = clinicalEffect;
        this.recommendedAction = recommendedAction;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getDrugA() {
        return drugA;
    }

    public void setDrugA(String drugA) {
        this.drugA = drugA;
    }

    public String getDrugB() {
        return drugB;
    }

    public void setDrugB(String drugB) {
        this.drugB = drugB;
    }

    public String getClinicalEffect() {
        return clinicalEffect;
    }

    public void setClinicalEffect(String clinicalEffect) {
        this.clinicalEffect = clinicalEffect;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }
}
