package com.sevatrack.service;

import com.sevatrack.dao.*;
import com.sevatrack.model.*;
import com.sevatrack.routing.RoutingEngine;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ComplaintService {
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\- ]{7,15}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final ComplaintDao complaints;
    private final ReferenceDao reference;
    private final OfficerDao officers;
    private final HistoryDao history;
    private final SlaPolicy sla;
    private final RoutingEngine routing;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public ComplaintService(ComplaintDao complaints, ReferenceDao reference, OfficerDao officers,
                            HistoryDao history, SlaPolicy sla, RoutingEngine routing, Clock clock) {
        this.complaints = complaints;
        this.reference = reference;
        this.officers = officers;
        this.history = history;
        this.sla = sla;
        this.routing = routing;
        this.clock = clock;
    }

    /** Validates, stores and auto-routes a new complaint. Returns it as stored (with ticket and assignee). */
    public Complaint register(ComplaintForm form) {
        List<String> errors = new ArrayList<>();
        Category category = validate(form, errors);
        if (!errors.isEmpty()) throw new ValidationException(errors);

        LocalDateTime now = LocalDateTime.now(clock);
        Priority priority = Priority.parse(form.getPriority(), category.getDefaultPriority());

        Complaint c = new Complaint();
        c.setTicketNo(newTicket(now));
        c.setCitizenName(form.getCitizenName().trim());
        c.setCitizenPhone(form.getCitizenPhone().trim());
        c.setCitizenEmail(blankToNull(form.getCitizenEmail()));
        c.setTitle(form.getTitle().trim());
        c.setDescription(form.getDescription().trim());
        c.setCategoryId(category.getId());
        c.setWardId(form.getWardId());
        c.setDepartmentId(category.getDepartmentId());
        c.setPriority(priority);
        c.setStatus(Status.NEW);
        c.setEscalationLevel(1);
        LocalDateTime due = now.plus(sla.windowFor(priority, 1));
        c.setSlaDueAt(due);

        long id = complaints.insert(c);
        c.setId(id);

        List<Officer> candidates = officers.findActive(c.getDepartmentId(), 1);
        Optional<Officer> chosen = routing.pick(c, candidates);
        chosen.ifPresent(o -> complaints.assign(id, o.getId(), due, "SYSTEM",
                "Auto-routed to " + o.getName() + " (category, ward, priority, workload rules)"));

        return complaints.findById(id).orElseThrow(() -> new NotFoundException("Complaint vanished after insert"));
    }

    public Complaint track(String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) throw new ValidationException(List.of("Enter a ticket number"));
        return complaints.findByTicket(ticketNo.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException("No complaint found for ticket " + ticketNo.trim()));
    }

    public Complaint get(long id) {
        return complaints.findById(id).orElseThrow(() -> new NotFoundException("Complaint " + id + " not found"));
    }

    public List<StatusHistory> history(long complaintId) {
        return history.history(complaintId);
    }

    public List<EscalationRecord> escalations(long complaintId) {
        return history.escalations(complaintId);
    }

    public List<Complaint> forOfficer(int officerId) {
        return complaints.findByOfficer(officerId);
    }

    public List<Complaint> recent(int limit) {
        return complaints.findRecent(limit);
    }

    /** An officer moves one of their own complaints along the lifecycle. */
    public void updateStatus(long complaintId, int officerId, Status target, String note) {
        Complaint c = get(complaintId);
        Officer officer = officers.findById(officerId)
                .orElseThrow(() -> new NotFoundException("Officer " + officerId + " not found"));
        if (c.getAssignedOfficerId() == null || c.getAssignedOfficerId() != officerId) {
            throw new BusinessException("Complaint " + c.getTicketNo() + " is not assigned to " + officer.getName());
        }
        if (!c.getStatus().canTransitionTo(target)) {
            throw new BusinessException("Cannot move a complaint from " + c.getStatus() + " to " + target);
        }
        if (target == Status.RESOLVED && (note == null || note.isBlank())) {
            throw new BusinessException("A resolution note is required to mark a complaint as resolved");
        }
        complaints.updateStatus(complaintId, target, "OFFICER:" + officer.getName(), blankToNull(note));
    }

    // ------------------------------------------------------------------ helpers

    private Category validate(ComplaintForm f, List<String> errors) {
        if (isBlank(f.getCitizenName()) || f.getCitizenName().trim().length() > 100) errors.add("Name is required (max 100 characters)");
        if (isBlank(f.getCitizenPhone()) || !PHONE.matcher(f.getCitizenPhone().trim()).matches()) errors.add("Enter a valid phone number");
        if (!isBlank(f.getCitizenEmail()) && !EMAIL.matcher(f.getCitizenEmail().trim()).matches()) errors.add("Email address looks invalid");
        if (isBlank(f.getTitle()) || f.getTitle().trim().length() < 5 || f.getTitle().trim().length() > 150) errors.add("Title must be 5-150 characters");
        if (isBlank(f.getDescription()) || f.getDescription().trim().length() < 10 || f.getDescription().trim().length() > 2000) errors.add("Description must be 10-2000 characters");
        Category category = reference.findCategory(f.getCategoryId()).orElse(null);
        if (category == null) errors.add("Choose a valid category");
        if (reference.findWard(f.getWardId()).isEmpty()) errors.add("Choose a valid ward");
        return category;
    }

    private String newTicket(LocalDateTime now) {
        for (int attempt = 0; attempt < 5; attempt++) {
            StringBuilder sb = new StringBuilder("ST-").append(DateTimeFormatter.ofPattern("yyMMdd").format(now)).append('-');
            for (int i = 0; i < 5; i++) sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            String ticket = sb.toString();
            if (complaints.findByTicket(ticket).isEmpty()) return ticket;
        }
        throw new BusinessException("Could not allocate a ticket number, please retry");
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
