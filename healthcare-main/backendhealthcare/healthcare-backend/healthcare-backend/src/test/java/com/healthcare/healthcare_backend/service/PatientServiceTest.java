package com.healthcare.healthcare_backend.service;

import com.healthcare.healthcare_backend.entity.Patient;
import com.healthcare.healthcare_backend.repository.PatientRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    private PatientService patientService;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        patientService =
                new PatientService(patientRepository);
    }


    // TC-PS-01: Save patient
    @Test
    void shouldSavePatientSuccessfully() {

        Patient patient = new Patient();
        patient.setId(1L);

        when(patientRepository.save(patient))
                .thenReturn(patient);

        Patient result =
                patientService.savePatient(patient);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(patientRepository).save(patient);
    }


    // TC-PS-02: Get all patients
    @Test
    void shouldGetAllPatientsSuccessfully() {

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        when(patientRepository.findAll())
                .thenReturn(List.of(patient1, patient2));

        List<Patient> result =
                patientService.getAllPatients();

        assertEquals(2, result.size());

        verify(patientRepository).findAll();
    }


    // TC-PS-03: Update medical history
    @Test
    void shouldUpdateMedicalHistorySuccessfully() {

        Patient patient = new Patient();
        patient.setId(1L);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientRepository.save(patient))
                .thenReturn(patient);

        Patient result =
                patientService.updateMedicalHistory(
                        1L,
                        "Diabetes"
                );

        assertEquals(
                "Diabetes",
                result.getMedicalHistory()
        );

        verify(patientRepository).save(patient);
    }


    // TC-PS-04: Patient not found during update
    @Test
    void shouldThrowExceptionWhenPatientNotFound() {

        when(patientRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> patientService.updateMedicalHistory(
                        999L,
                        "New history"
                )
        );

        assertEquals(
                "Patient not found",
                exception.getMessage()
        );

        verify(patientRepository, never())
                .save(any());
    }


    // TC-PS-05: Verify repository interaction
    @Test
    void shouldSavePatientOnlyOnce() {

        Patient patient = new Patient();

        when(patientRepository.save(patient))
                .thenReturn(patient);

        patientService.savePatient(patient);

        verify(patientRepository, times(1))
                .save(patient);
    }
}