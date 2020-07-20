CREATE TABLE
    mwl_item
    (
        id INT NOT NULL AUTO_INCREMENT,
        PatientNameAbstraction VARCHAR(128),
        ExamID VARCHAR(128),
        PatientID VARCHAR(128),
        PatientNameChinese VARCHAR(128),
        PatientNameEnglish VARCHAR(128),
        PatientGender VARCHAR(8),
        PatientBirtday VARCHAR(20),
        ExamAccessionID VARCHAR(64),
        PreRegisteDate VARCHAR(20),
        ProcedureStepDescriptions VARCHAR(128),
        StudyInstanceUID VARCHAR(128),
        PreExamScheduleDate VARCHAR(20),
        PatientHomeAddress VARCHAR(128),
        PatientTel VARCHAR(32),
        PreExamScheduleTime VARCHAR(20),
        ProcedureStepID VARCHAR(32),
        ModalityName VARCHAR(32),
        ModalityLocation VARCHAR(8),
        PRIMARY KEY (id)
    )
    ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO mwl_item (id, PatientNameAbstraction, ExamID, PatientID, PatientNameChinese, PatientNameEnglish, PatientGender, PatientBirtday, ExamAccessionID, PreRegisteDate, ProcedureStepDescriptions, StudyInstanceUID, PreExamScheduleDate, PatientHomeAddress, PatientTel, PreExamScheduleTime, ProcedureStepID, ModalityName, ModalityLocation) VALUES (1, 'ZS', '1', '10080081', 'Zhang San', 'Zhang San', 'M', '1980-01-01', '10013018188CT1', '2018-01-01', 'CT Head Scan', '1.2.840.1111122.11234', '2018-03-01', 'BeijingFengtai', '01088888888', '10:30:00', '3008', 'CT1', 'CT');
    