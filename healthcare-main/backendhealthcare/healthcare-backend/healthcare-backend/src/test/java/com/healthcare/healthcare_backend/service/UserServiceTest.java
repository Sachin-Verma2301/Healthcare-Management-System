package com.healthcare.healthcare_backend.service;

import com.healthcare.healthcare_backend.dto.RegisterRequest;
import com.healthcare.healthcare_backend.entity.Doctor;
import com.healthcare.healthcare_backend.entity.Patient;
import com.healthcare.healthcare_backend.entity.Role;
import com.healthcare.healthcare_backend.entity.User;
import com.healthcare.healthcare_backend.repository.DoctorRepository;
import com.healthcare.healthcare_backend.repository.PatientRepository;
import com.healthcare.healthcare_backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        userService = new UserService(
                userRepository,
                patientRepository,
                doctorRepository
        );
    }


    // TC-US-01: Register patient successfully
    @Test
    void shouldRegisterPatientSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setRole(Role.PATIENT);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setRole(Role.PATIENT);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals(Role.PATIENT, result.getRole());

        verify(patientRepository).save(any(Patient.class));
    }


    // TC-US-02: Register doctor successfully
    @Test
    void shouldRegisterDoctorSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Dr Smith");
        request.setEmail("doctor@example.com");
        request.setPassword("password123");
        request.setPhone("9876543210");
        request.setRole(Role.DOCTOR);

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setRole(Role.DOCTOR);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals(Role.DOCTOR, result.getRole());

        verify(doctorRepository).save(any(Doctor.class));
    }


    // TC-US-03: Create patient profile when missing
    @Test
    void shouldCreatePatientProfileWhenSavingUser() {

        User user = new User();
        user.setRole(Role.PATIENT);

        when(userRepository.save(user))
                .thenReturn(user);

        when(patientRepository.findByUser(user))
                .thenReturn(Optional.empty());

        userService.saveUser(user);

        verify(patientRepository).save(any(Patient.class));
    }


    // TC-US-04: Do not create duplicate patient profile
    @Test
    void shouldNotCreateDuplicatePatientProfile() {

        User user = new User();
        user.setRole(Role.PATIENT);

        Patient patient = new Patient();
        patient.setUser(user);

        when(userRepository.save(user))
                .thenReturn(user);

        when(patientRepository.findByUser(user))
                .thenReturn(Optional.of(patient));

        userService.saveUser(user);

        verify(patientRepository, never())
                .save(any(Patient.class));
    }


    // TC-US-05: Get all users
    @Test
    void shouldGetAllUsersSuccessfully() {

        User user1 = new User();
        User user2 = new User();

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());

        verify(userRepository).findAll();
    }


    // TC-US-06: Login successfully
    @Test
    void shouldLoginSuccessfully() {

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("password123");

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(user);

        User result = userService.login(
                "john@example.com",
                "password123"
        );

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }


    // TC-US-07: User not found during login
    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(
                        "unknown@example.com",
                        "password"
                )
        );

        assertEquals("User not found", exception.getMessage());
    }


    // TC-US-08: Wrong password
    @Test
    void shouldThrowExceptionForInvalidPassword() {

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("correctPassword");

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(user);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(
                        "john@example.com",
                        "wrongPassword"
                )
        );

        assertEquals("Invalid password", exception.getMessage());
    }
}