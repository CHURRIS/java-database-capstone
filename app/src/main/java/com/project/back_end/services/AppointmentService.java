package com.project.back_end.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public String updateAppointment(Long appointmentId, Long patientId, Doctor doctor, LocalDateTime appointmentTime) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            return "Appointment not found";
        }

        Appointment appointment = optionalAppointment.get();
        if (!appointment.getPatient().getId().equals(patientId)) {
            return "Patient does not match the appointment owner";
        }

        if (doctor != null) {
            appointment.setDoctor(doctor);
        }
        appointment.setAppointmentTime(appointmentTime);
        appointmentRepository.save(appointment);
        return "Appointment updated successfully";
    }

    @Transactional
    public String cancelAppointment(Long appointmentId, Long patientId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            return "Appointment not found";
        }

        Appointment appointment = optionalAppointment.get();
        if (!appointment.getPatient().getId().equals(patientId)) {
            return "Patient does not match the appointment owner";
        }

        appointmentRepository.delete(appointment);
        return "Appointment cancelled successfully";
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointments(Long doctorId, LocalDateTime start, LocalDateTime end, String patientName) {
        if (patientName != null && !patientName.isBlank()) {
            return appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorId, patientName, start, end);
        }
        return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
    }

    @Transactional
    public String changeStatus(Long appointmentId, Integer status) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            return "Appointment not found";
        }

        Appointment appointment = optionalAppointment.get();
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
        return "Appointment status updated";
    }
}
