set session group_concat_max_len = 20000;
SET @sql = NULL;

SET @patientAttributesSql = '#patientAttributes#';

SET @patientAttributesJoin = 'JOIN person_attribute_type pat ON pat.name in(#patientAttributesInClauseEscapeQuote#)
                       LEFT JOIN person_attribute pattr ON pattr.person_attribute_type_id = pat. person_attribute_type_id
                                                         AND pattr.person_id = person.person_id AND pattr.voided = false
                       LEFT JOIN concept_view person_attribute_cn ON pattr.value = person_attribute_cn.concept_id AND pat.format LIKE "%Concept%"';

SET @patientAttributesSelectClause = 'pat.name  as patient_attr_name,
                       coalesce(person_attribute_cn.concept_short_name, person_attribute_cn.concept_full_name, pattr.value) as patient_attr_value,';

SET @programAttributesSql = '#programAttributes#';

SET @programAttributesJoin = 'JOIN program_attribute_type pg_at ON pg_at.name in(#programAttributesInClauseEscapeQuote#)
                       LEFT JOIN patient_program_attribute pg_attr ON pg_attr.attribute_type_id = pg_at.program_attribute_type_id
                                                         AND pg_attr.patient_program_id = pp.patient_program_id AND pg_attr.voided = false
                       LEFT JOIN concept_view pg_attr_cn ON pg_attr.value_reference = pg_attr_cn.concept_id AND pg_at.datatype LIKE "%Concept%"';

SET @programAttributesSelectClause = 'pg_at.name  as program_attr_name,
                       coalesce(pg_attr_cn.concept_short_name, pg_attr_cn.concept_full_name, pg_attr.value_reference) as program_attr_value,';


SET @conceptSourceId = NULL;

SELECT concept_source_id from concept_reference_source WHERE name = "#conceptSourceName#" into @conceptSourceId;

SET @conceptMapType = NULL;

SELECT concept_map_type_id from concept_map_type WHERE name = 'SAME-AS' into @conceptMapType;

SET @conceptRefMapSql = 'LEFT JOIN (SELECT CRM.concept_id,  CRT.code
                                    FROM (SELECT * from concept_reference_term
                                          WHERE concept_source_id = @conceptSourceId) as CRT
                                    INNER JOIN concept_reference_map as CRM
                                    ON CRT.concept_reference_term_id = CRM.concept_reference_term_id AND CRM.concept_map_type_id = @conceptMapType) CRT
                         ON answer.concept_id = CRT.concept_id';

SET @sql = CONCAT('SELECT
                      o.identifier,
                      o.patient_name,
                      o.age,
                      o.gender,',
                      IF(@patientAttributesSql = '', '', CONCAT(@patientAttributesSql, ',')),
                      IF(@programAttributesSql = '', '', CONCAT(@programAttributesSql, ',')),
                      'o.provider_id,
                      o.encounter_id,
                      o.program_name,
                      o.date_completed,
                      o.date_enrolled,
                      GROUP_CONCAT(DISTINCT(o.provider_name) SEPARATOR \',\') as provider_name,
                      o.date_created,
                      o.encounter_datetime
                      #conceptNamesAndValue#
                   FROM concept_view cv
                      LEFT JOIN
                      (SELECT
                         pi.identifier,
                         ob.concept_id,
                         concat(pat_name.given_name, '' '', pat_name.family_name) AS patient_name,
                         floor(DATEDIFF(DATE(ob.date_created), person.birthdate) / 365)   AS age,
                         person.gender,',
                         IF(@patientAttributesSql = '', '', @patientAttributesSelectClause),
                         IF(@programAttributesSql = '', '', @programAttributesSelectClause),
                         'ep.provider_id,
                         ep.encounter_id,
                         concat(pn.given_name, '' '', pn.family_name)   AS provider_name,
                         e.date_created,
                         prog.name as program_name,
                         pp.date_completed,
                         pp.date_enrolled,
                         e.encounter_datetime,
                         ob.value_numeric,
                         ob.value_boolean,
                         ob.value_datetime,
                         ob.date_created AS obs_date,
                         ob.value_text,
                         answer.concept_short_name,
                         answer.concept_full_name ',
                         IF(@conceptSourceId IS NULL, '', ', CRT.code'),
                      'FROM obs obs_temp
                         JOIN concept_view cv
                            ON (obs_temp.concept_id = cv.concept_id and cv.concept_full_name = \'#templateName#\' and obs_temp.voided is false)
                         JOIN obs ob
                            ON (ob.encounter_id = obs_temp.encounter_id AND ob.voided IS FALSE)
                         JOIN encounter e
                            ON (ob.encounter_id = e.encounter_id)
                         JOIN episode_encounter ee
                            ON (e.encounter_id=ee.encounter_id)
                         JOIN episode_patient_program epp
                            ON (epp.episode_id = ee.episode_id)
                         JOIN patient_program pp
                            ON (epp.patient_program_id = pp.patient_program_id  AND cast(pp.date_enrolled AS DATE) <= \'#endDate#\'  AND (cast(pp.date_completed AS DATE) >= \'#startDate#\' or  pp.date_completed is NULL) )
                         JOIN program prog
                            ON (pp.program_id = prog.program_id #programNamesListInClause#)
                         JOIN patient_identifier pi
                            ON pi.patient_id = ob.person_id
                         JOIN person ON person.person_id = pi.patient_id ',
                         IF(@patientAttributesSql = '', '', @patientAttributesJoin),
                         IF(@programAttributesSql = '', '', @programAttributesJoin),
                       ' JOIN person_name pat_name ON pat_name.person_id = person.person_id
                         JOIN encounter_provider ep ON e.encounter_id = ep.encounter_id
                         JOIN provider p ON ep.provider_id = p.provider_id
                         JOIN person_name pn ON p.person_id = pn.person_id
                         LEFT JOIN concept_view answer ON ob.value_coded = answer.concept_id ',
                         IF(@conceptSourceId IS NULL, '', @conceptRefMapSql),
                      ') o ON o.concept_id = cv.concept_id
                    where cv.concept_full_name IN ( #conceptNameInClauseEscapeQuote# )
                    group by identifier, encounter_id
                    order by identifier, encounter_id');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
