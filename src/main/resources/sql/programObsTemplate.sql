set session group_concat_max_len = 20000;
SET @sql = NULL;

SET @patientAttributesSql = '#patientAttributes#';

SET @patientAttributesJoin = ' LEFT JOIN (SELECT
                           person_id,
                           #patientAttributes#
                           FROM
                             person_attribute_type pat
                             LEFT JOIN person_attribute pattr ON pattr.person_attribute_type_id = pat.person_attribute_type_id AND pattr.voided = FALSE AND pat.name IN (#patientAttributesInClauseEscapeQuote#)
                             LEFT JOIN concept_view person_attribute_cn ON pattr.value = person_attribute_cn.concept_id AND pat.format LIKE "%Concept"
                           GROUP BY person_id) pattr_result ON pattr_result.person_id = person.person_id';
SET @patientAttributesSelectClause = '#patientAttributesInSelectClause#';

SET @programAttributesSql = '#programAttributes#';
SET @programAttributesJoin = ' LEFT JOIN (SELECT
                           patient_program_id,
                           #programAttributes#
                           FROM program_attribute_type pg_at
                                       LEFT JOIN patient_program_attribute pg_attr ON pg_attr.attribute_type_id = pg_at.program_attribute_type_id AND pg_attr.voided = false AND pg_at.name in(#programAttributesInClauseEscapeQuote#)
                                       LEFT JOIN concept_view pg_attr_cn ON pg_attr.value_reference = pg_attr_cn.concept_id AND pg_at.datatype LIKE "%Concept%"
                            GROUP BY patient_program_id ) prog_attr_result ON prog_attr_result.patient_program_id = pp.patient_program_id ';
SET @programAttributesSelectClause = '#programAttributesInSelectClause#';

SET @addressAttributesJoin = ' LEFT JOIN person_address address ON person.person_id = address.person_id ';
SET @addressAttributesInInnerQuery = '#addressAttributesInInnerQuery#';
SET @addressAttributesInOuterQuery = '#addressAttributesInOuterQuery#';

SET @conceptSourceId = NULL;

SELECT concept_source_id from concept_reference_source WHERE name = "#conceptSourceName#" into @conceptSourceId;

SET @conceptMapType = NULL;

SELECT concept_map_type_id from concept_map_type WHERE name = 'SAME-AS' into @conceptMapType;

SET @conceptRefMapSql = ' LEFT JOIN (SELECT CRM.concept_id,  CRT.code
                                    FROM (SELECT * from concept_reference_term
                                          WHERE concept_source_id = @conceptSourceId) as CRT
                                    INNER JOIN concept_reference_map as CRM
                                    ON CRT.concept_reference_term_id = CRM.concept_reference_term_id AND CRM.concept_map_type_id = @conceptMapType) CRTM
                         ON answer.concept_id = CRTM.concept_id';

SET @sql = CONCAT('SELECT
                      pi.identifier,
                      concat(pat_name.given_name, '' '', ifnull(pat_name.family_name,'')) AS patient_name,
                      floor(DATEDIFF(DATE(o.date_created), person.birthdate) / 365)   AS age,
                      person.gender,',
                      IF(@patientAttributesSql = '', '', @patientAttributesSelectClause),
                      IF(@programAttributesSql = '', '', @programAttributesSelectClause),
                      IF(@addressAttributesInInnerQuery = '', '', @addressAttributesInInnerQuery),
                      'e.encounter_id,
                      prog.name as program_name,
                      pp.date_completed,
                      pp.date_enrolled,
                      GROUP_CONCAT(DISTINCT(concat(pn.given_name, '' '', ifnull(pn.family_name, '')) SEPARATOR \',\') as provider_name,
                      e.date_created,
                      e.encounter_datetime
                      #conceptNamesAndValue#
                      FROM patient_program pp
                        JOIN program prog ON (pp.program_id = prog.program_id #programNamesListInClause#)  AND pp.voided = 0
                                              AND cast(pp.date_enrolled AS DATE) <= \'#endDate#\'  AND (cast(pp.date_completed AS DATE) >= \'#startDate#\' or  pp.date_completed is NULL)
                        JOIN episode_patient_program epp ON epp.patient_program_id = pp.patient_program_id
                        JOIN episode_encounter ee ON ee.episode_id = epp.episode_id
                        JOIN encounter e ON e.encounter_id = ee.encounter_id
                        JOIN (SELECT child_obs.obs_id,child_obs.encounter_id, child_obs.obs_group_id,max(root_obs.obs_id) root_obs_id
                              FROM obs child_obs
                                JOIN obs root_obs on (child_obs.encounter_id = root_obs.encounter_id and
                                                  child_obs.voided is FALSE AND
                                                  root_obs.voided is FALSE and
                                                  child_obs.obs_id > root_obs.obs_id and
                                                  root_obs.obs_group_id is null AND
                                                  child_obs.form_namespace_and_path = root_obs.form_namespace_and_path )
                                JOIN concept on ( concept.uuid =  TRIM(TRAILING ''^'' FROM root_obs.form_namespace_and_path ))
                                JOIN concept_view template on ( concept.concept_id=template.concept_id and template.concept_full_name = \'#templateName#\' )
                              GROUP BY child_obs.obs_id, child_obs.obs_group_id, child_obs.encounter_id) root_obs on (e.encounter_id = root_obs.encounter_id )
                        JOIN obs o ON o.obs_id = root_obs.obs_id AND o.voided IS FALSE
                        RIGHT JOIN concept_view cv ON cv.concept_id = o.concept_id
                        INNER JOIN person_name pat_name ON pat_name.person_id = o.person_id
                        INNER JOIN person ON person.person_id = o.person_id ',
                        IF (@addressAttributesInInnerQuery = '', '', @addressAttributesJoin),
                        IF(@patientAttributesSql = '', '', @patientAttributesJoin),
                        IF(@programAttributesSql = '', '', @programAttributesJoin),
                        ' INNER JOIN patient_identifier pi ON pi.patient_id = o.person_id and pi.preferred = 1
                        LEFT JOIN encounter_provider ON encounter_provider.encounter_id = o.encounter_id
                        LEFT JOIN provider ON provider.provider_id = encounter_provider.provider_id
                        LEFT JOIN person_name pn ON pn.person_id = provider.person_id
                        LEFT JOIN concept_view answer ON o.value_coded = answer.concept_id',
                        IF(@conceptSourceId IS NULL, '', @conceptRefMapSql),
                      ' GROUP BY pi.identifier, e.encounter_id,root_obs.root_obs_id');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
