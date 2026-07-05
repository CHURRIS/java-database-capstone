# User Stories - Clinic Management System

## Lógica de negocio del proyecto

El sistema de clínica debe permitir gestionar de forma organizada las operaciones diarias entre pacientes, médicos y administradores. La lógica del negocio se centra en tres procesos principales:

1. Registro y administración de usuarios
   - Un paciente puede registrarse, actualizar su información personal y solicitar citas.
   - Un doctor puede registrarse, actualizar su especialidad y gestionar su disponibilidad.
   - Un administrador puede crear, activar o desactivar cuentas y supervisar el funcionamiento del sistema.

2. Gestión de citas médicas
   - Un paciente puede solicitar una cita con un doctor disponible.
   - El sistema debe evitar la sobreposición de horarios para el mismo médico.
   - El estado de la cita puede cambiar entre pendiente, confirmada, completada o cancelada.

3. Gestión de tratamientos y seguimiento
   - Después de una consulta, el doctor puede generar una prescripción asociada a la cita.
   - El paciente puede ver su historial de citas y recetas.
   - El administrador puede monitorear los registros para asegurar el correcto funcionamiento del negocio.

## Reglas de negocio importantes

- Cada paciente debe tener un registro único con correo y teléfono válidos.
- Cada doctor debe tener un identificador único y una especialidad registrada.
- No se debe permitir que un doctor tenga dos citas simultáneas en la misma fecha y hora.
- Una prescripción debe estar vinculada a una cita específica y a un paciente y doctor determinados.
- La información clínica debe conservarse aunque un paciente o doctor sea desactivado, para no perder el historial médico.

## Historia de usuario - Doctor

**Title:**
Como doctor, quiero gestionar mis citas y crear prescripciones para mis pacientes, para brindar un seguimiento clínico ordenado y eficiente.

**Acceptance Criteria:**
1. El doctor debe poder ver todas sus citas programadas para una fecha determinada.
2. El sistema debe impedir que registre dos citas simultáneas para el mismo horario.
3. El doctor debe poder registrar una prescripción asociada a una cita específica.
4. El doctor debe poder consultar la información básica del paciente antes de atender la cita.

**Priority:** High
**Story Points:** 8
**Notes:**
- La información de la cita debe almacenarse en la tabla appointments.
- La prescripción debe guardarse en la tabla prescriptions y estar relacionada con el doctor, el paciente y la cita.

## Historia de usuario - Paciente

**Title:**
Como paciente, quiero solicitar citas médicas y ver mi historial de consultas y recetas, para recibir atención oportuna y estar informado de mi tratamiento.

**Acceptance Criteria:**
1. El paciente debe poder registrarse con sus datos básicos y acceder al sistema.
2. El paciente debe poder solicitar una cita seleccionando un doctor y una fecha disponible.
3. El paciente debe poder ver el estado de sus citas: pendiente, confirmada, completada o cancelada.
4. El paciente debe poder consultar las prescripciones emitidas en sus citas anteriores.

**Priority:** High
**Story Points:** 5
**Notes:**
- La solicitud de cita debe quedar registrada en appointments con el patient_id y doctor_id correspondientes.
- Si una cita es cancelada, su estado debe actualizarse sin eliminar el historial.

## Historia de usuario - Administrador

**Title:**
Como administrador, quiero gestionar usuarios, citas y registros del sistema, para asegurar el correcto funcionamiento de la clínica y la integridad de los datos.

**Acceptance Criteria:**
1. El administrador debe poder registrar, activar o desactivar a doctores y pacientes.
2. El administrador debe poder visualizar todas las citas programadas y su estado actual.
3. El administrador debe poder revisar registros de prescripciones y mantener la coherencia de la información clínica.
4. El sistema debe proteger el acceso administrativo mediante credenciales válidas.

**Priority:** High
**Story Points:** 8
**Notes:**
- El administrador debe interactuar con la tabla admin y con los datos principales de pacientes, doctores y citas.
- El acceso debe limitarse según el rol para evitar modificaciones no autorizadas.
