package com.automeds.service;

import com.automeds.dto.PrescriptionOcrCandidateDTO;
import com.automeds.dto.PrescriptionOcrDTO;
import com.automeds.entity.Medicine;
import com.automeds.repository.MedicineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PrescriptionOcrService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionOcrService.class);

    private final MedicineRepository medicineRepository;
    private final ObjectMapper objectMapper;
    private final boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
    private Path cachedScriptPath;

    // Regex patterns for clinical entities
    private static final Pattern DOCTOR_PATTERN = Pattern.compile("(?:(?:Dr|Doctor)\\.?\\s+([A-Za-z\\s\\.]+?)(?:,|\\r?\\n|MBBS|MD|MS|BAMS|$))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern REG_NO_PATTERN = Pattern.compile("(?:Reg(?:istration)?\\.?\\s*(?:No|#)?\\.?:?\\s*([A-Za-z0-9\\-]+))", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\b\\d{1,2}[\\/\\-\\.]\\d{1,2}[\\/\\-\\.]\\d{2,4}\\b)");
    private static final Pattern FREQUENCY_PATTERN = Pattern.compile("(\\b(?:OD|BD|BID|TDS|TID|QID|1-0-1|1-0-0|0-0-1|0-1-0|Once Daily|Twice Daily|Three Times Daily|As Directed)\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOSAGE_PATTERN = Pattern.compile("(\\b\\d+(?:\\.\\d+)?\\s*(?:mg|mcg|g|ml|IU)\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\b(?:x|for)?\\s*(\\d+)\\s*(?:days?|weeks?|months?)\\b)", Pattern.CASE_INSENSITIVE);

    public PrescriptionOcrService(MedicineRepository medicineRepository, ObjectMapper objectMapper) {
        this.medicineRepository = medicineRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Process an uploaded prescription file and perform optical/digital text extraction
     * and intelligent clinical catalog matching.
     */
    public PrescriptionOcrDTO processPrescription(MultipartFile file) {
        PrescriptionOcrDTO result = new PrescriptionOcrDTO();
        result.setFileName(file.getOriginalFilename());

        String extractedText = extractTextFromFile(file);
        result.setRawExtractedText(extractedText);

        // Extract metadata
        extractDoctorMetadata(extractedText, result);

        // Extract and match candidate medicines
        List<Medicine> allCatalog = medicineRepository.findByActive(1);
        List<PrescriptionOcrCandidateDTO> candidates = extractAndMatchMedicines(extractedText, allCatalog);
        result.setCandidates(candidates);

        // Determine overall confidence
        if (candidates.isEmpty()) {
            result.setConfidenceOverall(0.0);
            result.setStatus("MANUAL_REVIEW_REQUIRED");
        } else {
            double avgScore = candidates.stream()
                    .mapToDouble(PrescriptionOcrCandidateDTO::getConfidenceScore)
                    .average()
                    .orElse(0.0);
            result.setConfidenceOverall(BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP).doubleValue());
            result.setStatus(avgScore >= 0.80 ? "PROCESSED" : "PARTIALLY_MATCHED");
        }

        return result;
    }

    /**
     * Serialize PrescriptionOcrDTO to JSON string for persistent audit storage.
     */
    public String toJson(PrescriptionOcrDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.error("Failed to serialize OCR DTO to JSON", e);
            return null;
        }
    }

    /**
     * Deserialize JSON string to PrescriptionOcrDTO.
     */
    public PrescriptionOcrDTO fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, PrescriptionOcrDTO.class);
        } catch (Exception e) {
            log.warn("Failed to parse OCR JSON", e);
            return null;
        }
    }

    private String extractTextFromFile(MultipartFile file) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (fileName.endsWith(".txt")) {
            try {
                return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("Could not read text file: {}", e.getMessage());
            }
        }
        if (fileName.endsWith(".pdf")) {
            try (InputStream is = file.getInputStream(); PDDocument document = PDDocument.load(is)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                if (text != null && text.trim().length() > 20) {
                    return text.trim();
                }
                // Scanned PDF with no digital text: render first page and perform OCR
                if (document.getNumberOfPages() > 0) {
                    PDFRenderer renderer = new PDFRenderer(document);
                    BufferedImage bim = renderer.renderImageWithDPI(0, 200);
                    Path tempImg = Files.createTempFile("ocr_pdf_page_", ".png");
                    try {
                        ImageIO.write(bim, "PNG", tempImg.toFile());
                        return performOcr(tempImg);
                    } finally {
                        Files.deleteIfExists(tempImg);
                    }
                }
            } catch (Exception e) {
                log.warn("PDFBox could not extract text from PDF file {}: {}", fileName, e.getMessage());
            }
        }

        // Image files (jpg, jpeg, png, bmp, webp)
        try {
            String suffix = ".png";
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                suffix = ".jpg";
            }
            Path tempImg = Files.createTempFile("ocr_rx_scan_", suffix);
            try {
                Files.write(tempImg, file.getBytes());
                return performOcr(tempImg);
            } finally {
                Files.deleteIfExists(tempImg);
            }
        } catch (Exception e) {
            log.error("Failed to perform OCR on uploaded image {}: {}", fileName, e.getMessage());
        }

        return "";
    }

    /**
     * Unified cross-platform OCR dispatcher:
     * - On Windows local dev: Runs high-speed Native Windows OCR (with Tesseract fallback).
     * - In Linux / Docker / Cloud environments: Executes native Tesseract OCR engine.
     */
    private String performOcr(Path imagePath) {
        String result = "";

        // 1. If running on Windows, try Native Windows OCR engine first
        if (isWindows) {
            result = performNativeWindowsOcr(imagePath);
            if (result != null && !result.isBlank()) {
                return result;
            }
        }

        // 2. Try Tesseract OCR (standard on Linux Docker / cross-platform)
        result = performTesseractOcr(imagePath);
        if (result != null && !result.isBlank()) {
            return result;
        }

        log.info("No optical text could be recognized from scan {}", imagePath.getFileName());
        return "";
    }

    private String performNativeWindowsOcr(Path imagePath) {
        Path scriptPath = getOcrScriptPath();
        if (scriptPath == null || !Files.exists(scriptPath)) {
            log.warn("Windows Native OCR script not found; skipping Windows OCR execution.");
            return "";
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-ExecutionPolicy", "Bypass",
                    "-File", scriptPath.toAbsolutePath().toString(),
                    "-ImagePath", imagePath.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        sb.append(line.trim()).append("\n");
                    }
                }
            }

            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("Windows OCR process timed out for image {}", imagePath);
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("Error executing Windows OCR script on {}: {}", imagePath, e.getMessage());
            return "";
        }
    }

    private String performTesseractOcr(Path imagePath) {
        try {
            String tesseractCmd = isWindows ? "tesseract.exe" : "tesseract";
            ProcessBuilder pb = new ProcessBuilder(
                    tesseractCmd,
                    imagePath.toAbsolutePath().toString(),
                    "stdout",
                    "-l", "eng"
            );
            pb.redirectErrorStream(false);
            Process process = pb.start();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        sb.append(line.trim()).append("\n");
                    }
                }
            }

            boolean finished = process.waitFor(25, TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0 && sb.length() > 0) {
                log.info("Tesseract OCR successfully extracted {} characters from {}", sb.length(), imagePath.getFileName());
                return sb.toString().trim();
            }
        } catch (Exception e) {
            log.debug("Tesseract CLI not available in environment: {}", e.getMessage());
        }
        return "";
    }

    private synchronized Path getOcrScriptPath() {
        if (cachedScriptPath != null && Files.exists(cachedScriptPath)) {
            return cachedScriptPath;
        }

        // 1. Check local source directory in development
        Path devPath = Paths.get("src/main/resources/scripts/ocr_extractor.ps1");
        if (Files.exists(devPath)) {
            cachedScriptPath = devPath;
            return cachedScriptPath;
        }

        // 2. Extract from classpath resource if packaged
        try (InputStream is = getClass().getResourceAsStream("/scripts/ocr_extractor.ps1")) {
            if (is != null) {
                Path temp = Files.createTempFile("automeds_ocr_", ".ps1");
                Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
                temp.toFile().deleteOnExit();
                cachedScriptPath = temp;
                return cachedScriptPath;
            }
        } catch (Exception e) {
            log.error("Failed to load OCR script from classpath: {}", e.getMessage());
        }

        return null;
    }

    private void extractDoctorMetadata(String text, PrescriptionOcrDTO dto) {
        if (text == null || text.isBlank()) {
            dto.setDoctorName("Pending Pharmacist Verification");
            dto.setDoctorRegNumber("Not detected in scan");
            dto.setClinicOrHospital("Not detected in scan");
            dto.setPrescriptionDate(LocalDate.now().toString());
            return;
        }

        Matcher docMatcher = DOCTOR_PATTERN.matcher(text);
        if (docMatcher.find()) {
            String name = docMatcher.group(1).trim().replaceAll("[\\,\\.]+$", "");
            dto.setDoctorName("Dr. " + name);
        } else {
            dto.setDoctorName("Pending Pharmacist Verification");
        }

        Matcher regMatcher = REG_NO_PATTERN.matcher(text);
        if (regMatcher.find()) {
            dto.setDoctorRegNumber(regMatcher.group(1).trim());
        } else {
            dto.setDoctorRegNumber("Not detected in scan");
        }

        Matcher dateMatcher = DATE_PATTERN.matcher(text);
        if (dateMatcher.find()) {
            dto.setPrescriptionDate(dateMatcher.group(1).trim());
        } else {
            dto.setPrescriptionDate(LocalDate.now().toString());
        }

        boolean foundClinic = false;
        if (text.toLowerCase().contains("hospital") || text.toLowerCase().contains("clinic") || text.toLowerCase().contains("center") || text.toLowerCase().contains("care")) {
            for (String line : text.split("\n")) {
                String lower = line.toLowerCase();
                if (lower.contains("hospital") || lower.contains("clinic") || lower.contains("healthcare") || lower.contains("care center")) {
                    dto.setClinicOrHospital(line.trim());
                    foundClinic = true;
                    break;
                }
            }
        }
        if (!foundClinic) {
            dto.setClinicOrHospital("Not detected in scan");
        }
    }

    private List<PrescriptionOcrCandidateDTO> extractAndMatchMedicines(String text, List<Medicine> catalog) {
        List<PrescriptionOcrCandidateDTO> results = new ArrayList<>();
        if (text == null || catalog == null || catalog.isEmpty()) {
            return results;
        }

        String[] lines = text.split("\n");
        Set<Long> matchedIds = new HashSet<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.length() < 3 || isHeaderOrFooter(line)) {
                continue;
            }

            // Find best matching catalog medicine for this line
            Medicine bestMatch = null;
            double highestScore = 0.0;

            for (Medicine med : catalog) {
                double score = computeMatchScore(line, med);
                if (score > highestScore && score >= 0.45) {
                    highestScore = score;
                    bestMatch = med;
                }
            }

            if (bestMatch != null && !matchedIds.contains(bestMatch.getId())) {
                matchedIds.add(bestMatch.getId());
                PrescriptionOcrCandidateDTO candidate = buildCandidate(line, bestMatch, highestScore, catalog);
                results.add(candidate);
            }
        }

        // If no line matches were found, do a token-based search across the whole text
        if (results.isEmpty()) {
            for (Medicine med : catalog) {
                if (matchedIds.size() >= 4) break;
                double score = computeMatchScore(text, med);
                if (score >= 0.70 && !matchedIds.contains(med.getId())) {
                    matchedIds.add(med.getId());
                    results.add(buildCandidate(med.getMedicineName() + " " + med.getStrength(), med, score, catalog));
                }
            }
        }

        return results;
    }

    private boolean isHeaderOrFooter(String line) {
        String lower = line.toLowerCase();
        return lower.startsWith("doctor:") || lower.startsWith("date:") || lower.startsWith("clinic:")
                || lower.startsWith("reg") || lower.startsWith("prescription document") || lower.equals("rx lines:");
    }

    private double computeMatchScore(String line, Medicine med) {
        String lowerLine = line.toLowerCase();
        String medName = med.getMedicineName().toLowerCase();
        String brandName = med.getBrandName() != null ? med.getBrandName().toLowerCase() : "";
        String comp = med.getComposition() != null ? med.getComposition().toLowerCase() : "";

        // Exact name or brand contains
        if (lowerLine.contains(medName) || (!brandName.isBlank() && lowerLine.contains(brandName))) {
            return 0.95;
        }

        // Composition contains
        if (!comp.isBlank() && lowerLine.contains(comp)) {
            return 0.88;
        }

        // Token-level matching
        String[] tokens = lowerLine.replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s+");
        for (String token : tokens) {
            if (token.length() >= 4) {
                if (medName.contains(token) || brandName.contains(token) || comp.contains(token)) {
                    return 0.82;
                }
                // Partial string similarity
                double simName = calculateLevenshteinSimilarity(token, medName);
                if (simName >= 0.75) return simName * 0.9;
            }
        }

        return 0.0;
    }

    private PrescriptionOcrCandidateDTO buildCandidate(String line, Medicine med, double score, List<Medicine> catalog) {
        PrescriptionOcrCandidateDTO candidate = new PrescriptionOcrCandidateDTO();
        candidate.setDetectedText(line);
        candidate.setMedicineId(med.getId());
        candidate.setMedicineName(med.getMedicineName());
        candidate.setBrandName(med.getBrandName());
        candidate.setComposition(med.getComposition());
        candidate.setStrength(med.getStrength());
        candidate.setPrice(med.getPrice());
        candidate.setConfidenceScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP).doubleValue());

        if (score >= 0.85) {
            candidate.setConfidenceBadge("HIGH_CONFIDENCE");
        } else if (score >= 0.60) {
            candidate.setConfidenceBadge("MEDIUM_CONFIDENCE");
        } else {
            candidate.setConfidenceBadge("LOW_CONFIDENCE");
        }

        // Parse dosage from line or default to standard
        Matcher dosageMatcher = DOSAGE_PATTERN.matcher(line);
        if (dosageMatcher.find()) {
            candidate.setDosage("1 tablet (" + dosageMatcher.group(1) + ")");
        } else {
            candidate.setDosage("1 tablet (" + med.getStrength() + ")");
        }

        // Parse frequency
        Matcher freqMatcher = FREQUENCY_PATTERN.matcher(line);
        if (freqMatcher.find()) {
            String f = freqMatcher.group(1).toUpperCase();
            if (f.equals("1-0-1") || f.equals("BD") || f.equals("BID") || f.contains("TWICE")) {
                candidate.setFrequency("Twice Daily");
            } else if (f.equals("1-0-0") || f.equals("0-1-0") || f.equals("0-0-1") || f.equals("OD") || f.contains("ONCE")) {
                candidate.setFrequency("Once Daily");
            } else if (f.equals("1-1-1") || f.equals("TDS") || f.equals("TID") || f.contains("THREE")) {
                candidate.setFrequency("Every 12 Hours");
            } else {
                candidate.setFrequency("As Directed by Physician");
            }
        } else {
            candidate.setFrequency("Once Daily");
        }

        // Parse quantity / duration
        Matcher durMatcher = DURATION_PATTERN.matcher(line);
        if (durMatcher.find()) {
            int days = Integer.parseInt(durMatcher.group(2));
            candidate.setQuantity(days <= 60 ? days : 30);
        } else {
            candidate.setQuantity(30);
        }

        // Find generic alternative for cost comparison
        findGenericSavings(med, catalog, candidate);

        return candidate;
    }

    private void findGenericSavings(Medicine med, List<Medicine> catalog, PrescriptionOcrCandidateDTO candidate) {
        if (med.getComposition() == null || med.getComposition().isBlank()) return;

        for (Medicine other : catalog) {
            if (!other.getId().equals(med.getId()) &&
                    other.getComposition().equalsIgnoreCase(med.getComposition()) &&
                    other.getPrice().compareTo(med.getPrice()) < 0) {

                candidate.setGenericAlternative(other.getMedicineName() + " (" + other.getBrandName() + ")");
                candidate.setGenericPrice(other.getPrice());

                BigDecimal diff = med.getPrice().subtract(other.getPrice());
                BigDecimal pct = diff.divide(med.getPrice(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                candidate.setSavingsPercentage(pct.intValue());
                break;
            }
        }
    }

    private double calculateLevenshteinSimilarity(String s1, String s2) {
        int dist = computeLevenshteinDistance(s1.toLowerCase(), s2.toLowerCase());
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - ((double) dist / maxLen);
    }

    private int computeLevenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) prev[j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            int[] curr = new int[s2.length() + 1];
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            prev = curr;
        }
        return prev[s2.length()];
    }
}
