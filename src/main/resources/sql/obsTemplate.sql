set session group_concat_max_len = 20000;
SET @sql = NULL;
SELECT
  GROUP_CONCAT(DISTINCT
               CONCAT(
                   'GROUP_CONCAT(IF(cv.concept_full_name = ''',
                   concept_full_name,
                   ''', coalesce(ob.value_numeric, ob.value_boolean, ob.value_datetime, ob.value_text, answer.concept_short_name, answer.concept_full_name), NULL) SEPARATOR \',\') AS `',
                   concept_full_name , '`'
               )
  ) into @sql
FROM concept_view cv where cv.concept_full_name in (%s);

SET @sql = CONCAT('SELECT
                  pi.identifier,
                  ep.provider_id,
                  ep.encounter_id,
                  concat(pn.given_name, \' \', pn.family_name) AS provider_name,
                  e.encounter_datetime,',
                 @sql,
                  ' FROM concept_view cv
                  LEFT JOIN obs ob ON ob.concept_id = cv.concept_id AND ob.voided is false
                  LEFT JOIN concept_view answer on ob.value_coded = answer.concept_id
                  LEFT JOIN encounter e ON ob.encounter_id = e.encounter_id and cast(e.encounter_datetime AS DATE) BETWEEN \'%s\' AND \'%s\'
                  LEFT JOIN patient_identifier pi ON pi.patient_id = ob.person_id
                  LEFT JOIN encounter_provider ep ON e.encounter_id = ep.encounter_id
                  LEFT JOIN provider p ON ep.provider_id = p.provider_id
                  LEFT JOIN person_name pn on p.person_id = pn.person_id
                  where cv.concept_full_name IN ( %s )
                  group by identifier, encounter_id, provider_id
                  order by identifier, e.encounter_id');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

