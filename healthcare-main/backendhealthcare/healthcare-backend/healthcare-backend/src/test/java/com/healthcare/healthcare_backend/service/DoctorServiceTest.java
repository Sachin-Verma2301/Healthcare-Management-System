package com.healthcare.healthcare_backend.service;

import com.healthcare.healthcare_backend.entity.Doctor;
import com.healthcare.healthcare_backend.repository.DoctorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    private DoctorService doctorService;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        doctorService =
                new DoctorService(doctorRepository);
    }


    // TC-DS-01: Save doctor
    @Test
    void shouldSaveDoctorSuccessfully() {

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setSpecialization("Cardiology");

        when(doctorRepository.save(doctor))
                .thenReturn(doctor);

        Doctor result =
                doctorService.saveDoctor(doctor);

        assertNotNull(result);
        assertEquals(
                "Cardiology",
                result.getSpecialization()
        );
    }


    // TC-DS-02: Get all doctors
    @Test
    void shouldGetAllDoctorsSuccessfully() {

        Doctor doctor1 = new Doctor();
        Doctor doctor2 = new Doctor();

        when(doctorRepository.findAll())
                .thenReturn(List.of(doctor1, doctor2));

        List<Doctor> result =
                doctorService.getAllDoctors();

        assertEquals(2, result.size());
    }


    // TC-DS-03: Update availability
    @Test
    void shouldUpdateAvailabilitySuccessfully() {

        Doctor doctor = new Doctor();
        doctor.setId(1L);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(doctorRepository.save(doctor))
                .thenReturn(doctor);

        Doctor result =
                doctorService.updateAvailability(
                        1L,
                        "Available"
                );

        assertEquals(
                "Available",
                result.getAvailability()
        );
    }


    // TC-DS-04: Doctor not found while updating availability
    @Test
    void shouldThrowExceptionWhenDoctorNotFoundForAvailability() {

        when(doctorRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> doctorService.updateAvailability(
                        999L,
                        "Available"
                )
        );

        assertEquals(
                "Doctor not found",
                exception.getMessage()
        );
    }


    // TC-DS-05: Update consultation fee
    @Test
    void shouldUpdateConsultationFeeSuccessfully() {

        Doctor doctor = new Doctor();
        doctor.setId(1L);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(doctorRepository.save(doctor))
                .thenReturn(doctor);

        Doctor result =
                doctorService.updateConsultationFee(
                        1L,
                        800.0
                );

        assertEquals(
                800.0,
                result.getConsultationFee()
        );
    }


    // TC-DS-06: Doctor not found while updating fee
    @Test
    void shouldThrowExceptionWhenDoctorNotFoundForFee() {

        when(doctorRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> doctorService.updateConsultationFee(
                        999L,
                        800.0
                )
        );

        assertEquals(
                "Doctor not found",
                exception.getMessage()
        );
    }


    // TC-DS-07: Verify doctor save interaction
    @Test
    void shouldSaveDoctorOnlyOnce() {

        Doctor doctor = new Doctor();

        when(doctorRepository.save(doctor))
                .thenReturn(doctor);

        doctorService.saveDoctor(doctor);

        verify(doctorRepository, times(1))
                .save(doctor);
    }
}