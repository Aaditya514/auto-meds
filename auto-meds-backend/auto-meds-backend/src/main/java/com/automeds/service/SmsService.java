package com.automeds.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * H1 - SMS Refill Confirmation Service
 *
 * Sends SMS via Twilio REST API. In dev/test environments, SMS is disabled by default
 * (automeds.sms.enabled=false) and all calls are no-ops logged at DEBUG level.
 *
 * To enable in production:
 *   automeds.sms.enabled=true
 *   automeds.sms.twilio.account-sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   automeds.sms.twilio.auth-token=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   automeds.sms.twilio.from-number=+1XXXXXXXXXX
 */
@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private static final String TWILIO_API_URL = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";

    @Value("${automeds.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${automeds.sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${automeds.sms.twilio.auth-token:}")
    private String authToken;

    @Value("${automeds.sms.twilio.from-number:}")
    private String fromNumber;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends a plain text SMS to the given phone number.
     * Silently no-ops if SMS is disabled or phone number is blank.
     *
     * @param toPhone  E.164 format phone number, e.g. "+919876543210"
     * @param message  Message body (max 160 chars for a single SMS segment)
     */
    public void sendSms(String toPhone, String message) {
        if (!smsEnabled) {
            logger.debug("[SMS DISABLED] Would have sent to {}: {}", toPhone, message);
            return;
        }

        if (toPhone == null || toPhone.isBlank()) {
            logger.warn("SMS skipped — no phone number registered for user");
            return;
        }

        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()) {
            logger.error("SMS enabled but Twilio credentials not configured. Set automeds.sms.twilio.account-sid and auth-token.");
            return;
        }

        try {
            String url = String.format(TWILIO_API_URL, accountSid);

            // Basic Auth header: Base64(accountSid:authToken)
            String credentials = accountSid + ":" + authToken;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedCredentials);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("To", toPhone);
            form.add("From", fromNumber);
            form.add("Body", message);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
            restTemplate.postForEntity(url, request, String.class);

            logger.info("SMS sent successfully to {}", toPhone);
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", toPhone, e.getMessage());
            // SMS failure is non-fatal — refill still proceeds via in-app notification
        }
    }

    /**
     * Sends the standard auto-refill confirmation SMS.
     */
    public void sendRefillConfirmationSms(String toPhone, String patientName, String medicineName, int quantity) {
        String message = String.format(
                "AutoMeds: Hi %s! Your monthly refill of %s (%d units) is ready to dispatch. " +
                "Reply YES to confirm or NO to skip this cycle. -AutoMeds Pharmacy",
                patientName, medicineName, quantity
        );
        sendSms(toPhone, message);
    }

    /**
     * Sends a low-stock alert SMS to the patient when their subscribed medicine is out of stock.
     */
    public void sendStockAlertSms(String toPhone, String patientName, String medicineName) {
        String message = String.format(
                "AutoMeds: Hi %s, your subscribed medicine %s is currently out of stock. " +
                "Our team has been alerted to restock. We will notify you when it is back. -AutoMeds",
                patientName, medicineName
        );
        sendSms(toPhone, message);
    }
}

