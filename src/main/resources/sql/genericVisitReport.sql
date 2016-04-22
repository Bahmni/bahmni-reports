set session group_concat_max_len = 20000;
SET @sql = NULL;
SET @patientAttributesSql = '#patientAttributes#';
SET @patientAddressesSql = '#patientAddresses#';
SET @visitAttributesSql = '#visitAttributes#';
SET @visitTypesToFilterSql = '#visitTypesToFilter#';
SET @locationTagsToFilterSql = '#locationTagsToFilter#';
SET @visitAttributeJoinSql = ' LEFT OUTER JOIN visit_attribute va ON va.visit_id=v.visit_id AND va.voided is false
  LEFT OUTER JOIN visit_attribute_type vat ON vat.visit_attribute_type_id = va.attribute_type_id AND vat.retired is false';
SET @patientAttributeJoinSql = ' LEFT OUTER JOIN person_attribute pa ON p.person_id = pa.person_id AND pa.voided is false
  LEFT OUTER JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id AND pat.retired is false
  LEFT OUTER JOIN concept_name scn ON pat.format = "org.openmrs.Concept" AND pa.value = scn.concept_id AND scn.concept_name_type = "SHORT" AND scn.voided is false
  LEFT OUTER JOIN concept_name fscn ON pat.format = "org.openmrs.Concept" AND pa.value = fscn.concept_id AND fscn.concept_name_type = "FULLY_SPECIFIED" AND fscn.voided is false ';
SET @patientAddressJoinSql = ' LEFT OUTER JOIN person_address paddress ON p.person_id = paddress.person_id AND paddress.voided is false ';

SET @sql = CONCAT('SELECT
  pi.identifier                                                 AS "Patient Identifier",
  concat(pn.given_name, " ", pn.family_name)                    AS "Patient Name",
  floor(DATEDIFF(DATE(v.date_started), p.birthdate) / 365)      AS "Age",
  p.birthdate                                                   AS "Birthdate",
  p.gender                                                      AS "Gender",
  ',IF(@patientAttributesSql = '', '', CONCAT(@patientAttributesSql, ',')),'
  ',IF(@patientAddressesSql = '', '', CONCAT(@patientAddressesSql, '')),'
  ',IF(@visitAttributesSql = '', '', CONCAT(@visitAttributesSql, ',')),'
  l.name                                                        AS "Location name",
  vt.name                                                       AS "Visit type",
  v.date_started                                                AS "Date started",
  v.date_stopped                                                AS "Date stopped",
  v.visit_id                                                    AS "Visit Id",
  p.person_id                                                   AS "Patient Id"
FROM visit v
  JOIN visit_type vt ON v.visit_type_id = vt.visit_type_id
  JOIN person p ON p.person_id = v.patient_id AND p.voided is false
  JOIN patient_identifier pi ON p.person_id = pi.patient_id AND pi.voided is false
  JOIN person_name pn ON pn.person_id = p.person_id AND pn.voided is false
  LEFT OUTER JOIN location l ON v.location_id = l.location_id AND l.retired is false
  ',IF(@locationTagsToFilterSql = '', '', 'AND l.location_id in (SELECT ltm.location_id from location_tag_map ltm JOIN location_tag lt ON ltm.location_tag_id=lt.location_tag_id AND lt.retired is false AND lt.name in (#locationTagsToFilter#))'),'
  ',IF(@visitAttributesSql = '', '', @visitAttributeJoinSql),'
  ',IF(@patientAttributesSql = '', '', @patientAttributeJoinSql),'
  ',IF(@patientAddressesSql = '', '', @patientAddressJoinSql),'
WHERE v.voided is false
  AND cast(#applyDateRangeFor# AS DATE) BETWEEN \'#startDate#\' AND \'#endDate#\'
  ',IF(@visitTypesToFilterSql = '', '', 'AND vt.name in (#visitTypesToFilter#)'),'
GROUP BY v.visit_id;');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
