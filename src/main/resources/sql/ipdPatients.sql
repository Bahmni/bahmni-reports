set session group_concat_max_len = 20000;
SET @sql = NULL;
SET @patientAttributePivot = NULL;
SELECT
  GROUP_CONCAT(
      CONCAT('GROUP_CONCAT(DISTINCT (IF(patient_attributes.key = \'', name, '\', patient_attributes.value, NULL))) as \'', name, '\''))
into @patientAttributePivot
FROM person_attribute_type where name in (#patientAttributes#);

SET @conceptObsPivot = NULL;
SELECT
  GROUP_CONCAT(
      CONCAT('GROUP_CONCAT(DISTINCT(IF(obs_value.obs_name = \'', name, '\', obs_value.value, NULL))) as \'', name, '\''))
into @conceptObsPivot
FROM concept_name where concept_name.name in (#conceptNames#) and concept_name_type = 'FULLY_SPECIFIED';

SET @sql = CONCAT('SELECT
      visit_attribute.date_created                                               AS "Date of Admission",
      visit_attribute.date_changed                                               AS "Date of Discharge",
      pi.identifier                                                              AS "Patient ID",
      CONCAT(pn.given_name, " ", pn.family_name)                                 AS "Patient Name",
      floor(datediff(#filterColumn#, p.birthdate) / 365)                         AS "Age",',
      @patientAttributePivot,
      ',',
      @conceptObsPivot ,
      ', GROUP_CONCAT(DISTINCT (confirmed_patient_diagnosis_view.name)) AS "Diagnosis",
      pa.*
    FROM
      visit_attribute
    INNER JOIN visit v on v.visit_id = visit_attribute.visit_id
    INNER JOIN patient_identifier pi ON pi.patient_id = v.patient_id
    INNER JOIN person p ON p.person_id = v.patient_id
    INNER JOIN person_name pn ON pn.person_id = v.patient_id
    INNER JOIN person_address pa ON pa.person_id = v.patient_id
    INNER JOIN encounter e ON e.visit_id = v.visit_id
    LEFT JOIN (SELECT
          person_id,
          person_attribute_type.name as "key",
          IFNULL(concept_name.name, person_attribute.value) as "value"
        FROM person_attribute
          INNER JOIN person_attribute_type on person_attribute_type.person_attribute_type_id = person_attribute.person_attribute_type_id
          LEFT JOIN concept_name on person_attribute.value = concept_name.concept_id and person_attribute_type.format like "%Concept" and concept_name.concept_name_type="FULLY_SPECIFIED"
        WHERE
          person_attribute_type.name in (#patientAttributes#)
       ) patient_attributes on patient_attributes.person_id = p.person_id
    LEFT JOIN confirmed_patient_diagnosis_view ON confirmed_patient_diagnosis_view.encounter_id = e.encounter_id AND confirmed_patient_diagnosis_view.person_id = v.patient_id
    LEFT JOIN (Select encounter_id, concept_name.name as obs_name, coalesce(obs.value_numeric, obs.value_boolean, obs.value_datetime, obs.value_text, coded_value.name) as value
      FROM obs
      INNER JOIN concept_name on obs.concept_id = concept_name.concept_id and concept_name_type = "FULLY_SPECIFIED"
      LEFT JOIN concept_name as coded_value on obs.value_coded is not null and obs.value_coded = coded_value.concept_id and coded_value.concept_name_type = "FULLY_SPECIFIED"
      WHERE concept_name.name in (#conceptNames#)
    ) obs_value on obs_value.encounter_id = e.encounter_id
    WHERE visit_attribute.value_reference IN ("Admitted", "Discharged")
      and #filterColumn# BETWEEN "#startDate#" AND "#endDate#"
    GROUP BY visit_attribute.date_created, pi.identifier, pn.given_name, pn.family_name, p.birthdate');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
