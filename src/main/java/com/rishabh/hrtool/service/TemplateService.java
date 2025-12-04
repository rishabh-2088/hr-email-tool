package com.rishabh.hrtool.service;

import com.rishabh.hrtool.model.CandidateRequest;
import com.rishabh.hrtool.model.EmailPreviewResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class TemplateService {

    @Value("${app.company.name:Our Company}")
    private String companyName;

    // Filenames under src/main/resources/templates/
    private static final String SELECTION_TEMPLATE = "templates/selection.txt";
    private static final String REJECTION_TEMPLATE = "templates/rejection.txt";

    // Default (in-code) templates as fallback
    private static final String DEFAULT_SELECTION_SUBJECT = "Congratulations — {{position}} at {{company}}";
    private static final String DEFAULT_SELECTION_BODY =
            "Dear {{name}},\n\n" +
                    "We are pleased to inform you that you have been selected for the position of {{position}} at {{company}}.\n\n" +
                    "Please reply to this email to confirm your acceptance.\n\n" +
                    "Best regards,\n" +
                    "HR Team";

    private static final String DEFAULT_REJECTION_SUBJECT = "Application outcome — {{position}} at {{company}}";
    private static final String DEFAULT_REJECTION_BODY =
            "Dear {{name}},\n\n" +
                    "Thank you for applying for the position of {{position}} at {{company}}.\n\n" +
                    "We regret to inform you that we have decided to move forward with other candidates.\n\n" +
                    "Best regards,\n" +
                    "HR Team";

    /**
     * Render best-fit template for the candidate request and return subject+body.
     * Uses req.overrideBody (if present) only for body when returning (but preview still shows rendered body).
     */
    public EmailPreviewResponse renderTemplate(CandidateRequest req) {
        String status = req.getStatus() != null ? req.getStatus().trim().toLowerCase() : "";

        String subjectTemplate;
        String bodyTemplate;

        if ("selected".equals(status) || "selection".equals(status)) {
            subjectTemplate = loadTemplateOrDefault(SELECTION_TEMPLATE, DEFAULT_SELECTION_SUBJECT);
            bodyTemplate = loadTemplateOrDefault(SELECTION_TEMPLATE.replace(".txt", ".body.txt"), DEFAULT_SELECTION_BODY);
            // Note: simpler approach uses single file for body; read selection.txt as body if you put body there
            // For compatibility we try the single file too:
            if (isDefault(subjectTemplate) && isDefault(bodyTemplate)) {
                // fallback: use default selection templates
                subjectTemplate = DEFAULT_SELECTION_SUBJECT;
                bodyTemplate = DEFAULT_SELECTION_BODY;
            }
            // If the file contains both subject and body in a single file, our loadTemplateOrDefault reads the file contents.
            // Use placeholders accordingly.
        } else {
            // treat anything else as rejection
            subjectTemplate = loadTemplateOrDefault(REJECTION_TEMPLATE, DEFAULT_REJECTION_SUBJECT);
            bodyTemplate = loadTemplateOrDefault(REJECTION_TEMPLATE.replace(".txt", ".body.txt"), DEFAULT_REJECTION_BODY);
            if (isDefault(subjectTemplate) && isDefault(bodyTemplate)) {
                subjectTemplate = DEFAULT_REJECTION_SUBJECT;
                bodyTemplate = DEFAULT_REJECTION_BODY;
            }
        }

        // In many simple setups we store full body in selection.txt or rejection.txt.
        // If loadTemplateOrDefault returned the file content for subjectTemplate (because we used the same filename),
        // then treat that as body. To keep things simple: if subjectTemplate contains newline and bodyTemplate is default,
        // assume subject is default and subjectTemplate is actually the body.
        if (subjectTemplate.contains("\n") && (DEFAULT_SELECTION_BODY.equals(bodyTemplate) || DEFAULT_REJECTION_BODY.equals(bodyTemplate))) {
            bodyTemplate = subjectTemplate;
            subjectTemplate = (status.startsWith("sel") ? DEFAULT_SELECTION_SUBJECT : DEFAULT_REJECTION_SUBJECT);
        }

        // Replace placeholders
        String subject = applyPlaceholders(subjectTemplate, req);
        String body = applyPlaceholders(bodyTemplate, req);

        // If overrideBody is present and non-empty, prefer it as the body (useful when HR edits before sending)
        if (req.getOverrideBody() != null && !req.getOverrideBody().trim().isEmpty()) {
            body = req.getOverrideBody();
        }

        return new EmailPreviewResponse(subject, body);
    }

    private boolean isDefault(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Basic placeholder replacement:
     * {{name}} -> candidate name
     * {{position}} -> position
     * {{company}} -> configured company name
     */
    private String applyPlaceholders(String template, CandidateRequest req) {
        if (template == null) return "";

        String result = template;
        String name = req.getCandidateName() != null ? req.getCandidateName() : "";
        String position = req.getPosition() != null ? req.getPosition() : "";

        result = result.replace("{{name}}", name);
        result = result.replace("{{position}}", position);
        result = result.replace("{{company}}", this.companyName);

        return result;
    }

    /**
     * Load a text file from classpath (resources). Return the file contents, or defaultValue if file not found.
     */
    private String loadTemplateOrDefault(String classpathLocation, String defaultValue) {
        try {
            ClassPathResource res = new ClassPathResource(classpathLocation);
            if (!res.exists()) {
                // try alternative: read the base filename (e.g., templates/selection.txt)
                // If it doesn't exist, return default
                return defaultValue;
            }
            byte[] bytes = Files.readAllBytes(res.getFile().toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Could not load resource — return default
            return defaultValue;
        }
    }
}
