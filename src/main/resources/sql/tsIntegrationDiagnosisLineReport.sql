SET @sql = NULL;
SET @patientAttributesFromClause = '#patientAttributesFromClause#';
SET @patientAddressesSql = '#patientAddresses#';
SET @patientAddressJoinSql = ' LEFT OUTER JOIN person_address paddress ON p.person_id = paddress.person_id AND paddress.voided is false ';
SET @patientAttributeJoinSql = ' LEFT JOIN (
      #patientAttributeSql#
    ) personattribute on personattribute.person_id = p.person_id ';

SET @sql = CONCAT('
SELECT
    DISTINCT
    IF(extraIdentifier.identifier IS NULL OR extraIdentifier.identifier = "",
       primaryIdentifier.identifier, extraIdentifier.identifier)                        AS "Patient Id",
    concat(pn.given_name, " ", ifnull(pn.family_name, ""))                              AS "Patient Name",
    p.gender 										                                    AS "Gender", ',
    IF(@patientAttributesFromClause = '', '', CONCAT(@patientAttributesFromClause, ',')),'
    ',IF(@patientAddressesSql = '', '', CONCAT(@patientAddressesSql, '')),'
    ',
    'p.birthdate                                                                         AS "Date of Birth",
    cn.name                                                                             AS "Diagnosis",
    crt.code                                                                            AS "Terminology Code",
    DATE_FORMAT(CONVERT_TZ(
                        diagnosisObs.obs_datetime,
                        "+00:00", "+5:30"
                    ), "%d-%b-%Y %H:%i")                                                AS "Date & Time of Diagnosis"
FROM patient pt
         JOIN person p ON p.person_id = pt.patient_id AND p.voided is FALSE
         JOIN person_name pn ON pn.person_id = p.person_id AND pn.voided is FALSE
         JOIN (SELECT pri.patient_id, pri.identifier
               FROM patient_identifier pri
                        JOIN patient_identifier_type pit ON pri.identifier_type = pit.patient_identifier_type_id AND pit.retired is FALSE
                        JOIN global_property gp ON gp.property="bahmni.primaryIdentifierType" AND INSTR (gp.property_value, pit.uuid)) primaryIdentifier
              ON pt.patient_id = primaryIdentifier.patient_id
         LEFT OUTER JOIN (SELECT ei.patient_id, ei.identifier
                          FROM patient_identifier ei
                                   JOIN patient_identifier_type pit ON ei.identifier_type = pit.patient_identifier_type_id AND pit.retired is FALSE
                                   JOIN global_property gp ON gp.property="bahmni.extratientIdentifierTypes" AND INSTR (gp.property_value, pit.uuid)) extraIdentifier
                         ON pt.patient_id = extraIdentifier.patient_id ','
                  ',IF(@patientAttributesFromClause = '', '', @patientAttributeJoinSql),'
                      ',IF(@patientAddressesSql = '', '', @patientAddressJoinSql),'
         JOIN (SELECT  diagnosis.value_coded, diagnosis.person_id,
                       diagnosis.obs_datetime from obs AS diagnosis
                                                       JOIN concept_view AS cv
                                                            ON cv.concept_id = diagnosis.value_coded AND cv.concept_class_name = "Diagnosis" AND
                                                               cast(CONVERT_TZ(
                                                                       diagnosis.obs_datetime,
                                                                       "+00:00", "+5:30"
                                                                   ) AS DATE) BETWEEN "#startDate#" AND "#endDate#"  AND diagnosis.voided = 0
                                                                AND diagnosis.obs_group_id IN (
                                                                    SELECT DISTINCT certaintyObs.obs_id from (
                                                                                                                 SELECT DISTINCT parent.obs_id
                                                                                                                 FROM obs AS parent
                                                                                                                          JOIN concept_view pcv ON pcv.concept_id = parent.concept_id AND
                                                                                                                                                   pcv.concept_full_name = "Visit Diagnoses"
                                                                                                                          LEFT JOIN obs AS child
                                                                                                                                    ON child.obs_group_id = parent.obs_id
                                                                                                                                        AND child.voided IS FALSE
                                                                                                                          JOIN concept_name AS certainty
                                                                                                                               ON certainty.concept_id = child.value_coded AND
                                                                                                                                  (certainty.name = "Confirmed" || certainty.name = "Presumed") AND
                                                                                                                                  certainty.concept_name_type = "FULLY_SPECIFIED"
                                                                                                                 WHERE parent.voided IS FALSE) AS certaintyObs
                                                                )
               UNION
               SELECT patient_conditions.condition_coded AS value_coded,
                      patient_conditions.patient_id AS person_id,
                      patient_conditions.date_created AS obs_datetime
               FROM conditions patient_conditions
               WHERE patient_conditions.clinical_status = "ACTIVE"
                 AND cast(CONVERT_TZ(
                       patient_conditions.date_created,
                       "+00:00", "+5:30"
                   ) AS DATE) BETWEEN "#startDate#" AND "#endDate#" AND voided = FALSE
) as diagnosisObs on diagnosisObs.person_id = p.person_id
         JOIN concept_set cs on cs.concept_id = diagnosisObs.value_coded
         JOIN concept_name cn on cn.concept_id = cs.concept_id AND diagnosisObs.value_coded
    AND cn.concept_name_type = "#conceptNameDisplayFormat#" AND cn.locale = "en" AND cn.voided = false
         INNER JOIN concept_reference_map crm on crm.concept_id = cn.concept_id
         INNER JOIN concept_reference_term crt on crt.concept_reference_term_id = crm.concept_reference_term_id
    AND crt.concept_source_id = (
        select
            concept_source_id
        from
            concept_reference_source
        where
          hl7_code like "#conceptSourceCode#"
    )
  INNER JOIN #tempTable# tmp on tmp.code = crt.code
ORDER BY diagnosisObs.obs_datetime DESC', ';');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;