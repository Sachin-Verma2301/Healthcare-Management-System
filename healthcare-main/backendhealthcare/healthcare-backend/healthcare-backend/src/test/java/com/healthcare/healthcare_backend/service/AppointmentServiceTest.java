package com.healthcare.healthcare_backend.service;

import com.healthcare.healthcare_backend.dto.AppointmentRequest;
import com.healthcare.healthcare_backend.entity.Appointment;
import com.healthcare.healthcare_backend.entity.Doctor;
import com.healthcare.healthcare_backend.entity.Patient;
import com.healthcare.healthcare_backend.exception.AppointmentAlreadyExistsException;
import com.healthcare.healthcare_backend.repository.AppointmentRepository;
import com.healthcare.healthcare_backend.repository.DoctorRepository;
import com.healthcare.healthcare_backend.repository.PatientRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    private AppointmentService appointmentService;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        appointmentService = new AppointmentService(
                appointmentRepository,
                patientRepository,
                doctorRepository
        );
    }


    private AppointmentRequest createValidRequest() {

        AppointmentRequest request = new AppointmentRequest();

        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setAppointmentDate("2026-10-10");
        request.setAppointmentTime("10:00 AM");

        return request;
    }


    // =========================================================
    // createAppointment() TEST CASES
    // =========================================================


    // TC-AS-01: Create appointment successfully
    @Test
    void shouldCreateAppointmentSuccessfully() {

        Patient patient = new Patient();
        patient.setId(1L);

        Doctor doctor = new Doctor();
        doctor.setId(2L);
        doctor.setConsultationFee(500.0);

        AppointmentRequest request = createValidRequest();

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateAndAppointmentTime(
                        doctor,
                        "2026-10-10",
                        "10:00 AM"
                ))
                .thenReturn(false);

        when(appointmentRepository
                .existsByPatientAndAppointmentDateAndAppointmentTime(
                        patient,
                        "2026-10-10",
                        "10:00 AM"
                ))
                .thenReturn(false);

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result =
                appointmentService.createAppointment(request);

        assertNotNull(result);
        assertEquals(patient, result.getPatient());
        assertEquals(doctor, result.getDoctor());
        assertEquals("2026-10-10", result.getAppointmentDate());
        assertEquals("10:00 AM", result.getAppointmentTime());
        assertEquals("Booked", result.getStatus());
        assertEquals(500.0, result.getConsultationFee());

        verify(appointmentRepository)
                .save(any(Appointment.class));
    }


    // TC-AS-02: Patient not found
    @Test
    void shouldThrowExceptionWhenPatientNotFound() {

        AppointmentRequest request = createValidRequest();

        when(patientRepository.findById(1L))
                .thenReturn(Optional.empty());

        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals("Patient not found", exception.getMessage());

        verify(appointmentRepository, never())
                .save(any());
    }


    // TC-AS-03: Doctor not found
    @Test
    void shouldThrowExceptionWhenDoctorNotFound() {

        Patient patient = new Patient();

        AppointmentRequest request = createValidRequest();

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.empty());

        when(doctorRepository.findByUserId(2L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals("Doctor not found", exception.getMessage());

        verify(appointmentRepository, never())
                .save(any());
    }


    // TC-AS-04: Appointment date is missing
    @Test
    void shouldThrowExceptionWhenAppointmentDateIsMissing() {

        Patient patient = new Patient();
        Doctor doctor = new Doctor();

        AppointmentRequest request = createValidRequest();
        request.setAppointmentDate(null);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(
                "Appointment date is required.",
                exception.getMessage()
        );
    }


    // TC-AS-05: Appointment time is missing
    @Test
    void shouldThrowExceptionWhenAppointmentTimeIsMissing() {

        Patient patient = new Patient();
        Doctor doctor = new Doctor();

        AppointmentRequest request = createValidRequest();
        request.setAppointmentTime(null);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(
                "Appointment time is required.",
                exception.getMessage()
        );
    }


    // TC-AS-06: Doctor already booked
    @Test
    void shouldThrowExceptionWhenDoctorAlreadyBooked() {

        Patient patient = new Patient();
        Doctor doctor = new Doctor();

        AppointmentRequest request = createValidRequest();

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateAndAppointmentTime(
                        doctor,
                        "2026-10-10",
                        "10:00 AM"
                ))
                .thenReturn(true);

        AppointmentAlreadyExistsException exception = assertThrows(
                AppointmentAlreadyExistsException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(
                "Doctor is already booked at this time.",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .save(any());
    }


    // TC-AS-07: Patient already has an appointment
    @Test
    void shouldThrowExceptionWhenPatientAlreadyBooked() {

        Patient patient = new Patient();
        Doctor doctor = new Doctor();

        AppointmentRequest request = createValidRequest();

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateAndAppointmentTime(
                        doctor,
                        "2026-10-10",
                        "10:00 AM"
                ))
                .thenReturn(false);

        when(appointmentRepository
                .existsByPatientAndAppointmentDateAndAppointmentTime(
                        patient,
                        "2026-10-10",
                        "10:00 AM"
                ))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(
                "Patient already has an appointment at this time.",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .save(any());
    }


    // =========================================================
    // cancelAppointment() TEST CASES
    // =========================================================


    // TC-AS-08: Cancel appointment successfully
    @Test
    void shouldCancelAppointmentSuccessfully() {

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus("Booked");

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(appointment))
                .thenReturn(appointment);

        Appointment result =
                appointmentService.cancelAppointment(1L);

        assertEquals("CANCELLED", result.getStatus());

        verify(appointmentRepository).save(appointment);
    }


    // TC-AS-09: Cancel non-existing appointment
    @Test
    void shouldThrowExceptionWhenCancellingMissingAppointment() {

        when(appointmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.cancelAppointment(999L)
        );

        assertEquals(
                "Appointment not found",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .save(any());
    }


    // =========================================================
    // getAllAppointments() TEST CASE
    // =========================================================


    // TC-AS-10: Get all appointments
    @Test
    void shouldGetAllAppointmentsSuccessfully() {

        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();

        when(appointmentRepository.findAll())
                .thenReturn(List.of(appointment1, appointment2));

        List<Appointment> result =
                appointmentService.getAllAppointments();

        assertEquals(2, result.size());

        verify(appointmentRepository).findAll();
    }


    // =========================================================
    // GET BY PATIENT / DOCTOR / STATUS
    // =========================================================


    // TC-AS-11: Get appointments by patient
    @Test
    void shouldGetAppointmentsByPatient() {

        Appointment appointment = new Appointment();

        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(List.of(appointment));

        List<Appointment> result =
                appointmentService.getAppointmentByPatient(1L);

        assertEquals(1, result.size());
    }


    // TC-AS-12: Get appointments by doctor
    @Test
    void shouldGetAppointmentsByDoctor() {

        Appointment appointment = new Appointment();

        when(appointmentRepository.findByDoctorId(2L))
                .thenReturn(List.of(appointment));

        List<Appointment> result =
                appointmentService.getAppointmentByDoctor(2L);

        assertEquals(1, result.size());
    }


    // TC-AS-13: Get appointments by status
    @Test
    void shouldGetAppointmentsByStatus() {

        Appointment appointment = new Appointment();
        appointment.setStatus("Booked");

        when(appointmentRepository.findByStatus("Booked"))
                .thenReturn(List.of(appointment));

        List<Appointment> result =
                appointmentService.getAppointmentsByStatus("Booked");

        assertEquals(1, result.size());
        assertEquals(
                "Booked",
                result.get(0).getStatus()
        );
    }


    // =========================================================
    // rescheduleAppointment() TEST CASES
    // =========================================================


    // TC-AS-14: Reschedule appointment successfully
    @Test
    void shouldRescheduleAppointmentSuccessfully() {

        Doctor doctor = new Doctor();

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctor(doctor);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateAndAppointmentTime(
                        doctor,
                        "2026-10-15",
                        "02:00 PM"
                ))
                .thenReturn(false);

        when(appointmentRepository.save(appointment))
                .thenReturn(appointment);

        Appointment result =
                appointmentService.rescheduleAppointment(
                        1L,
                        "2026-10-15",
                        "02:00 PM"
                );

        assertEquals(
                "2026-10-15",
                result.getAppointmentDate()
        );

        assertEquals(
                "02:00 PM",
                result.getAppointmentTime()
        );

        assertEquals(
                "Booked",
                result.getStatus()
        );

        verify(appointmentRepository).save(appointment);
    }


    // TC-AS-15: Doctor already booked during rescheduling
    @Test
    void shouldThrowExceptionWhenDoctorBookedDuringReschedule() {

        Doctor doctor = new Doctor();

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateAndAppointmentTime(
                        doctor,
                        "2026-10-15",
                        "02:00 PM"
                ))
                .thenReturn(true);

        AppointmentAlreadyExistsException exception = assertThrows(
                AppointmentAlreadyExistsException.class,
                () -> appointmentService.rescheduleAppointment(
                        1L,
                        "2026-10-15",
                        "02:00 PM"
                )
        );

        assertEquals(
                "Doctor is already booked at this time.",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .save(any());
    }


    // TC-AS-16: Reschedule non-existing appointment
    @Test
    void shouldThrowExceptionWhenReschedulingMissingAppointment() {

        when(appointmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appointmentService.rescheduleAppointment(
                        999L,
                        "2026-10-15",
                        "02:00 PM"
                )
        );

        assertEquals(
                "Appointment not found",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .save(any());
    }
}