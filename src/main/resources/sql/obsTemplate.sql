set session group_concat_max_len = 20000;
SET @sql = NULL;

SET @patientAttributesSql = '#patientAttributes#';

SET @patientAttributesJoin = 'LEFT JOIN (SELECT
            person_id,
            #patientAttributes#
              FROM
                  person_attribute_type pat
                  LEFT JOIN person_attribute pattr ON pattr.person_attribute_type_id = pat.person_attribute_type_id AND pattr.voided = FALSE AND pat.name IN (#patientAttributesInClauseEscapeQuote#)
                  LEFT JOIN concept_view person_attribute_cn ON pattr.value = person_attribute_cn.concept_id AND pat.format LIKE "%Concept"
              GROUP BY person_id) pattr_result ON pattr_result.person_id = person.person_id';

SET @patientAttributesSelectClause = '#patientAttributesInSelectClause#';


SET @conceptSourceId = NULL;

SELECT concept_source_id from concept_reference_source WHERE name = "#conceptSourceName#" into @conceptSourceId;

SET @conceptMapType = NULL;

SELECT concept_map_type_id from concept_map_type WHERE name = 'SAME-AS' into @conceptMapType;

SET @conceptRefMapSql = "LEFT JOIN (SELECT CRM.concept_id,  CRT.code FROM (SELECT * from concept_reference_term  WHERE concept_source_id = @conceptSourceId) as CRT INNER JOIN concept_reference_map as CRM ON CRT.concept_reference_term_id = CRM.concept_reference_term_id AND CRM.concept_map_type_id = @conceptMapType) CRT ON answer.concept_id = CRT.concept_id";

SET @sql = CONCAT('SELECT
               pi.identifier,
               ob.concept_id,
               concat(pat_name.given_name, '' '', pat_name.family_name)       AS patient_name,
               floor(DATEDIFF(DATE(ob.date_created), person.birthdate) / 365) AS age,
               person.gender,',
               IF(@patientAttributesSql = '', '', @patientAttributesSelectClause),
               'e.encounter_id,
               concat(pn.given_name, '' '', pn.family_name)                   AS provider_name,
               e.date_created,
               e.encounter_datetime
               #conceptNamesAndValue#
               FROM concept_view cv
                      LEFT JOIN obs ob on ob.concept_id = cv.concept_id and ob.voided = false
                      INNER JOIN encounter e on ob.encounter_id = e.encounter_id and cast(#applyDateRangeFor# AS DATE) BETWEEN \'#startDate#\' AND \'#endDate#\'
                      JOIN (select encounter_id from obs
                          WHERE concept_id = (select concept_id from concept_view WHERE concept_full_name = \'#templateName#\') and voided is false) e1
                            ON e.encounter_id = e1.encounter_id
                      #countOnlyTaggedLocationsJoin#
                      INNER JOIN person_name pat_name  on pat_name.person_id = ob.person_id
                      INNER JOIN person  on person.person_id = ob.person_id ',
                      IF(@patientAttributesSql = '', '', @patientAttributesJoin),
                      ' INNER JOIN patient_identifier pi on pi.patient_id = ob.person_id
                      INNER JOIN person_name pn on pn.person_id = ob.creator
                      LEFT JOIN concept_view coded_answer on coded_answer.concept_id = ob.value_coded ',
                       IF(@conceptSourceId IS NULL, '', @conceptRefMapSql),
                      ' GROUP BY pi.identifier');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

