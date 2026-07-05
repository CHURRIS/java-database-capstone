# Schema Design

## MySQL Database Design

The MySQL schema is intended for the structured, core operational data of the clinic. This includes patients, doctors, appointments, administrators, and prescriptions.

### Table: patients

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| patient_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each patient |
| first_name | VARCHAR(100) | NOT NULL | Patient first name |
| last_name | VARCHAR(100) | NOT NULL | Patient last name |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Patient email address |
| phone | VARCHAR(20) | NULL | Patient phone number |
| date_of_birth | DATE | NULL | Date of birth |
| gender | ENUM('M','F','Other') | NULL | Patient gender |
| address | VARCHAR(255) | NULL | Residential address |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation date |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Last update time |

### Table: doctors

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| doctor_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each doctor |
| first_name | VARCHAR(100) | NOT NULL | Doctor first name |
| last_name | VARCHAR(100) | NOT NULL | Doctor last name |
| specialization | VARCHAR(100) | NOT NULL | Doctor specialty |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Doctor email |
| phone | VARCHAR(20) | NULL | Doctor phone number |
| license_number | VARCHAR(50) | UNIQUE, NOT NULL | Professional license number |
| is_active | BOOLEAN | DEFAULT TRUE | Whether the doctor is currently active |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation date |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Last update time |

### Table: appointments

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| appointment_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each appointment |
| patient_id | INT | NOT NULL, FOREIGN KEY -> patients.patient_id | Patient linked to the appointment |
| doctor_id | INT | NOT NULL, FOREIGN KEY -> doctors.doctor_id | Doctor assigned to the appointment |
| appointment_date | DATETIME | NOT NULL | Scheduled appointment date and time |
| duration_minutes | INT | DEFAULT 30 | Appointment length |
| status | ENUM('scheduled','completed','cancelled','no_show') | DEFAULT 'scheduled' | Appointment state |
| notes | TEXT | NULL | Optional doctor or clinic notes |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation time |

```sql
-- Example relationship rules:
-- A patient can have many appointments.
-- A doctor can have many appointments.
-- If a patient is deleted, the appointment history should be preserved, so the appointment should be marked as cancelled instead of being physically removed.
-- Overlapping appointment times for the same doctor should be prevented in application logic or via a trigger.
```

### Table: admin

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| admin_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each admin |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Admin login username |
| password_hash | VARCHAR(255) | NOT NULL | Securely stored hashed password |
| full_name | VARCHAR(100) | NOT NULL | Full name of the admin |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Admin email |
| role | ENUM('super_admin','clinic_admin') | DEFAULT 'clinic_admin' | Admin access level |
| is_active | BOOLEAN | DEFAULT TRUE | Whether the admin account is active |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation date |

### Table: prescriptions

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| prescription_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each prescription |
| appointment_id | INT | NOT NULL, FOREIGN KEY -> appointments.appointment_id | Prescription created from a visit |
| doctor_id | INT | NOT NULL, FOREIGN KEY -> doctors.doctor_id | Doctor who issued it |
| patient_id | INT | NOT NULL, FOREIGN KEY -> patients.patient_id | Patient receiving the prescription |
| medication_name | VARCHAR(255) | NOT NULL | Name of the medication |
| dosage | VARCHAR(100) | NOT NULL | Amount and strength |
| frequency | VARCHAR(100) | NOT NULL | How often it should be taken |
| duration_days | INT | NULL | Number of days the medication should be taken |
| instructions | TEXT | NULL | Additional instructions |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation date |

```sql
-- Design note:
-- A prescription is currently tied to a specific appointment because it is usually created during a consultation.
-- If the system later needs standalone prescriptions, this table can be adjusted to allow creation without an appointment.
```

### Optional table: clinic_locations

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| location_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| name | VARCHAR(100) | NOT NULL | Clinic branch or room name |
| address | VARCHAR(255) | NOT NULL | Physical location |
| phone | VARCHAR(20) | NULL | Contact number |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation date |

### Design considerations

- Email and phone formats should be validated in the application layer before saving to the database.
- Some fields such as email and license numbers should remain unique to avoid duplicates.
- Appointment overlap should be checked by the application or a database trigger to prevent double-booking.
- A soft-delete strategy (for example, marking a patient as inactive) is often better than deleting records permanently, especially for medical history.

## MongoDB Collection Design

MongoDB is well suited for flexible, document-oriented data that does not need strict relational structure. This is useful for prescription metadata, notes, uploaded files, or logs.

### Collection: prescriptions

A MongoDB document can store a prescription together with extra metadata that would be awkward in a traditional SQL table.

```json
{
  "_id": "ObjectId",
  "mysql_prescription_id": 101,
  "appointment_id": 45,
  "patient_id": 12,
  "doctor_id": 8,
  "status": "active",
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "twice daily",
      "duration_days": 7,
      "instructions": "Take with food"
    }
  ],
  "notes": "Patient reported mild nausea after the first dose.",
  "attachments": [
    {
      "file_name": "lab-report.pdf",
      "url": "https://storage.example.com/files/lab-report.pdf",
      "uploaded_at": "2026-07-05T10:30:00Z"
    }
  ],
  "tags": ["antibiotic", "follow-up"],
  "metadata": {
    "source": "doctor-portal",
    "created_by": "doctor_8",
    "priority": "normal"
  },
  "created_at": "2026-07-05T10:30:00Z",
  "updated_at": "2026-07-05T10:35:00Z"
}
```

### Design considerations

- The document stores only the relevant IDs from MySQL rather than duplicating the full patient or doctor object, which keeps it easier to maintain.
- MongoDB is flexible enough to evolve if new fields are later needed, such as `reactions`, `reminder_settings`, or `digital_signature`.
- This collection complements the relational model by holding richer, less structured information that is not essential for core transactional operations.
