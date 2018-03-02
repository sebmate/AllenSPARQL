-- Run this file on the MIMIC-III PostgreSQL database. It generates a table "MIMIC_I2B2", which you can
-- export from the database and then import into the i2b2 Oracle database.

-- Before you can run this script, you need to execute the following scripts to compute the SAPS scores:

-- https://github.com/MIT-LCP/mimic-code/blob/master/concepts/firstday/gcs-first-day.sql
-- https://github.com/MIT-LCP/mimic-code/blob/master/concepts/firstday/vitals-first-day.sql
-- https://github.com/MIT-LCP/mimic-code/blob/master/concepts/firstday/urine-output-first-day.sql
-- https://github.com/MIT-LCP/mimic-code/blob/master/concepts/durations/ventilation-durations.sql
-- https://github.com/MIT-LCP/mimic-code/blob/master/concepts/firstday/ventilation-first-day.sql
-- https://github.com/MIT-LCP/mimic-code/blob/master/concepts/firstday/labs-first-day.sql
-- https://github.com/MIT-LCP/mimic-code/blob/master/concepts/severityscores/saps.sql

SET search_path TO mimiciii;

DROP TABLE fake_times;

CREATE TABLE fake_times as (
SELECT subject_id, min(admittime) + interval '0 second' as starttime, min(admittime) + interval '1 second' as endtime from admissions group by subject_id
);

DROP TABLE mimic_i2b2;

CREATE TABLE mimic_i2b2 as (
SELECT * FROM
(
(
-- Age intervals:
SELECT p.subject_id as patient, 
       'Age' as concept,
       p.dob + interval '1y' * extract(years FROM age(admittime, dob))::integer AS starttime,
       p.dob + interval '1y' * extract(years FROM age(admittime, dob))::integer + interval '1 year' AS endtime,
       extract(years FROM age(admittime, dob))::integer AS value
FROM patients p
INNER JOIN admissions a
ON p.subject_id = a.subject_id
ORDER BY p.subject_id
)
UNION
(
-- ICU intervals:
SELECT  subject_id AS patient, 'ICU' AS concept, intime AS starttime, outtime AS endtime, null AS value
FROM icustays
)
--UNION
--(
---- Glucose intervals:
--SELECT  subject_id AS patient, 'Glu' AS concept, charttime AS starttime, charttime + interval '1 second' AS endtime, valuenum AS value
--FROM labevents WHERE itemid = 50809
--)
UNION
(
-- % Hemoglobin A1c:
SELECT  subject_id AS patient, 'HbA1c' AS concept, charttime AS starttime, charttime + interval '1 second' AS endtime, valuenum AS value
FROM labevents WHERE itemid = 50852
)
UNION
(
-- % TSH:
SELECT  subject_id AS patient, 'TSH' AS concept, charttime AS starttime, charttime + interval '1 second' AS endtime, valuenum AS value
FROM labevents WHERE itemid = 50993
)
UNION
(
-- Albumin, Urine:
SELECT  subject_id AS patient, 'uAlb' AS concept, charttime AS starttime, charttime + interval '1 second' AS endtime, valuenum AS value
FROM labevents WHERE itemid = 51069
)
UNION
(
-- Sepsis:
SELECT diagnoses_icd.subject_id AS patient, 'Sepsis' AS concept, fake_times.starttime, fake_times.endtime, 1 AS value
FROM diagnoses_icd, fake_times WHERE (diagnoses_icd.icd9_code = '99591' OR diagnoses_icd.icd9_code = '99592') and diagnoses_icd.subject_id = fake_times.subject_id
)
UNION
(
-- Diabetes Type I:
SELECT diagnoses_icd.subject_id AS patient, 'Diabetes Type I' AS concept, fake_times.starttime, fake_times.endtime, 1 AS value
FROM diagnoses_icd, fake_times WHERE diagnoses_icd.icd9_code = '25001' and diagnoses_icd.subject_id = fake_times.subject_id
)
UNION
(
-- Congenital Hypothyroidism:
SELECT diagnoses_icd.subject_id AS patient, 'Congenital Hypothyroidism' AS concept, fake_times.starttime, fake_times.endtime, 1 AS value
FROM diagnoses_icd, fake_times WHERE diagnoses_icd.icd9_code = '243' AND diagnoses_icd.subject_id = fake_times.subject_id
)
UNION
(
-- SAPS:
SELECT saps.subject_id AS patient, 'SAPS' AS concept, icustays.intime AS starttime, icustays.intime + interval '24 hours' AS endtime, saps.saps AS value
FROM saps, icustays WHERE saps.subject_id = icustays.subject_id AND saps.icustay_id = icustays.icustay_id
)
) AS Temp ORDER BY patient);

SELECT COUNT(*) FROM MIMIC_I2B2;

--select * FROM d_labitems where label like 'alb%';
--select * FROM d_labitems where label like 'alb%';
--select * FROM d_icd_diagnoses where short_title like '%betes%';
--select * FROM d_icd_diagnoses where icd9_code like '243';



