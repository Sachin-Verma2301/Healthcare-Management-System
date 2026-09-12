package com.healthcare.healthcare_backend.service;

import com.healthcare.healthcare_backend.dto.AppointmentRequest;
import com.healthcare.healthcare_backend.entity.Appointment;
import com.healthcare.healthcare_backend.entity.Doctor;
import com.healthcare.healthcare_backend.entity.Patient;
import com.healthcare.healthcare_backend.repository.AppointmentRepository;
import com.healthcare.healthcare_backend.repository.DoctorRepository;
import com.healthcare.healthcare_backend.repository.PatientRepository;
import com.healthcare.healthcare_backend.exception.AppointmentAlreadyExistsException;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final RestTemplate restTemplate;

    private static final String NOTIFICATION_URL = "http://localhost:8082/notifications/send";

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              RestTemplate restTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.restTemplate = restTemplate;
    }

    public Appointment createAppointment(AppointmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Appointment request cannot be null");
        }

        if (request.getAppointmentDate() == null || request.getAppointmentDate().isBlank()) {
            throw new IllegalArgumentException("Appointment date is required.");
        }

        try {
            LocalDate date = LocalDate.parse(request.getAppointmentDate().trim());
            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Cannot book an appointment for a past date: " + request.getAppointmentDate());
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD: " + request.getAppointmentDate());
        }

        if (request.getAppointmentTime() == null || request.getAppointmentTime().isBlank()) {
            throw new IllegalArgumentException("Appointment time is required.");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .or(() -> patientRepository.findByUserId(request.getPatientId()))
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .or(() -> doctorRepository.findByUserId(request.getDoctorId()))
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean alreadyBooked = appointmentRepository.existsByDoctorAndAppointmentDateAndAppointmentTime(
                doctor,
                request.getAppointmentDate(),
                request.getAppointmentTime());

        if (alreadyBooked) {
            throw new AppointmentAlreadyExistsException("Doctor is already booked at this time.");
        }

        boolean patientAlreadyBooked = appointmentRepository.existsByPatientAndAppointmentDateAndAppointmentTime(
                patient,
                request.getAppointmentDate(),
                request.getAppointmentTime());

        if (patientAlreadyBooked) {
            throw new RuntimeException("Patient already has an appointment at this time.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus("Booked");

        if (doctor.getConsultationFee() != null) {
            appointment.setConsultationFee(doctor.getConsultationFee());
        }

        Appointment saved = appointmentRepository.save(appointment);

        // 🔔 Email the patient at their registered address
        String patientName = patient.getUser() != null ? patient.getUser().getName() : "Patient";
        String patientEmail = patient.getUser() != null ? patient.getUser().getEmail() : null;
        String doctorName = doctor.getUser() != null ? doctor.getUser().getName() : "the doctor";

        String subject = "Appointment Confirmation";
        String message = "Dear " + patientName + ",\n\n"
                + "Greetings from Healthcare Management System!\n\n"
                + "Your appointment has been successfully booked with the following details:\n\n"
                + "Doctor: " + doctorName + "\n"
                + "Date: " + request.getAppointmentDate() + "\n"
                + "Time: " + request.getAppointmentTime() + "\n\n"
                + "Please arrive a few minutes early. If you need to reschedule or cancel, "
                + "please do so through the app.\n\n"
                + "Wishing you good health,\n"
                + "Healthcare Management System Team";

        sendAppointmentNotification(patientEmail, patientName, subject, message);

        return saved;
    }

    private void sendAppointmentNotification(String email, String name, String subject, String message) {
        if (email == null || email.isBlank()) {
            System.out.println("⚠️ Skipped notification — patient has no registered email.");
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("recipientEmail", email);
            payload.put("recipientName", name);
            payload.put("subject", subject);
            payload.put("message", message);
            payload.put("notificationType", "EMAIL");

            restTemplate.postForObject(NOTIFICATION_URL, payload, Map.class);
        } catch (Exception e) {
            // Don't let a notification failure block the appointment booking
            System.out.println("⚠️ Failed to send notification: " + e.getMessage());
        }
    }

    public Appointment cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus("CANCELLED");
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentByPatient(Long patientId) {
        List<Appointment> list = appointmentRepository.findByPatientId(patientId);

        if (list.isEmpty()) {
            list = appointmentRepository.findByPatientUserId(patientId);
        }

        return list;
    }

    public List<Appointment> getAppointmentByDoctor(Long doctorId) {
        List<Appointment> list = appointmentRepository.findByDoctorId(doctorId);

        if (list.isEmpty()) {
            list = appointmentRepository.findByDoctorUserId(doctorId);
        }

        return list;
    }

    public List<Appointment> getAppointmentsByStatus(String status) {
        return appointmentRepository.findByStatus(status);
    }

    public Appointment rescheduleAppointment(Long id, String newDate, String newTime) {
        if (newDate == null || newDate.isBlank()) {
            throw new IllegalArgumentException("New appointment date is required.");
        }

        try {
            LocalDate date = LocalDate.parse(newDate.trim());

            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "Cannot reschedule appointment to a past date: " + newDate);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format. Expected YYYY-MM-DD: " + newDate);
        }

        if (newTime == null || newTime.isBlank()) {
            throw new IllegalArgumentException("New appointment time is required.");
        }

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        boolean alreadyBooked =
                appointmentRepository.existsByDoctorAndAppointmentDateAndAppointmentTime(
                        appointment.getDoctor(),
                        newDate.trim(),
                        newTime.trim());

        if (alreadyBooked) {
            throw new AppointmentAlreadyExistsException(
                    "Doctor is already booked at this time.");
        }

        appointment.setAppointmentDate(newDate.trim());
        appointment.setAppointmentTime(newTime.trim());
        appointment.setStatus("Booked");

        return appointmentRepository.save(appointment);
    }

    public Appointment updatePrescription(Long id, String prescription) {
        if (prescription == null || prescription.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Prescription note cannot be empty.");
        }

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setPrescription(prescription.trim());

        return appointmentRepository.save(appointment);
    }
}