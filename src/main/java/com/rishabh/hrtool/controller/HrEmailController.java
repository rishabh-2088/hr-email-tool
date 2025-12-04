package com.rishabh.hrtool.controller;

import com.rishabh.hrtool.model.CandidateRequest;
import com.rishabh.hrtool.model.EmailPreviewResponse;
import com.rishabh.hrtool.service.EmailService;
import com.rishabh.hrtool.service.TemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // remove or restrict in production
public class HrEmailController {

    private final TemplateService templateService;
    private final EmailService emailService;

    public HrEmailController(TemplateService templateService, EmailService emailService) {
        this.templateService = templateService;
        this.emailService = emailService;
    }

    /**
     * Preview endpoint — generate subject + body from template (does NOT send email).
     */
    @PostMapping("/preview")
    public ResponseEntity<?> preview(@Valid @RequestBody CandidateRequest req, BindingResult bindingResult) {
        // validation
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(buildValidationErrors(bindingResult));
        }

        EmailPreviewResponse preview = templateService.renderTemplate(req);
        return ResponseEntity.ok(preview);
    }

    /**
     * Send endpoint — generates content (or uses overrideBody), then sends email.
     */
    @PostMapping("/send")
    public ResponseEntity<?> send(@Valid @RequestBody CandidateRequest req, BindingResult bindingResult) {
        // validation
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(buildValidationErrors(bindingResult));
        }

        // Render template (TemplateService will respect overrideBody if present)
        EmailPreviewResponse preview = templateService.renderTemplate(req);

        // Decide whether to send HTML or plain-text (simple heuristic)
        boolean isHtml = looksLikeHtml(preview.getBody());

        try {
            emailService.sendEmail(req.getFromEmail(), req.getCandidateEmail(), preview.getSubject(), preview.getBody(), isHtml);
            Map<String, String> resp = new HashMap<>();
            resp.put("status", "ok");
            resp.put("message", "Email sent successfully");
            return ResponseEntity.ok(resp);
        } catch (MailException mex) {
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            // Do not leak sensitive config - include mex.getMessage() only for helpful debugging
            err.put("message", "Failed to send email: " + mex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    /**
     * Simple read-only endpoint to view recent sent audit records (helpful for demos).
     */
    @GetMapping("/sent")
    public ResponseEntity<List<EmailService.SentRecord>> recentSent() {
        return ResponseEntity.ok(emailService.getRecentSent());
    }

    /* ----------------- helpers ----------------- */

    private boolean looksLikeHtml(String body) {
        if (body == null) return false;
        String lower = body.toLowerCase();
        return lower.contains("<html") || lower.contains("<p") || lower.contains("<br") || lower.contains("<div");
    }

    private Map<String, Object> buildValidationErrors(BindingResult bindingResult) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", "validation_error");
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : bindingResult.getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        errors.put("errors", fieldErrors);
        return errors;
    }
}
