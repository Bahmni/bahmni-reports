SET SESSION group_concat_max_len = 20000;
SET @sql = NULL;
SET @patientAttributesSql = '#patientAttributes#';
SET @patientAddressesSql = '#patientAddresses#';
SET @programAttributesSql = '#programAttributes#';
SET @programNamesToFilterSql = '#programNamesToFilterSql#';
SET @showAllStates = '#showAllStates#';

SET @patientAttributeJoinSql = ' LEFT OUTER JOIN person_attribute pa ON p.person_id = pa.person_id AND pa.voided is false
  LEFT OUTER JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id AND pat.retired is false
  LEFT OUTER JOIN concept_name scn ON pat.format = "org.openmrs.Concept" AND pa.value = scn.concept_id AND scn.concept_name_type = "SHORT" AND scn.voided is false
  LEFT OUTER JOIN concept_name fscn ON pat.format = "org.openmrs.Concept" AND pa.value = fscn.concept_id AND fscn.concept_name_type = "FULLY_SPECIFIED" AND fscn.voided is false ';
SET @programAttributesJoinSql = '  LEFT OUTER JOIN patient_program_attribute ppa ON ppa.patient_program_id = pprog.patient_program_id AND ppa.voided is false
  LEFT OUTER JOIN program_attribute_type prat ON ppa.attribute_type_id = prat.program_attribute_type_id AND prat.retired is false
  LEFT OUTER JOIN concept_name pratsn ON prat.datatype like "%Concept%" AND ppa.value_reference = pratsn.concept_id AND pratsn.concept_name_type = "SHORT" AND pratsn.voided is false
  LEFT OUTER JOIN concept_name pratfn ON prat.datatype like "%Concept%" AND ppa.value_reference = pratfn.concept_id AND pratfn.concept_name_type = "FULLY_SPECIFIED" AND pratfn.voided is false ';
SET @patientAddressJoinSql = ' LEFT OUTER JOIN person_address paddress ON p.person_id = paddress.person_id AND paddress.voided is false ';
SET @selectAllStatesSql = 'ps.start_date AS "Start Date", ps.end_date AS "End Date"';


SET @sql = CONCAT('SELECT pi.identifier AS "Patient Identifier",
       CONCAT(pn.given_name, " ", pn.family_name)  AS "Patient Name",
       FLOOR(DATEDIFF(DATE(CURDATE()), p.birthdate) / 365)      AS "Age",
       p.birthdate     AS "Birthdate",
       p.gender AS "Gender",
       ', IF(@patientAttributesSql = '', '', CONCAT(@patientAttributesSql, ',')), '
       ', IF(@patientAddressesSql = '', '', CONCAT(@patientAddressesSql, '')), '
       ', IF(@programAttributesSql = '', '', CONCAT(@programAttributesSql, ',')), '
       prog.name AS "Program Name",
       pprog.date_enrolled AS "Enrolled Date",
       pprog.date_completed AS "Completed Date",
       coalesce(stsname.name, stfname.name) AS "Current State",
       p.person_id AS "Patient Id",
       prog.program_id AS "Program Id",
       ', IF(@showAllStates = 'true', CONCAT(@selectAllStatesSql, ','), ''), '
       coalesce(NULLIF(osname.name,''''), ofname.name) AS "Outcome"

FROM patient_program pprog
  JOIN program prog on prog.program_id = pprog.program_id AND pprog.voided is FALSE
  JOIN person p on p.person_id = pprog.patient_id AND p.voided is  FALSE
  JOIN person_name pn on pn.person_id = p.person_id AND pn.voided is FALSE
  JOIN patient_identifier pi on pi.patient_id = p.person_id AND pi.voided is FALSE
  ', IF(@programNamesToFilterSql = '', '',
        'AND prog.name in (#programNamesToFilterSql#)'), '
  LEFT JOIN patient_state ps on ps.patient_program_id = pprog.patient_program_id AND ps.voided is FALSE
  ', IF(@showAllStates = 'true', '', 'AND end_date is NULL'), '
  JOIN program_workflow_state pws on pws.program_workflow_state_id = ps.state AND pws.retired is FALSE
  JOIN concept_name stfname on stfname.concept_id = pws.concept_id AND stfname.concept_name_type = "FULLY_SPECIFIED" AND stfname.voided is FALSE
  LEFT JOIN concept_name stsname on stsname.concept_id = pws.concept_id AND stsname.concept_name_type = "SHORT" AND stsname.voided is FALSE
  LEFT JOIN concept_name ofname on ofname.concept_id = pprog.outcome_concept_id AND ofname.concept_name_type = "FULLY_SPECIFIED" AND ofname.voided is FALSE
  LEFT JOIN concept_name osname on osname.concept_id = pprog.outcome_concept_id AND osname.concept_name_type = "SHORT" AND ofname.voided is FALSE
  ', IF(@patientAttributesSql = '', '', @patientAttributeJoinSql), '
  ', IF(@patientAddressesSql = '', '', @patientAddressJoinSql), '
  ', IF(@programAttributesJoinSql = '', '', @programAttributesJoinSql), '
  WHERE pprog.voided is false AND cast(pprog.date_enrolled AS DATE) <= "#endDate#"  AND (cast(pprog.date_completed AS DATE) >= "#startDate#" OR  pprog.date_completed is NULL)
  GROUP BY pprog.patient_program_id, ps.state
;');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
