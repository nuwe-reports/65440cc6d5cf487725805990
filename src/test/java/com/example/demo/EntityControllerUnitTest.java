
package com.example.demo;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;
import java.time.format.*;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.demo.controllers.*;
import com.example.demo.repositories.*;
import com.example.demo.entities.*;
import com.fasterxml.jackson.databind.ObjectMapper;



/** TODO
 * Implement all the unit test in its corresponding class.
 * Make sure to be as exhaustive as possible. Coverage is checked ;)
 */

@WebMvcTest(DoctorController.class)
class DoctorControllerUnitTest{

    @MockBean
    private DoctorRepository doctorRepository;

    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAllDoctors() throws Exception {
        Doctor doc1 = new Doctor("Juan","Carlos", 34, "j.carlos@hospital.accwe");
        Doctor doc2 = new Doctor("Cornelio","Andrea", 59, "c.andrea@hospital.accwe");
        Doctor doc3 = new Doctor("Clarisa","Julia", 29, "c.julia@hospital.accwe");
        List<Doctor> doctors = Arrays.asList(doc1, doc2, doc3);

        when(doctorRepository.findAll()).thenReturn(doctors);

        mockMvc.perform(get("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(3)))
                .andExpect(jsonPath("$[0].firstName", Matchers.is("Juan")))
                .andExpect(jsonPath("$[1].firstName", Matchers.is("Cornelio")))
                .andExpect(jsonPath("$[2].firstName", Matchers.is("Clarisa")))
                .andExpect(status().isOk());

    }

    @Test
    void shouldGetZeroDoctors() throws Exception {
        when(doctorRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetDoctorById() throws Exception {
        Doctor doctor = new Doctor("Juan","Carlos", 34, "j.carlos@hospital.accwe");
        doctor.setId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        mockMvc.perform(get("/api/doctors/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", Matchers.is("Juan")))
                .andExpect(jsonPath("$.lastName", Matchers.is("Carlos")))
                .andExpect(status().isOk());

    }

    @Test
    void shouldNotGetDoctorById() throws Exception {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateDoctor() throws Exception {
        Doctor doctor = new Doctor("Juan","Carlos", 34, "j.carlos@hospital.accwe");

        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        mockMvc.perform(post("/api/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctor)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDeleteDoctorById() throws Exception {
        Doctor doctor = new Doctor("Juan","Carlos", 34, "j.carlos@hospital.accwe");
        doctor.setId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        doNothing().when(doctorRepository).deleteById(1L);

        mockMvc.perform(delete("/api/doctors/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotDeleteDoctorById() throws Exception {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/doctors/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllDoctors() throws Exception {
        doNothing().when(doctorRepository).deleteAll();

        mockMvc.perform(delete("/api/doctors"))
                .andExpect(status().isOk());
    }
}


@WebMvcTest(PatientController.class)
class PatientControllerUnitTest{

    @MockBean
    private PatientRepository patientRepository;

    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldGetAllPatients() throws Exception {
        List<Patient> patients = Arrays.asList(
                new Patient("Juan","Carlos", 34, "j.carlos@hospital.accwe"),
                new Patient("Cornelio","Andrea", 59, "c.andrea@hospital.accwe")
        );

        when(patientRepository.findAll()).thenReturn(patients);

        mockMvc.perform(get("/api/patients"))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", Matchers.is("Juan")))
                .andExpect(jsonPath("$[1].firstName", Matchers.is("Cornelio")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetNoPatients() throws Exception {
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetPatientById() throws Exception {
        Patient patient = new Patient("Juan","Carlos", 34, "j.carlos@hospital.accwe");
        patient.setId(1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(jsonPath("$.firstName", Matchers.is("Juan")))
                .andExpect(jsonPath("$.lastName", Matchers.is("Carlos")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotGetPatientById() throws Exception {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldreatePatient() throws Exception {
        Patient patient = new Patient("Juan","Carlos", 34, "j.carlos@hospital.accwe");

        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        mockMvc.perform(post("/api/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDeletePatientById() throws Exception {
        Patient patient = new Patient("Juan","Carlos", 34, "j.carlos@hospital.accwe");
        patient.setId(1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).deleteById(1L);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotDeletePatientById() throws Exception {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllPatients() throws Exception {
        doNothing().when(patientRepository).deleteAll();

        mockMvc.perform(delete("/api/patients"))
                .andExpect(status().isOk());
    }

}

@WebMvcTest(RoomController.class)
class RoomControllerUnitTest{

    @MockBean
    private RoomRepository roomRepository;

    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAllRooms() throws Exception {
        List<Room> rooms = Arrays.asList(
                new Room("Dermatology"),
                new Room("Cardiology")
        );

        when(roomRepository.findAll()).thenReturn(rooms);

        mockMvc.perform(get("/api/rooms"))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].roomName", Matchers.is("Dermatology")))
                .andExpect(jsonPath("$[1].roomName", Matchers.is("Cardiology")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetNoRooms() throws Exception {
        when(roomRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetRoomByRoomName() throws Exception {
        Room room = new Room("Dermatology");

        when(roomRepository.findByRoomName("Dermatology")).thenReturn(Optional.of(room));

        mockMvc.perform(get("/api/rooms/Dermatology"))
                .andExpect(jsonPath("$.roomName", Matchers.is("Dermatology")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotGetRoomByRoomName() throws Exception {
        when(roomRepository.findByRoomName("Dermatology")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rooms/Dermatology"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateRoom() throws Exception {
        Room room = new Room("Dermatology");

        when(roomRepository.save(any(Room.class))).thenReturn(room);

        mockMvc.perform(post("/api/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDeleteRoomByRoomName() throws Exception {
        Room room = new Room("Dermatology");

        when(roomRepository.findByRoomName("Dermatology")).thenReturn(Optional.of(room));
        doNothing().when(roomRepository).deleteByRoomName("Dermatology");

        mockMvc.perform(delete("/api/rooms/Dermatology"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotDeleteRoomByRoomName() throws Exception {
        when(roomRepository.findByRoomName("Dermatology")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/rooms/Dermatology"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllRooms() throws Exception {
        doNothing().when(roomRepository).deleteAll();

        mockMvc.perform(delete("/api/rooms"))
                .andExpect(status().isOk());
    }
}
