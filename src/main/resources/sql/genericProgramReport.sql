SET SESSION group_concat_max_len = 20000;
SET @sql = NULL;
SET @patientAttributesSql = '#patientAttributes#';
SET @patientAddressesSql = '#patientAddresses#';
SET @programAttributesSql = '#programAttributes#';
SET @programNamesToFilterSql = '#programNamesToFilterSql#';
SET @showAllStates = '#showAllStates#';
SET @extraPatientIdentifierTypes = '#extraPatientIdentifierTypes#';
SET @applyAgeGroup = '#ageGroupName#';
SET @sortByColumns = '#sortByColumns#';


SET @patientAttributeJoinSql = ' LEFT OUTER JOIN person_attribute pa ON p.person_id = pa.person_id AND pa.voided is false
  LEFT OUTER JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id AND pat.retired is false
  LEFT OUTER JOIN concept_name scn ON pat.format = "org.openmrs.Concept" AND pa.value = scn.concept_id AND scn.concept_name_type = "SHORT" AND scn.voided is false
  LEFT OUTER JOIN concept_name fscn ON pat.format = "org.openmrs.Concept" AND pa.value = fscn.concept_id AND fscn.concept_name_type = "FULLY_SPECIFIED" AND fscn.voided is false ';
SET @programAttributesJoinSql = '  LEFT OUTER JOIN patient_program_attribute ppa ON ppa.patient_program_id = pprog.patient_program_id AND ppa.voided is false
  LEFT OUTER JOIN program_attribute_type prat ON ppa.attribute_type_id = prat.program_attribute_type_id AND prat.retired is false
  LEFT OUTER JOIN concept_name pratsn ON prat.datatype like "%Concept%" AND ppa.value_reference = pratsn.concept_id AND pratsn.concept_name_type = "SHORT" AND pratsn.voided is false
  LEFT OUTER JOIN concept_name pratfn ON prat.datatype like "%Concept%" AND ppa.value_reference = pratfn.concept_id AND pratfn.concept_name_type = "FULLY_SPECIFIED" AND pratfn.voided is false ';
SET @patientAddressJoinSql = ' LEFT OUTER JOIN person_address paddress ON p.person_id = paddress.person_id AND paddress.voided is false ';
SET @selectAllStatesSql = 'DATE_FORMAT(ps.start_date, "%d-%b-%Y") AS "Start Date", DATE_FORMAT(ps.end_date, "%d-%b-%Y") AS "End Date"';
SET @ageGroupJoinSql = 'LEFT JOIN reporting_age_group rag ON DATE(pprog.date_enrolled) BETWEEN (DATE_ADD(
                     DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY)) AND (DATE_ADD(
                     DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
                                                       AND rag.report_group_name = "#ageGroupName#"';
SET @ageGroupSelectSql = 'rag.name AS "#ageGroupName#", rag.sort_order AS "Age Group Order"';

SET @primaryIdentifierTypeUuid = NULL;
SELECT property_value FROM global_property WHERE property = 'bahmni.primaryIdentifierType' into @primaryIdentifierTypeUuid;

SET @primaryIdentifierTypeName = NULL;
SELECT name FROM patient_identifier_type WHERE uuid = @primaryIdentifierTypeUuid INTO @primaryIdentifierTypeName;

SET @sql = CONCAT('SELECT
       GROUP_CONCAT(DISTINCT(IF(pit.name = @primaryIdentifierTypeName, pi.identifier, NULL)))     AS "Patient Identifier",
       ',IF(@extraPatientIdentifierTypes = '', '', CONCAT(@extraPatientIdentifierTypes, ',')),'
       CONCAT(pn.given_name, " ", ifnull(pn.family_name,""))  AS "Patient Name",
       FLOOR(DATEDIFF(DATE(pprog.date_enrolled), p.birthdate) / 365)      AS "Age",
        DATE_FORMAT(p.birthdate, "%d-%b-%Y")                             AS "Birthdate",
       p.gender AS "Gender",
       ', IF(@patientAttributesSql = '', '', CONCAT(@patientAttributesSql, ',')), '
       ', IF(@patientAddressesSql = '', '', CONCAT(@patientAddressesSql, '')), '
       ', IF(@programAttributesSql = '', '', CONCAT(@programAttributesSql, ',')), '
       ',IF(@applyAgeGroup = '', '', CONCAT(@ageGroupSelectSql, ',')),'
       prog.name AS "Program Name",
       DATE_FORMAT(pprog.date_enrolled, "%d-%b-%Y") AS "Enrolled Date",
       DATE_FORMAT(pprog.date_completed, "%d-%b-%Y") AS "Completed Date",
       coalesce(stsname.name, stfname.name) AS "Current State",
       p.person_id AS "Patient Id",
       DATE_FORMAT(p.date_created, "%d-%b-%Y") AS "Patient Created Date",
       prog.program_id AS "Program Id",
       ', IF(@showAllStates = 'true', CONCAT(@selectAllStatesSql, ','), ''), '
       coalesce(NULLIF(osname.name,''''), ofname.name) AS "Outcome"

FROM patient_program pprog
  JOIN program prog on prog.program_id = pprog.program_id AND pprog.voided is FALSE
  JOIN person p on p.person_id = pprog.patient_id AND p.voided is  FALSE
  JOIN person_name pn on pn.person_id = p.person_id AND pn.voided is FALSE
  JOIN patient_identifier pi ON p.person_id = pi.patient_id AND pi.voided is false
  JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pit.retired is false  ', IF(@programNamesToFilterSql = '', '',
        'AND prog.name in (#programNamesToFilterSql#)'), '
  LEFT JOIN patient_state ps on ps.patient_program_id = pprog.patient_program_id AND ps.voided is FALSE
  ', IF(@showAllStates = 'true', '', 'AND end_date is NULL'), '
  ',IF(@applyAgeGroup = '', '', @ageGroupJoinSql),'
  LEFT JOIN program_workflow_state pws on pws.program_workflow_state_id = ps.state AND pws.retired is FALSE
  LEFT JOIN concept_name stfname on stfname.concept_id = pws.concept_id AND stfname.concept_name_type = "FULLY_SPECIFIED" AND stfname.voided is FALSE
  LEFT JOIN concept_name stsname on stsname.concept_id = pws.concept_id AND stsname.concept_name_type = "SHORT" AND stsname.voided is FALSE
  LEFT JOIN concept_name ofname on ofname.concept_id = pprog.outcome_concept_id AND ofname.concept_name_type = "FULLY_SPECIFIED" AND ofname.voided is FALSE
  LEFT JOIN concept_name osname on osname.concept_id = pprog.outcome_concept_id AND osname.concept_name_type = "SHORT" AND ofname.voided is FALSE
  ', IF(@patientAttributesSql = '', '', @patientAttributeJoinSql), '
  ', IF(@patientAddressesSql = '', '', @patientAddressJoinSql), '
  ', IF(@programAttributesJoinSql = '', '', @programAttributesJoinSql), '
  WHERE pprog.voided is false AND cast(pprog.date_enrolled AS DATE) <= "#endDate#"  AND (cast(pprog.date_completed AS DATE) >= "#startDate#" OR  pprog.date_completed is NULL)
  GROUP BY pprog.patient_program_id, ps.state
 ' ,IF(@sortByColumns = '','',@sortByColumns),';');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
