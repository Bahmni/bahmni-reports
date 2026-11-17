-- BAH-5299: when the report is filtering by visitStopDate (applyOpenVisitFallback, set in
-- GenericVisitDaoImpl from GenericVisitReportConfig.DateRange), the WHERE clause below also
-- includes a visit whose v.date_stopped is NULL -- i.e. it is still open/ongoing -- as long as
-- it had already started by endDate. Without this, "NULL BETWEEN x AND y" evaluates to NULL
-- (not true) in SQL, so every open visit was silently excluded from same-day reports -- this,
-- not a timezone bug, was the actual cause of "visits created today don't show up today".
-- The fallback is scoped to visitStopDate only (not visitStartDate/dateOfAdmission/
-- dateOfDischarge): date_started is never NULL for a visit that exists at all, so it needs no
-- fallback, and admission/discharge dates come from an optional visit_attribute that is simply
-- never recorded for plenty of ordinary visits (e.g. OPD visits) -- NULL there does not mean
-- "still open" the way it does for date_stopped, and auditing whether dateOfDischarge deserves
-- its own NULL-handling is intentionally left as a follow-up (see BAH-5299 discovery doc).
-- This fix intentionally does NOT add any timezone-conversion logic (no ZoneId/TimeZone/Instant
-- anywhere in this codebase). Report date filters assume a single, consistent deployment
-- timezone shared by the OpenMRS and bahmni-reports containers (see BAH-3017, which wires the
-- same TZ env var into both). A prior attempt to compensate for timezones on the reports side
-- (BAH-2131) was reverted once that container-level TZ config landed -- see PR 73.
  set session group_concat_max_len = 20000;
SET @sql = NULL;
SET @patientAttributesSql = '#patientAttributes#';
SET @patientAddressesSql = '#patientAddresses#';
SET @visitAttributesSql = '#visitAttributes#';
SET @visitTypesToFilterSql = '#visitTypesToFilter#';
SET @extraPatientIdentifierTypes = '#extraPatientIdentifierTypes#';
SET @applyAgeGroup = '#ageGroupName#';
SET @sortByColumns = '#sortByColumns#';
SET @visitAttributeJoinSql = ' LEFT OUTER JOIN visit_attribute va ON va.visit_id=v.visit_id AND va.voided is false
  LEFT OUTER JOIN visit_attribute_type vat ON vat.visit_attribute_type_id = va.attribute_type_id AND vat.retired is false';
SET @patientAttributeJoinSql = ' LEFT OUTER JOIN person_attribute pa ON p.person_id = pa.person_id AND pa.voided is false
  LEFT OUTER JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id AND pat.retired is false
  LEFT OUTER JOIN concept_name scn ON pat.format = "org.openmrs.Concept" AND pa.value = scn.concept_id AND scn.concept_name_type = "SHORT" AND scn.voided is false
  LEFT OUTER JOIN concept_name fscn ON pat.format = "org.openmrs.Concept" AND pa.value = fscn.concept_id AND fscn.concept_name_type = "FULLY_SPECIFIED" AND fscn.voided is false ';
SET @patientAddressJoinSql = ' LEFT OUTER JOIN person_address paddress ON p.person_id = paddress.person_id AND paddress.voided is false ';

SET @ageGroupJoinSql = 'LEFT JOIN reporting_age_group rag ON DATE(v.date_started) BETWEEN (DATE_ADD(
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
  concat(pn.given_name, " ", ifnull(pn.family_name, ""))                    AS "Patient Name",
  floor(DATEDIFF(DATE(v.date_started), p.birthdate) / 365)      AS "Age",
  DATE_FORMAT(p.birthdate, "%d-%b-%Y")         AS "Birthdate",
  p.gender                                                      AS "Gender",
  ',IF(@patientAttributesSql = '', '', CONCAT(@patientAttributesSql, ',')),'
  ',IF(@patientAddressesSql = '', '', CONCAT(@patientAddressesSql, '')),'
  ',IF(@visitAttributesSql = '', '', CONCAT(@visitAttributesSql, ',')),'
  ',IF(@applyAgeGroup = '', '', CONCAT(@ageGroupSelectSql, ',')),'
  vt.name                                                       AS "Visit type",
  DATE_FORMAT(v.date_started, "%d-%b-%Y")                       AS "Date started",
  DATE_FORMAT(v.date_stopped, "%d-%b-%Y")                       AS "Date stopped",
  v.visit_id                                                    AS "Visit Id",
  p.person_id                                                   AS "Patient Id",
  DATE_FORMAT(p.date_created, "%d-%b-%Y")                       AS "Patient Created Date",
  DATE_FORMAT(admission_details.admission_date, "%d-%b-%Y")     AS "Date Of Admission",
  DATE_FORMAT(admission_details.discharge_date, "%d-%b-%Y")     AS "Date Of Discharge",
  IF(DATE(v.date_started) = DATE(p.date_created),"Yes","No")                AS "New patient visit"
FROM visit v
  JOIN visit_type vt ON v.visit_type_id = vt.visit_type_id
  JOIN person p ON p.person_id = v.patient_id AND p.voided is false
  JOIN patient_identifier pi ON p.person_id = pi.patient_id AND pi.voided is false
  JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pit.retired is false
  JOIN person_name pn ON pn.person_id = p.person_id AND pn.voided is false
  ',IF(@applyAgeGroup = '', '', @ageGroupJoinSql),'
  LEFT OUTER JOIN (SELECT
                      va.date_created                                              AS admission_date,
                      if(va.value_reference = "Discharged", va.date_changed, NULL) AS discharge_date,
                      va.visit_id                                                  AS visit_id
                    FROM visit_attribute va
                    JOIN visit_attribute_type vat ON vat.visit_attribute_type_id = va.attribute_type_id
                    AND vat.name="Admission Status") as admission_details ON admission_details.visit_id = v.visit_id
  ',IF(@visitAttributesSql = '', '', @visitAttributeJoinSql),'
  ',IF(@patientAttributesSql = '', '', @patientAttributeJoinSql),'
  ',IF(@patientAddressesSql = '', '', @patientAddressJoinSql),'
WHERE v.voided is false
  -- The conditional two lines down is StringTemplate v4\'s own if/endif tag, not this file\'s
  -- usual SET-a-variable-then-ask-MySQL\'s-IF-to-test-it pattern (see visitTypesToFilterSql a
  -- few lines below). That is deliberate: applyOpenVisitFallback is a plain Java boolean (see
  -- GenericVisitDaoImpl), not a string fragment to splice in, so there is nothing for a
  -- MySQL-side IF to test -- the condition has to be resolved before this SQL text is even
  -- built, which only StringTemplate itself can do.
  AND (
    cast(#applyDateRangeFor# AS DATE) BETWEEN \'#startDate#\' AND \'#endDate#\'
    #if(applyOpenVisitFallback)#OR (#applyDateRangeFor# IS NULL AND cast(v.date_started AS DATE) <= \'#endDate#\')#endif#
  )
  ',IF(@visitTypesToFilterSql = '', '', 'AND vt.name in (#visitTypesToFilter#)'),'
GROUP BY v.visit_id
',IF(@sortByColumns = '', ' ORDER BY v.visit_id',@sortByColumns),';');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
