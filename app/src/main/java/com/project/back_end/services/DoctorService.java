package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDoctorAvailability(Long doctorId, String date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return null;
        }

        LocalDate appointmentDate;
        try {
            appointmentDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return Map.of("message", "Invalid date format");
        }

        LocalDateTime startOfDay = appointmentDate.atStartOfDay();
        LocalDateTime endOfDay = appointmentDate.atTime(LocalTime.MAX);

        Set<String> bookedTimes = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay)
                .stream()
                .map(a -> a.getAppointmentTime().toLocalTime().toString())
                .collect(Collectors.toSet());

        List<String> availableTimes = doctor.getAvailableTimes().stream()
                .filter(time -> !bookedTimes.contains(time))
                .collect(Collectors.toList());

        return Map.of(
                "doctorId", doctor.getId(),
                "doctorName", doctor.getName(),
                "date", date,
                "availableTimes", availableTimes,
                "bookedTimes", bookedTimes);
    }

    public int saveDoctor(Doctor doctor) {
        if (doctor == null || doctor.getEmail() == null) {
            return 0;
        }
        if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
            return -1;
        }
        doctorRepository.save(doctor);
        return 1;
    }

    public int updateDoctor(Doctor doctor) {
        if (doctor == null || doctor.getId() == null) {
            return 0;
        }
        return doctorRepository.findById(doctor.getId())
                .map(existing -> {
                    existing.setName(doctor.getName());
                    existing.setSpecialty(doctor.getSpecialty());
                    existing.setEmail(doctor.getEmail());
                    existing.setPhone(doctor.getPhone());
                    existing.setPassword(doctor.getPassword());
                    existing.setAvailableTimes(doctor.getAvailableTimes());
                    doctorRepository.save(existing);
                    return 1;
                })
                .orElse(-1);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    public int deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            return -1;
        }
        appointmentRepository.deleteAllByDoctorId(id);
        doctorRepository.deleteById(id);
        return 1;
    }

    public Map<String, Object> validateDoctor(String email, String password) {
        var doctor = doctorRepository.findByEmail(email);
        if (doctor == null || !doctor.getPassword().equals(password)) {
            return Map.of("message", "Invalid credentials");
        }
        String token = tokenService.generateToken(email, "doctor");
        return Map.of("message", "Login successful", "token", token, "doctorId", doctor.getId());
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctors(String name, String specialty, String time) {
        List<Doctor> doctors;
        boolean hasName = name != null && !name.isBlank();
        boolean hasSpecialty = specialty != null && !specialty.isBlank();
        if (hasName && hasSpecialty) {
            doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        } else if (hasName) {
            doctors = doctorRepository.findByNameContainingIgnoreCase(name);
        } else if (hasSpecialty) {
            doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        } else {
            doctors = doctorRepository.findAll();
        }

        if (time != null && !time.isBlank()) {
            String normalized = time.trim().toUpperCase();
            doctors = doctors.stream()
                    .filter(doctor -> isAvailableAtTime(doctor, normalized))
                    .collect(Collectors.toList());
        }

        return doctors;
    }

    private boolean isAvailableAtTime(Doctor doctor, String timePeriod) {
        if (doctor.getAvailableTimes() == null || doctor.getAvailableTimes().isEmpty()) {
            return false;
        }
        return doctor.getAvailableTimes().stream().anyMatch(slot -> {
            if (slot == null || slot.isBlank()) {
                return false;
            }
            String normalizedSlot = slot.trim();
            try {
                int hour = LocalTime.parse(normalizedSlot).getHour();
                if ("AM".equalsIgnoreCase(timePeriod)) {
                    return hour < 12;
                }
                if ("PM".equalsIgnoreCase(timePeriod)) {
                    return hour >= 12;
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }
}
