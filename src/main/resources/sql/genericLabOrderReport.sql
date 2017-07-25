SET SESSION group_concat_max_len = 20000;
SET @sql = NULL;
SET @patientAttributesSql = '#patientAttributes#';
SET @patientAddressesSql = '#patientAddresses#';
SET @visitAttributesSql = '#visitAttributes#';
SET @filterByConceptNames = '#conceptNamesToFilter#';
SET @filterByConceptValues = '#conceptValuesToFilter##numericRangesFilterSql#';
SET @filterByPrograms = '#programsToFilter#';
SET @showProvider = '#showProvider#';
SET @extraPatientIdentifierTypes = '#extraPatientIdentifierTypes#';
SET @applyAgeGroup = '#ageGroupName#';
SET @showReferredOutTests = '#showReferredOutTests#';
SET @sortByColumns = '#sortByColumns#';

SET @visitAttributeJoinSql = ' LEFT JOIN visit_attribute va ON va.visit_id=v.visit_id AND va.voided is false
  LEFT JOIN visit_attribute_type vat ON vat.visit_attribute_type_id = va.attribute_type_id AND vat.retired is false';
SET @patientAttributeJoinSql = ' LEFT JOIN person_attribute pa ON p.person_id = pa.person_id AND pa.voided is false
  LEFT JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id AND pat.retired is false
  LEFT JOIN concept_name scn ON pat.format = "org.openmrs.Concept" AND pa.value = scn.concept_id AND scn.concept_name_type = "SHORT" AND scn.voided is false
  LEFT JOIN concept_name fscn ON pat.format = "org.openmrs.Concept" AND pa.value = fscn.concept_id AND fscn.concept_name_type = "FULLY_SPECIFIED" AND fscn.voided is false ';
SET @patientAddressJoinSql = ' LEFT JOIN person_address paddress ON p.person_id = paddress.person_id AND paddress.voided is false ';
SET @conceptNamesToFilterSql = ' `Test Name` IN (#conceptNamesToFilter#)';
SET @filterByConceptValuesSql = CONCAT(IF(@filterByConceptNames != '', ' AND', ''),
                                       ' (coalesce(`Test Result`, bigTable.value_numeric) IN (#noValueFilter##conceptValuesToFilter#) #numericRangesFilterSql#)');
SET @programsJoinSql = ' JOIN episode_encounter ee ON e.encounter_id = ee.encounter_id
  JOIN episode_patient_program epp ON ee.episode_id=epp.episode_id
  JOIN patient_program pp ON epp.patient_program_id = pp.patient_program_id
  JOIN program program ON pp.program_id = program.program_id';
SET @filterByProgramsSql = '  AND program.name IN (#programsToFilter#)';
SET @providerJoinSql = '  JOIN provider pro ON pro.provider_id=ep.provider_id
  LEFT JOIN person_name provider_person ON provider_person.person_id = pro.person_id';
SET @providerSelectSql = 'coalesce(pro.name, concat(provider_person.given_name, " ", ifnull(provider_person.family_name, ""))) AS "Provider"';
SET @ageGroupJoinSql = 'LEFT JOIN reporting_age_group rag ON DATE(ord.date_activated) BETWEEN (DATE_ADD(
                     DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY)) AND (DATE_ADD(
                     DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
                                                       AND rag.report_group_name = "#ageGroupName#"';
SET @ageGroupSelectSql = 'rag.name AS "#ageGroupName#", rag.sort_order AS "Age Group Order"';

SET @programSelectSql = 'program.name AS "Program Name"';

SET @referredOutFilter = '`Referred Out` = "No"';

SET @labOrderType = NULL;
SELECT order_type_id
FROM order_type
WHERE name = 'Lab Order'
INTO @labOrderType;

SET @abnormalObsConceptId = NULL;
SELECT concept_id
FROM concept_name
WHERE name = 'LAB_ABNORMAL' AND concept_name_type = 'FULLY_SPECIFIED'
INTO @abnormalObsConceptId;

SET @trueConceptId = NULL;
SELECT concept_id
FROM concept_name
WHERE name = 'True' AND concept_name_type = 'FULLY_SPECIFIED'
INTO @trueConceptId;

SET @labMinNormalConceptId = NULL;
SELECT concept_id
FROM concept_name
WHERE name = 'LAB_MINNORMAL' AND concept_name_type = 'FULLY_SPECIFIED'
INTO @labMinNormalConceptId;

SET @labMaxNormalConceptId = NULL;
SELECT concept_id
FROM concept_name
WHERE name = 'LAB_MAXNORMAL' AND concept_name_type = 'FULLY_SPECIFIED'
INTO @labMaxNormalConceptId;

SET @labNotesConceptId = NULL;
SELECT concept_id
FROM concept_name
WHERE name = 'LAB_NOTES' AND concept_name_type = 'FULLY_SPECIFIED'
INTO @labNotesConceptId;

SET @labReportFileConceptId = NULL;
SELECT concept_id
FROM concept_name
WHERE name = 'LAB_REPORT' AND concept_name_type = 'FULLY_SPECIFIED'
INTO @labReportFileConceptId;

SET @orderIdList = NULL;
SELECT GROUP_CONCAT(DISTINCT(ord.order_id)) FROM orders ord WHERE cast(ord.date_activated AS DATE) BETWEEN "#startDate#" AND "#endDate#" and ord.order_type_id = @labOrderType and ord.voided is false INTO @orderIdList;

SET @labReferredOutConceptId = NULL;
SELECT concept_id from concept_name WHERE name='REFERRED_OUT' AND concept_name_type = 'FULLY_SPECIFIED'
INTO @labReferredOutConceptId;

SET @labReferredOutObsGroupIds = NULL;
SELECT group_concat(parent_obs.obs_id)
FROM obs parent_obs
  JOIN (
         SELECT o.obs_group_id
         FROM obs o JOIN orders ord ON o.order_id = ord.order_id AND
           FIND_IN_SET(o.order_id, @orderIdList)
           AND o.concept_id = @labReferredOutConceptId) referred_out_obs
    ON referred_out_obs.obs_group_id = parent_obs.obs_id
  LEFT JOIN obs child_value_obs ON parent_obs.obs_id = child_value_obs.obs_group_id and parent_obs.concept_id = child_value_obs.concept_id
where coalesce(child_value_obs.value_coded, child_value_obs.value_text, child_value_obs.value_numeric) is null or child_value_obs.obs_id is null
INTO @labReferredOutObsGroupIds;

SET @labReferredOutObsIds = NULL;
SELECT group_concat(o.obs_id)
FROM obs o
where concept_id = @labReferredOutConceptId and FIND_IN_SET(o.order_id, @orderIdList)
INTO @labReferredOutObsIds;

SET @labReportFileObsGroupIds = NULL;
SELECT group_concat(parent_obs.obs_id)
FROM obs parent_obs
  JOIN (
         SELECT o.obs_group_id
         FROM obs o
           JOIN orders ord ON o.order_id = ord.order_id AND FIND_IN_SET(o.order_id, @orderIdList)
                              AND o.concept_id = @labReportFileConceptId) upload_file_obs
    ON upload_file_obs.obs_group_id = parent_obs.obs_id
  LEFT JOIN obs child_value_obs ON parent_obs.obs_id = child_value_obs.obs_group_id and parent_obs.concept_id = child_value_obs.concept_id
where coalesce(child_value_obs.value_coded, child_value_obs.value_text, child_value_obs.value_numeric) is null or child_value_obs.obs_id is null
INTO @labReportFileObsGroupIds;

SET @primaryIdentifierTypeUuid = NULL;
SELECT property_value FROM global_property WHERE property = 'bahmni.primaryIdentifierType' into @primaryIdentifierTypeUuid;

SET @primaryIdentifierTypeName = NULL;
SELECT name FROM patient_identifier_type WHERE uuid = @primaryIdentifierTypeUuid INTO @primaryIdentifierTypeName;

SET @sql = CONCAT('SELECT * FROM (SELECT
  GROUP_CONCAT(DISTINCT(IF(pit.name = @primaryIdentifierTypeName, pi.identifier, NULL)))     AS "Patient Identifier",
  ',IF(@extraPatientIdentifierTypes = '', '', CONCAT(@extraPatientIdentifierTypes, ',')),'
  concat(pn.given_name, " ", ifnull(pn.family_name, ""))                    AS "Patient Name",
  floor(DATEDIFF(DATE(ord.date_activated), p.birthdate) / 365)      AS "Age",
  DATE_FORMAT(p.birthdate, "%d-%b-%Y")                                                   AS "Birthdate",
  p.gender                                                      AS "Gender",
  ', IF(@patientAttributesSql = '', '', CONCAT(@patientAttributesSql, ',')), '
  ', IF(@patientAddressesSql = '', '', CONCAT(@patientAddressesSql, '')), '
  ', IF(@visitAttributesSql = '', '', CONCAT(@visitAttributesSql, ',')), '
  ', IF(@filterByPrograms = '', '', CONCAT(@programSelectSql, ',')), '
  ',IF(@applyAgeGroup = '', '', CONCAT(@ageGroupSelectSql, ',')),'
  DATE_FORMAT(date(ord.date_activated), "%d-%b-%Y")                                        AS "Test Order Date",
  ord.date_activated            AS "Order DateTime",
  coalesce(test_scn.name, test_fscn.name) AS "Test Name",
  IF (o.`Test Result` is not NULL, o.`Test Result`, CONCAT(coded_fscn.name, "(", IF (coded_scn.name is NULL, "", coded_scn.name), ")")) AS "Test Result",
  IF (o.abnormal_coded = @trueConceptId, "Abnormal", IF (coalesce(o.`Test Result`, coded_scn.name, coded_fscn.name) is not null, "Normal", "")) as "Test Outcome",'
, IF(@showProvider = '', '', CONCAT(@providerSelectSql, ',')), '
  DATE_FORMAT(v.date_started, "%d-%b-%Y")                                                AS "Visit Start Date",
  DATE_FORMAT(v.date_stopped, "%d-%b-%Y")                                                AS "Visit Stop Date",
  vt.name                                                       AS "Visit Type",
  o.obs_id                                     AS "Obs Id",
  coalesce(o.concept_id, cs.concept_id, ord.concept_id) AS "Concept Id",
  p.person_id                                                   AS "Patient Id",
  ord.order_id                                                    AS "Order Id",
  o.`Min Range` AS "Min Range",
  o.`Max Range` AS "Max Range",
  coalesce(o.referred_out,"No") AS "Referred Out",
  coalesce(o.file_uploaded,"No") AS "File Uploaded",
  o.value_numeric AS "value_numeric"

FROM (SELECT * FROM orders ord WHERE cast(ord.date_activated AS DATE) BETWEEN "#startDate#" AND "#endDate#" and ord.order_type_id = @labOrderType and ord.voided is false and ord.date_stopped is NULL) ord
  JOIN person p ON p.person_id = ord.patient_id AND p.voided is false
  JOIN patient_identifier pi ON p.person_id = pi.patient_id AND pi.voided is false
  JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pit.retired is false
  JOIN person_name pn ON pn.person_id = p.person_id AND pn.voided is false
  JOIN encounter e ON ord.encounter_id=e.encounter_id AND e.voided is false
  JOIN encounter_provider ep ON ep.encounter_id=e.encounter_id and ep.voided is false ',
  IF(@showProvider = '', '', @providerJoinSql),
  ' JOIN visit v ON v.visit_id=e.visit_id AND v.voided is false ',
  ' JOIN visit_type vt ON vt.visit_type_id=v.visit_type_id AND vt.retired is false
  ', IF(@filterByPrograms != '', @programsJoinSql, ''), '
  ', IF(@filterByPrograms != '', @filterByProgramsSql, ''),'
  ', IF(@visitAttributesSql = '', '', @visitAttributeJoinSql), '
  ', IF(@patientAttributesSql = '', '', @patientAttributeJoinSql), '
  ', IF(@patientAddressesSql = '', '', @patientAddressJoinSql), '
  ', IF(@applyAgeGroup = '', '', @ageGroupJoinSql),'
  LEFT JOIN concept_set cs on cs.concept_set = ord.concept_id
  LEFT JOIN concept_name test_fscn on coalesce(cs.concept_id, ord.concept_id)=test_fscn.concept_id AND test_fscn.concept_name_type="FULLY_SPECIFIED" AND test_fscn.voided is false
  LEFT JOIN concept_name test_scn on coalesce(cs.concept_id, ord.concept_id)=test_scn.concept_id AND test_scn.concept_name_type="SHORT" AND test_scn.voided is false
  LEFT JOIN (select GROUP_CONCAT(DISTINCT(IF (o.concept_id = @labMinNormalConceptId, o.value_numeric, NULL))) AS "Min Range",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id = @labMaxNormalConceptId, o.value_numeric, NULL))) AS "Max Range",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id = @abnormalObsConceptId,  o.value_coded, NULL))) AS "abnormal_coded",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id = @labReferredOutConceptId or FIND_IN_SET(o.obs_id,@labReferredOutObsGroupIds),  "Yes",NULL))) AS "referred_out",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id = @labReportFileConceptId or FIND_IN_SET(o.obs_id,@labReportFileObsGroupIds),  "Yes", NULL))) AS "file_uploaded",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id not in (@labMinNormalConceptId, @labMaxNormalConceptId, @abnormalObsConceptId, @labNotesConceptId, @labReportFileConceptId), coalesce(o.value_text, o.value_numeric), NULL))) AS "Test Result",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id not in (@labMinNormalConceptId, @labMaxNormalConceptId, @abnormalObsConceptId, @labNotesConceptId, @labReportFileConceptId), o.value_coded , NULL))) AS "value_coded",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id not in (@labMinNormalConceptId, @labMaxNormalConceptId, @abnormalObsConceptId, @labNotesConceptId, @labReportFileConceptId,@labReferredOutConceptId), o.concept_id , NULL))) AS "concept_id",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id not in (@labMinNormalConceptId, @labMaxNormalConceptId, @abnormalObsConceptId, @labNotesConceptId, @labReportFileConceptId), o.obs_id , NULL))) AS "obs_id",
                    GROUP_CONCAT(DISTINCT(IF (o.concept_id not in (@labMinNormalConceptId, @labMaxNormalConceptId, @abnormalObsConceptId, @labNotesConceptId, @labReportFileConceptId), o.value_numeric , NULL))) AS "value_numeric",
                    GROUP_CONCAT(DISTINCT(o.order_id)) AS order_id
            from obs o where o.voided is false AND FIND_IN_SET(o.order_id, @orderIdList)
                      and (coalesce(o.value_text, o.value_numeric, o.value_coded) is not null
                            OR FIND_IN_SET(o.obs_id,@labReportFileObsGroupIds)
                            OR FIND_IN_SET(o.obs_id,@labReferredOutObsGroupIds)
                            OR FIND_IN_SET(o.obs_id,@labReferredOutObsIds))
                      GROUP BY o.obs_group_id) o
            ON o.concept_id = coalesce(cs.concept_id, ord.concept_id) AND o.order_id = ord.order_id
  LEFT JOIN concept_name coded_fscn on coded_fscn.concept_id = o.value_coded AND coded_fscn.concept_name_type="FULLY_SPECIFIED" AND coded_fscn.voided is false
  LEFT JOIN concept_name coded_scn on coded_scn.concept_id = o.value_coded AND coded_scn.concept_name_type="SHORT" AND coded_scn.voided is false
                   GROUP BY ord.order_id, cs.concept_id, o.concept_id ORDER BY ord.date_activated asc, o.obs_id asc) bigTable',
                  IF(@filterByConceptNames != '' OR @filterByConceptValues != '' OR @showReferredOutTests ='false' , ' WHERE', ''),
                  IF(@filterByConceptNames = '', '', @conceptNamesToFilterSql), '
   ', IF(@filterByConceptValues = '', '', @filterByConceptValuesSql),'
   ',IF(@sortByColumns != '', @sortByColumns, ''),'
   ',If(@showReferredOutTests = 'false' ,@referredOutFilter,'')

);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
