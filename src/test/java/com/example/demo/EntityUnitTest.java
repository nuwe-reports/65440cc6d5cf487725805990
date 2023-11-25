package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import com.example.demo.entities.*;

import javax.persistence.PersistenceException;

@DataJpaTest
@AutoConfigureTestDatabase(replace=Replace.NONE)
@TestInstance(Lifecycle.PER_CLASS)
class EntityUnitTest {

	@Autowired
	private TestEntityManager entityManager;

	private Doctor d1;

	private Patient p1;

    private Room r1;

    private Appointment a1;
    private Appointment a2;
    private Appointment a3;

    /** TODO
     * Implement tests for each Entity class: Doctor, Patient, Room and Appointment.
     * Make sure you are as exhaustive as possible. Coverage is checked ;)
     */


    @BeforeEach
    void setUp() {
        d1 = new Doctor("Juan", "Carlos", 34, "j.carlos@hospital.accwe");
        p1 = new Patient("Cornelio","Andrea", 59, "c.andrea@hospital.accwe");
        r1 = new Room("Dermatology");
        a1 = new Appointment(p1, d1, r1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        a2 = new Appointment(p1, d1, r1, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1));
        a3 = new Appointment(p1, d1, r1, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(1));
    }

    // DOCTOR TESTS
    @Test
    void should_persist_and_retrieve_doctor_correctly() {
        // Persistir el doctor
        entityManager.persist(d1);
        entityManager.flush();

        // Recuperar el doctor
        Doctor doctorRecuperado = entityManager.find(Doctor.class, d1.getId());

        // Afirmaciones
        assertThat(doctorRecuperado).isNotNull();
        assertThat(doctorRecuperado.getFirstName()).isEqualTo("Juan");
        assertThat(doctorRecuperado.getLastName()).isEqualTo("Carlos");
        assertThat(doctorRecuperado.getAge()).isEqualTo(34);
        assertThat(doctorRecuperado.getEmail()).isEqualTo("j.carlos@hospital.accwe");
    }


    @Test
    void should_update_doctor_correctly() {
        // Persistir el doctor
        entityManager.persist(d1);
        entityManager.flush();

        // Actualizar los detalles del doctor
        d1.setFirstName("James");
        d1.setLastName("Smith");
        entityManager.flush();

        // Recuperar el doctor actualizado
        Doctor doctorActualizado = entityManager.find(Doctor.class, d1.getId());

        // Afirmaciones
        assertThat(doctorActualizado).isNotNull();
        assertThat(doctorActualizado.getFirstName()).isEqualTo("James");
        assertThat(doctorActualizado.getLastName()).isEqualTo("Smith");
    }

    @Test
    void should_delete_doctor_correctly() {
        // Persistir el doctor
        entityManager.persist(d1);
        entityManager.flush();

        // Eliminar el doctor
        entityManager.remove(d1);
        entityManager.flush();

        // Recuperar el doctor eliminado
        Doctor deletedDoctor = entityManager.find(Doctor.class, d1.getId());

        // Afirmaciones
        assertThat(deletedDoctor).isNull();
    }

    @Test
    void should_throw_exception_for_long_firstname() {
        String longFirstName = repeatString("Juan", 70); // This will exceed varchar(255)
        Doctor doctorWithLongFirstName = new Doctor(longFirstName, "Carlos", 30, "j.carlos@hospital.accwe");

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(doctorWithLongFirstName);
            entityManager.flush();
        });
    }

    @Test
    void should_throw_exception_for_long_lastname() {
        String longLastName = repeatString("Carlos", 100); // This will exceed varchar(255)
        Doctor doctorWithLongLastName = new Doctor("Juan", longLastName, 30, "j.carlos@hospital.accwe");

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(doctorWithLongLastName);
            entityManager.flush();
        });
    }

    @Test
    void should_throw_exception_for_long_email() {
        String longEmail = repeatString("j.carlos@hospital.accwe", 20); // This will exceed varchar(255)
        Doctor doctorWithLongEmail = new Doctor("John", "Doe", 30, longEmail);

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(doctorWithLongEmail);
            entityManager.flush();
        });
    }

    @Test
    void should_persist_appointment_with_doctor() {
        // Persistir el doctor y la cita con ese doctor
        entityManager.persist(d1);
        entityManager.persist(a1);
        entityManager.flush();

        // Recuperar la cita y verificar que el doctor asociado es el correcto
        Appointment retrievedAppointment = entityManager.find(Appointment.class, a1.getId());
        assertEquals(d1.getId(), retrievedAppointment.getDoctor().getId());
    }

    @Test
    void should_update_doctor_in_appointment() {
        // Crear dos doctores
        Doctor doc1 = new Doctor("Juan", "Carlos", 30, "j.carlos@hospital.accwe");
        Doctor doc2 = new Doctor("Clarisa","Julia", 29, "c.julia@hospital.accwe");
        entityManager.persist(doc1);
        entityManager.persist(doc2);

        // Crear una cita con el primer doctor
        Appointment appointment = new Appointment(null, doc1, null, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        entityManager.persist(appointment);
        entityManager.flush();

        // Cambiar el doctor de la cita y actualizar
        appointment.setDoctor(doc2);
        entityManager.persist(appointment);
        entityManager.flush();

        // Recuperar la cita y verificar que el doctor asociado ha cambiado
        Appointment retrievedAppointment = entityManager.find(Appointment.class, appointment.getId());
        assertEquals(doc2.getId(), retrievedAppointment.getDoctor().getId());
    }

    @Test
    void should_delete_doctor_when_appointment_is_deleted() {
        // Persistir el doctor y la cita con ese doctor
        entityManager.persist(d1);
        entityManager.persist(a1);
        entityManager.flush();

        // Eliminar la cita
        entityManager.remove(a1);
        entityManager.flush();

        // Verificar que el doctor ya no existe
        Doctor retrievedDoctor = entityManager.find(Doctor.class, d1.getId());
        assertNull(retrievedDoctor);
    }

    @Test
    void should_not_delete_associated_appointments_when_doctor_is_deleted() {
        // Persistir el doctor y la cita con ese doctor
        entityManager.persist(d1);
        entityManager.persist(a1);
        entityManager.flush();

        // Intentar eliminar el doctor
        entityManager.remove(d1);
        entityManager.flush();

        // Verificar que las citas asociadas aún existen
        Appointment retrievedAppointment = entityManager.find(Appointment.class, a1.getId());
        assertNotNull(retrievedAppointment);
    }
    //--End Doctor Tests--


    // PATIENT TESTS
    @Test
    void should_persist_and_retrieve_patient_correctly() {
        // Persistir el paciente
        entityManager.persist(p1);
        entityManager.flush();

        // Recuperar el paciente
        Patient pacienteRecuperado = entityManager.find(Patient.class, p1.getId());

        // Afirmaciones
        assertThat(pacienteRecuperado).isNotNull();
        assertThat(pacienteRecuperado.getFirstName()).isEqualTo("Cornelio");
        assertThat(pacienteRecuperado.getLastName()).isEqualTo("Andrea");
        assertThat(pacienteRecuperado.getAge()).isEqualTo(59);
        assertThat(pacienteRecuperado.getEmail()).isEqualTo("c.andrea@hospital.accwe");
    }

    @Test
    void should_update_patient_correctly() {
        // Persistir el paciente
        entityManager.persist(p1);
        entityManager.flush();

        // Actualizar los detalles del paciente
        p1.setFirstName("Carlos");
        p1.setLastName("Martinez");
        entityManager.flush();

        // Recuperar el paciente actualizado
        Patient pacienteActualizado = entityManager.find(Patient.class, p1.getId());

        // Afirmaciones
        assertThat(pacienteActualizado).isNotNull();
        assertThat(pacienteActualizado.getFirstName()).isEqualTo("Carlos");
        assertThat(pacienteActualizado.getLastName()).isEqualTo("Martinez");
    }

    @Test
    void should_delete_patient_correctly() {
        // Persistir el paciente
        entityManager.persist(p1);
        entityManager.flush();

        // Eliminar el paciente
        entityManager.remove(p1);
        entityManager.flush();

        // Intentar recuperar el paciente eliminado
        Patient pacienteEliminado = entityManager.find(Patient.class, p1.getId());

        // Afirmaciones
        assertThat(pacienteEliminado).isNull();
    }

    @Test
    void should_throw_exception_for_long_patient_firstname() {
        String longFirstName = repeatString("Cornelio", 70); // Esto excederá varchar(255)
        Patient patientWithLongFirstName = new Patient(longFirstName, "Andrea", 59, "c.andrea@hospital.accwe");

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(patientWithLongFirstName);
            entityManager.flush();
        });
    }

    @Test
    void should_throw_exception_for_long_patient_lastname() {
        String longLastName = repeatString("Andrea", 100); // Esto excederá varchar(255)
        Patient patientWithLongLastName = new Patient("Cornelio", longLastName, 59, "c.andrea@hospital.accwe");

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(patientWithLongLastName);
            entityManager.flush();
        });
    }

    @Test
    void should_throw_exception_for_long_patient_email() {
        String longEmail = repeatString("c.andrea@hospital.accwe", 20); // Esto excederá varchar(255)
        Patient patientWithLongEmail = new Patient("Cornelio", "Andrea", 59, longEmail);

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(patientWithLongEmail);
            entityManager.flush();
        });
    }

    @Test
    void should_persist_appointment_with_patient() {
        // Persistir el paciente
        entityManager.persist(p1);

        // Crear una cita con ese paciente
        entityManager.persist(a1);
        entityManager.flush();

        // Recuperar la cita y verificar que el paciente asociado es el correcto
        Appointment citaRecuperada = entityManager.find(Appointment.class, a1.getId());
        assertEquals(p1.getId(), citaRecuperada.getPatient().getId());
    }

    @Test
    void should_update_patient_in_appointment() {
        // Crear dos pacientes
        Patient paciente1 = new Patient("Cornelio", "Andrea", 59, "c.andrea@hospital.accwe");
        Patient paciente2 = new Patient("Roberto", "Perez", 45, "r.perez@hospital.accwe");
        entityManager.persist(paciente1);
        entityManager.persist(paciente2);

        // Crear una cita con el primer paciente
        Appointment appointment = new Appointment(paciente1, d1, r1, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        entityManager.persist(appointment);
        entityManager.flush();

        // Cambiar el paciente de la cita y actualizar
        appointment.setPatient(paciente2);
        entityManager.persist(appointment);
        entityManager.flush();

        // Recuperar la cita y verificar que el paciente asociado ha cambiado
        Appointment citaRecuperada = entityManager.find(Appointment.class, appointment.getId());
        assertEquals(paciente2.getId(), citaRecuperada.getPatient().getId());
    }

    @Test
    void should_delete_patient_when_appointment_is_deleted() {
        // Persistir el paciente y la cita con ese paciente
        entityManager.persist(p1);
        entityManager.persist(a1);
        entityManager.flush();

        // Eliminar la cita
        entityManager.remove(a1);
        entityManager.flush();

        // Verificar que el paciente ya no existe
        Patient retrievedPatient = entityManager.find(Patient.class, p1.getId());
        assertNull(retrievedPatient);
    }

    @Test
    void should_not_delete_associated_appointments_when_patient_is_deleted() {
        // Persistir el paciente y la cita con ese paciente
        entityManager.persist(p1);
        entityManager.persist(a1);
        entityManager.flush();

        // Intentar eliminar el paciente
        entityManager.remove(p1);
        entityManager.flush();

        // Verificar que las citas asociadas aún existen
        Appointment retrievedAppointment = entityManager.find(Appointment.class, a1.getId());
        assertNotNull(retrievedAppointment);
    }
    //--End Patient Tests--

    // ROOM TESTS
    @Test
    void should_persist_and_retrieve_room_correctly() {
        // Persistir la sala
        entityManager.persist(r1);
        entityManager.flush();

        // Recuperar la sala
        Room salaRecuperada = entityManager.find(Room.class, r1.getRoomName());

        // Afirmaciones
        assertThat(salaRecuperada).isNotNull();
        assertThat(salaRecuperada.getRoomName()).isEqualTo("Dermatology");
    }

    @Test
    void should_delete_room_correctly() {
        // Persistir la sala
        entityManager.persist(r1);
        entityManager.flush();

        // Eliminar la sala
        entityManager.remove(r1);
        entityManager.flush();

        // Intentar recuperar la sala eliminada
        Room salaEliminada = entityManager.find(Room.class, r1.getRoomName());

        // Afirmaciones
        assertThat(salaEliminada).isNull();
    }

    @Test
    void should_throw_exception_for_long_room_name() {
        String longRoomName = repeatString("Dermatology", 50); // Esto excederá varchar(255)
        Room roomWithLongName = new Room(longRoomName);

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(roomWithLongName);
            entityManager.flush();
        });
    }

    @Test
    void should_persist_appointment_with_room() {
        // Persistir la sala y la cita en esa sala
        entityManager.persist(r1);
        entityManager.persist(a1);
        entityManager.flush();

        // Recuperar la cita y verificar que la sala asociada es la correcta
        Appointment citaRecuperada = entityManager.find(Appointment.class, a1.getId());
        assertEquals(r1.getRoomName(), citaRecuperada.getRoom().getRoomName());
    }

    @Test
    void should_update_room_in_appointment() {
        // Crear dos salas
        Room sala1 = new Room("Dermatology");
        Room sala2 = new Room("Cardiology");
        entityManager.persist(sala1);
        entityManager.persist(sala2);

        // Crear una cita con la primera sala
        Appointment appointment = new Appointment(p1, d1, sala1, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        entityManager.persist(appointment);
        entityManager.flush();

        // Cambiar la sala de la cita y actualizar
        appointment.setRoom(sala2);
        entityManager.persist(appointment);
        entityManager.flush();

        // Recuperar la cita y verificar que la sala asociada ha cambiado
        Appointment citaRecuperada = entityManager.find(Appointment.class, appointment.getId());
        assertEquals(sala2.getRoomName(), citaRecuperada.getRoom().getRoomName());
    }

    @Test
    void should_not_delete_associated_appointments_when_room_is_deleted() {
        // Persistir la sala y las citas asociadas
        entityManager.persist(r1);
        entityManager.persist(a1);
        entityManager.persist(a2);
        entityManager.persist(a3);
        entityManager.flush();

        // Eliminar la sala
        entityManager.remove(r1);
        entityManager.flush();

        // Verificar que las citas asociadas aún existen
        Appointment cita1Recuperada = entityManager.find(Appointment.class, a1.getId());
        Appointment cita2Recuperada = entityManager.find(Appointment.class, a2.getId());
        Appointment cita3Recuperada = entityManager.find(Appointment.class, a3.getId());

        assertThat(cita1Recuperada).isNotNull();
        assertThat(cita2Recuperada).isNotNull();
        assertThat(cita3Recuperada).isNotNull();
    }

    @Test
    void should_delete_room_when_appointment_is_deleted() {
        // Persistir la sala y la cita en esa sala
        entityManager.persist(r1);
        entityManager.persist(a1);
        entityManager.flush();

        // Eliminar la cita
        entityManager.remove(a1);
        entityManager.flush();

        // Verificar que la sala ya no existe
        Room retrievedRoom = entityManager.find(Room.class, r1.getRoomName());
        assertNull(retrievedRoom);
    }

    // APPOINTMENT TESTS
    @Test
    void should_persist_and_retrieve_appointment_correctly() {
        // Persistir la cita
        entityManager.persist(a1);
        entityManager.flush();

        // Recuperar la cita
        Appointment retrievedAppointment = entityManager.find(Appointment.class, a1.getId());

        // Afirmaciones
        assertThat(retrievedAppointment).isNotNull();
        assertThat(retrievedAppointment.getPatient().getId()).isEqualTo(p1.getId());
        assertThat(retrievedAppointment.getDoctor().getId()).isEqualTo(d1.getId());
        assertThat(retrievedAppointment.getRoom().getRoomName()).isEqualTo(r1.getRoomName());
        assertThat(retrievedAppointment.getStartsAt()).isEqualTo(a1.getStartsAt());
        assertThat(retrievedAppointment.getFinishesAt()).isEqualTo(a1.getFinishesAt());
    }

    @Test
    void should_update_appointment_correctly() {
        // Persistir la cita
        entityManager.persist(a1);
        entityManager.flush();

        // Actualizar los detalles de la cita
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime updatedStart = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond());
        LocalDateTime updatedEnd = updatedStart.plusDays(5).plusHours(2);

        a1.setStartsAt(updatedStart);
        a1.setFinishesAt(updatedEnd);
        entityManager.flush();

        // Recuperar la cita actualizada
        Appointment updatedAppointment = entityManager.find(Appointment.class, a1.getId());

        // Afirmaciones
        assertThat(updatedAppointment.getStartsAt()).isEqualTo(updatedStart);
        assertThat(updatedAppointment.getFinishesAt()).isEqualTo(updatedEnd);
    }

    @Test
    void should_delete_appointment_correctly() {
        // Persistir la cita
        entityManager.persist(a1);
        entityManager.flush();

        // Eliminar la cita
        entityManager.remove(a1);
        entityManager.flush();

        // Recuperar la cita eliminada
        Appointment deletedAppointment = entityManager.find(Appointment.class, a1.getId());

        // Afirmaciones
        assertThat(deletedAppointment).isNull();
    }

    @Test
    void should_overlap_when_appointments_start_at_same_time() {
        Appointment a1 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        Appointment a2 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 10, 30));
        assertTrue(a1.overlaps(a2));
    }

    @Test
    void should_overlap_when_appointments_end_at_same_time() {
        Appointment a1 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 9, 30),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        Appointment a2 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        assertTrue(a1.overlaps(a2));
    }

    @Test
    void should_overlap_when_one_appointment_starts_before_and_ends_during_another() {
        Appointment a1 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 9, 0),
                LocalDateTime.of(2022, 5, 10, 10, 30));
        Appointment a2 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        assertTrue(a1.overlaps(a2));
    }

    @Test
    void should_overlap_when_one_appointment_starts_during_and_ends_after_another() {
        Appointment a1 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 15),
                LocalDateTime.of(2022, 5, 10, 11, 30));
        Appointment a2 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        assertTrue(a1.overlaps(a2));
    }

    @Test
    void should_not_overlap_when_appointments_are_in_different_rooms() {
        Room r2 = new Room("Cardiology");
        Appointment a1 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        Appointment a2 = new Appointment(p1, d1, r2,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        assertFalse(a1.overlaps(a2));
    }

    @Test
    void should_not_overlap_when_appointments_do_not_overlap_at_all() {
        Appointment a1 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 8, 0),
                LocalDateTime.of(2022, 5, 10, 9, 0));
        Appointment a2 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        assertFalse(a1.overlaps(a2));
    }

    @Test
    void should_not_overlap_when_appointment_finishes_before_this_starts() {
        Appointment a1 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 8, 0),
                LocalDateTime.of(2022, 5, 10, 9, 0));
        Appointment a2 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 6, 0),
                LocalDateTime.of(2022, 5, 10, 7, 0));
        assertFalse(a1.overlaps(a2));
    }

    @Test
    void should_not_overlap_when_appointment_starts_after_this_finishes() {
        Appointment a1 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 8, 0),
                LocalDateTime.of(2022, 5, 10, 9, 0));
        Appointment a2 = new Appointment(p1, d1, r1,
                LocalDateTime.of(2022, 5, 10, 10, 0),
                LocalDateTime.of(2022, 5, 10, 11, 0));
        assertFalse(a1.overlaps(a2));
    }

    public static String repeatString(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

}
