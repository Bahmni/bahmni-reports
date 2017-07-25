set session group_concat_max_len = 20000;
SET @sql = NULL;

SET @patientAttributesSql = '#patientAttributes#';

SET @patientAttributesJoin = 'JOIN person_attribute_type pat ON pat.name in(#patientAttributesInClauseEscapeQuote#)
                       LEFT JOIN person_attribute pattr ON pattr.person_attribute_type_id = pat. person_attribute_type_id
                                                         AND pattr.person_id = person.person_id AND pattr.voided = false
                       LEFT JOIN concept_view person_attribute_cn ON pattr.value = person_attribute_cn.concept_id AND pat.format LIKE "%Concept"';

SET @patientAttributesSelectClause = 'pat.name  as patient_attr_name,
                       coalesce(person_attribute_cn.concept_short_name, person_attribute_cn.concept_full_name, pattr.value) as patient_attr_value,';


SET @conceptSourceId = NULL;

SELECT concept_source_id from concept_reference_source WHERE name = "#conceptSourceName#" into @conceptSourceId;

SET @conceptMapType = NULL;

SELECT concept_map_type_id from concept_map_type WHERE name = 'SAME-AS' into @conceptMapType;

SET @conceptRefMapSql = " LEFT JOIN (SELECT CRM.concept_id,  CRT.code FROM (SELECT * from concept_reference_term  WHERE concept_source_id = @conceptSourceId) as CRT INNER JOIN concept_reference_map as CRM ON CRT.concept_reference_term_id = CRM.concept_reference_term_id AND CRM.concept_map_type_id = @conceptMapType) CRT ON answer.concept_id = CRT.concept_id";

SET @sql = CONCAT('SELECT
                  o.identifier,
                  o.patient_name,
                  o.age,
                  o.gender,',
                  IF(@patientAttributesSql = '', '', CONCAT(@patientAttributesSql, ',')),
                  'o.provider_id,
                  o.encounter_id,
                  GROUP_CONCAT(DISTINCT(o.provider_name) SEPARATOR \',\') as provider_name,
                  o.date_created,
                  o.encounter_datetime
                  #conceptNamesAndValue#
                   FROM concept_view cv
                    LEFT JOIN
                    (SELECT
                       pi.identifier,
                       ob.concept_id,
                       concat(pat_name.given_name, '' '', ifnull(pat_name.family_name,"")) AS patient_name,
                       floor(DATEDIFF(DATE(ob.date_created), person.birthdate) / 365)   AS age,
                       person.gender,',
                       IF(@patientAttributesSql = '', '', @patientAttributesSelectClause),
                       'ep.provider_id,
                       ep.encounter_id,
                       concat(pn.given_name, '' '', ifnull(pn.family_name,""))   AS provider_name,
                       e.date_created,
                       e.encounter_datetime,
                       ob.value_numeric,
                       ob.value_datetime,
                       ob.date_created AS obs_date,
                       ob.value_text,
                       answer.concept_short_name,
                       answer.concept_full_name ',
                      IF(@conceptSourceId IS NULL, '', ', CRT.code'),
                     ' FROM obs ob
                       JOIN encounter e ON ob.encounter_id = e.encounter_id AND cast(#applyDateRangeFor# AS DATE) BETWEEN \'#startDate#\' AND \'#endDate#\' AND ob.voided IS FALSE
                       #countOnlyTaggedLocationsJoin#
                       JOIN (select encounter_id from obs
                                            where concept_id = (select concept_id from concept_view where concept_full_name = \'#templateName#\') and voided is false) e1
                       ON e.encounter_id = e1.encounter_id
                       JOIN patient_identifier pi ON pi.patient_id = ob.person_id and pi.preferred = 1
                       JOIN person ON person.person_id = pi.patient_id ',
                       IF(@patientAttributesSql = '', '', @patientAttributesJoin),
                       ' JOIN person_name pat_name ON pat_name.person_id = person.person_id
                       JOIN encounter_provider ep ON e.encounter_id = ep.encounter_id
                       JOIN provider p ON ep.provider_id = p.provider_id
                       JOIN person_name pn ON p.person_id = pn.person_id
                       LEFT JOIN concept_view answer ON ob.value_coded = answer.concept_id ',
                       IF(@conceptSourceId IS NULL, '', @conceptRefMapSql),
                  ' ) o ON o.concept_id = cv.concept_id
                  where cv.concept_full_name IN ( #conceptNameInClauseEscapeQuote# )
                  group by identifier, encounter_id
                  order by identifier, encounter_id');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;