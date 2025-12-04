package com.rishabh.hrtool.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Default "from" address (can be overridden per-request by CandidateRequest.fromEmail)
    @Value("${app.mail.from:#{null}}")
    private String defaultFrom;

    // small in-memory audit (keep last N records)
    private final Deque<SentRecord> audit = new LinkedList<>();
    private static final int MAX_AUDIT = 10;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send an email. If isHtml is true, send HTML using MimeMessage; otherwise send plain text.
     *
     * @param from    sender address (if null/empty, defaultFrom is used)
     * @param to      recipient address
     * @param subject subject line
     * @param body    message body
     * @param isHtml  whether body contains HTML
     * @throws MailException when sending fails (propagated so controller can return meaningful response)
     */
    public void sendEmail(String from, String to, String subject, String body, boolean isHtml) throws MailException {
        String fromAddress = (from != null && !from.trim().isEmpty()) ? from : defaultFrom;

        if (fromAddress == null || fromAddress.trim().isEmpty()) {
            throw new MailException("No sender address configured. Set app.mail.from or include fromEmail in request.") {};
        }

        if (isHtml) {
            sendHtmlEmail(fromAddress, to, subject, body);
        } else {
            sendPlainTextEmail(fromAddress, to, subject, body);
        }

        recordAudit(to, subject);
    }

    private void sendPlainTextEmail(String from, String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }

    private void sendHtmlEmail(String from, String to, String subject, String htmlBody) {
        MimeMessage mime = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            // true = isHtml
            helper.setText(htmlBody, true);
            mailSender.send(mime);
        } catch (Exception ex) {
            // wrap into a MailException so callers can handle uniformly
            throw new MailException("Failed to send HTML email: " + ex.getMessage()) {};
        }
    }

    private void recordAudit(String to, String subject) {
        SentRecord r = new SentRecord(Instant.now().toString(), to, subject);
        synchronized (audit) {
            audit.addFirst(r);
            while (audit.size() > MAX_AUDIT) {
                audit.removeLast();
            }
        }
    }

    /**
     * Return a snapshot list of recent sent records (most recent first).
     */
    public List<SentRecord> getRecentSent() {
        synchronized (audit) {
            return Collections.unmodifiableList(audit.stream().collect(Collectors.toList()));
        }
    }

    // Simple DTO for audit entries
    public static class SentRecord {
        private final String timestamp;
        private final String to;
        private final String subject;

        public SentRecord(String timestamp, String to, String subject) {
            this.timestamp = timestamp;
            this.to = to;
            this.subject = subject;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }
    }
}
