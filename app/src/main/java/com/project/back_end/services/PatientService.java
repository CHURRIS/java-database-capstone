package com.project.back_end.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public int createPatient(Patient patient) {
        if (patient == null || patient.getEmail() == null) {
            return 0;
        }
        if (patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) != null) {
            return -1;
        }
        patientRepository.save(patient);
        return 1;
    }

    public Patient getPatientDetails(String token) {
        String email = tokenService.extractEmail(token);
        if (email == null) {
            return null;
        }
        return patientRepository.findByEmail(email);
    }

    public Map<String, Object> validatePatientLogin(String email, String password) {
        var patient = patientRepository.findByEmail(email);
        if (patient == null || !patient.getPassword().equals(password)) {
            return Map.of("message", "Invalid credentials");
        }
        String token = tokenService.generateToken(email, "patient");
        return Map.of("message", "Login successful", "token", token, "patientId", patient.getId());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointment(Long id) {
        return appointmentRepository.findByPatientId(id).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterAppointments(String condition, String doctorName) {
        List<Appointment> appointments = appointmentRepository.findAll();
        boolean filterPast = "past".equalsIgnoreCase(condition);
        boolean filterFuture = "future".equalsIgnoreCase(condition);

        return appointments.stream()
                .filter(appointment -> {
                    if (filterPast) {
                        return appointment.getStatus() != null && appointment.getStatus() == 1;
                    }
                    if (filterFuture) {
                        return appointment.getStatus() != null && appointment.getStatus() == 0;
                    }
                    return true;
                })
                .filter(appointment -> {
                    if (doctorName == null || doctorName.isBlank()) {
                        return true;
                    }
                    return appointment.getDoctor() != null && appointment.getDoctor().getName() != null
                            && appointment.getDoctor().getName().toLowerCase().contains(doctorName.toLowerCase());
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AppointmentDTO mapToDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor() != null ? appointment.getDoctor().getId() : null,
                appointment.getDoctor() != null ? appointment.getDoctor().getName() : null,
                appointment.getPatient() != null ? appointment.getPatient().getId() : null,
                appointment.getPatient() != null ? appointment.getPatient().getName() : null,
                appointment.getPatient() != null ? appointment.getPatient().getEmail() : null,
                appointment.getPatient() != null ? appointment.getPatient().getPhone() : null,
                appointment.getPatient() != null ? appointment.getPatient().getAddress() : null,
                appointment.getAppointmentTime(),
                appointment.getStatus());
    }
}
