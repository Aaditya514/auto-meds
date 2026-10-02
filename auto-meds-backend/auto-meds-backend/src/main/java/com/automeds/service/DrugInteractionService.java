package com.automeds.service;

import com.automeds.dto.DrugInteractionAlertDTO;
import com.automeds.entity.Medicine;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DrugInteractionService {

    private static class InteractionRule {
        String component1;
        String component2;
        String severity;
        String effect;
        String action;

        InteractionRule(String c1, String c2, String severity, String effect, String action) {
            this.component1 = c1.toLowerCase();
            this.component2 = c2.toLowerCase();
            this.severity = severity;
            this.effect = effect;
            this.action = action;
        }

        boolean matches(String s1, String s2) {
            String lower1 = s1.toLowerCase();
            String lower2 = s2.toLowerCase();
            return (lower1.contains(component1) && lower2.contains(component2)) ||
                   (lower1.contains(component2) && lower2.contains(component1));
        }
    }

    private final List<InteractionRule> rules = new ArrayList<>();

    public DrugInteractionService() {
        // High-Risk Clinical Interactions
        rules.add(new InteractionRule("aspirin", "warfarin", "HIGH", 
                "Severe synergistic bleeding and hemorrhagic risk due to platelet inhibition and vitamin K antagonism.", 
                "Contraindicated. Monitor INR closely or consult prescribing physician before co-dispensing."));

        rules.add(new InteractionRule("ibuprofen", "aspirin", "HIGH", 
                "NSAID competitive Cox-1 inhibition blunts cardioprotective antiplatelet effect of aspirin and increases GI ulceration.", 
                "Separate administration by at least 2 to 4 hours or replace with Paracetamol for analgesia."));

        rules.add(new InteractionRule("amlodipine", "simvastatin", "HIGH", 
                "Amlodipine increases Simvastatin peak plasma concentration via CYP3A4 inhibition, increasing risk of severe myopathy and rhabdomyolysis.", 
                "Ensure Simvastatin daily dose does not exceed 20mg or consider alternative statin (Atorvastatin)."));

        rules.add(new InteractionRule("metformin", "contrast", "HIGH", 
                "Potential reduction in renal function can cause dangerous Metformin accumulation and lactic acidosis.", 
                "Withhold Metformin 48 hours prior to and post iodinated contrast imaging."));

        // Moderate-Risk Interactions
        rules.add(new InteractionRule("telmisartan", "spironolactone", "MODERATE", 
                "Concomitant renin-angiotensin-aldosterone blockade may lead to hyperkalemia, renal impairment, and severe hypotension.", 
                "Monitor serum potassium and renal function panels regularly."));

        rules.add(new InteractionRule("pantoprazole", "clopidogrel", "MODERATE", 
                "Proton pump inhibitor (PPI) may reduce antiplatelet bioactivation of Clopidogrel.", 
                "Evaluate separating doses or switching to Famotidine / Rabeprazole if indicated."));

        rules.add(new InteractionRule("atorvastatin", "clarithromycin", "MODERATE", 
                "Macrolide antibiotic inhibits CYP3A4 metabolism of Atorvastatin, elevating statin toxicity risk.", 
                "Consider temporarily pausing statin therapy during antibiotic course."));

        rules.add(new InteractionRule("metformin", "glipizide", "MODERATE", 
                "Dual antidiabetic therapy increases risk of acute hypoglycemic episodes.", 
                "Advise patient on frequent blood glucose self-monitoring and emergency carbohydrate intake."));

        rules.add(new InteractionRule("paracetamol", "alcohol", "MODERATE", 
                "Chronic concurrent intake potentiates hepatic CYP2E1 conversion into hepatotoxic NAPQI metabolite.", 
                "Advise avoiding alcohol while on scheduled Paracetamol therapy."));
    }

    /**
     * Check interactions between a list of medicines being ordered or subscribed.
     */
    public List<DrugInteractionAlertDTO> checkInteractions(List<Medicine> medicines, String patientAllergies) {
        List<DrugInteractionAlertDTO> alerts = new ArrayList<>();
        if (medicines == null || medicines.isEmpty()) {
            return alerts;
        }

        // 1. Check pairwise Drug-Drug Interactions
        for (int i = 0; i < medicines.size(); i++) {
            for (int j = i + 1; j < medicines.size(); j++) {
                Medicine m1 = medicines.get(i);
                Medicine m2 = medicines.get(j);

                String text1 = (m1.getMedicineName() + " " + (m1.getComposition() != null ? m1.getComposition() : "")).toLowerCase();
                String text2 = (m2.getMedicineName() + " " + (m2.getComposition() != null ? m2.getComposition() : "")).toLowerCase();

                for (InteractionRule rule : rules) {
                    if (rule.matches(text1, text2)) {
                        alerts.add(new DrugInteractionAlertDTO(
                                rule.severity,
                                "DRUG_DRUG",
                                m1.getMedicineName(),
                                m2.getMedicineName(),
                                rule.effect,
                                rule.action
                        ));
                    }
                }
            }
        }

        // 2. Check Patient Allergies against medicines
        if (patientAllergies != null && !patientAllergies.isBlank()) {
            String[] allergyTokens = patientAllergies.toLowerCase().split("[,;\\s]+");
            for (Medicine med : medicines) {
                String medText = (med.getMedicineName() + " " + (med.getComposition() != null ? med.getComposition() : "")).toLowerCase();
                for (String allergen : allergyTokens) {
                    if (allergen.length() >= 3 && matchesAllergy(medText, allergen)) {
                        alerts.add(new DrugInteractionAlertDTO(
                                "HIGH",
                                "ALLERGY_CONFLICT",
                                med.getMedicineName(),
                                "Patient Known Allergy: " + allergen.toUpperCase(),
                                "Patient profile records an allergy to '" + allergen + "'. Dispensing this formulation risks an adverse allergic reaction.",
                                "Do not dispense without consulting physician for an allergen-free alternative."
                        ));
                        break;
                    }
                }
            }
        }

        return alerts;
    }

    private boolean matchesAllergy(String medText, String allergen) {
        String lowerMed = medText.toLowerCase();
        String lowerAllergen = allergen.toLowerCase();

        if (lowerMed.contains(lowerAllergen)) {
            return true;
        }

        // Penicillin class cross-reactivity (Amoxicillin, Ampicillin, etc.)
        if (lowerAllergen.contains("penicillin") || lowerAllergen.equals("cillin")) {
            return lowerMed.contains("cillin");
        }

        // Sulfa / Sulfonamide cross-reactivity
        if (lowerAllergen.contains("sulfa") || lowerAllergen.contains("sulfonamide")) {
            return lowerMed.contains("sulf") || lowerMed.contains("co-trimoxazole");
        }

        // NSAID cross-reactivity
        if (lowerAllergen.contains("nsaid") || lowerAllergen.contains("aspirin")) {
            return lowerMed.contains("ibuprofen") || lowerMed.contains("aspirin") || 
                   lowerMed.contains("naproxen") || lowerMed.contains("diclofenac");
        }

        return false;
    }
}
