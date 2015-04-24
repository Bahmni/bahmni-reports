SET @sql = NULL;
SELECT
  GROUP_CONCAT(DISTINCT
               CONCAT(
                   'IF(cv.concept_full_name = ''',
                   concept_full_name,
                   ''', ob.value_numeric, NULL) AS ',
                   concept_full_name
               )
  ) into @sql
FROM concept_view cv join obs ob on cv.concept_id = ob.concept_id where cv.concept_full_name in ('Pulse');

SET @var = '\'Pulse\', \'Systolic\'';
SET @sql = CONCAT('SELECT
                  pi.identifier,
                  ep.provider_id,
                  ep.encounter_id,
                  pn.given_name AS provider_name,
                  e.encounter_datetime,',
                 @sql,
                  ' FROM concept_view cv
                  LEFT JOIN obs ob ON ob.concept_id = cv.concept_id AND ob.voided = 0
                  LEFT JOIN encounter e ON e.patient_id = ob.person_id
                  LEFT JOIN patient_identifier pi ON pi.patient_id = ob.person_id
                  LEFT JOIN encounter_provider ep ON e.encounter_id = ep.encounter_id
                  LEFT JOIN person_name pn ON ep.provider_id = pn.person_id
                  where cv.concept_full_name IN (', @var, ')
                  group by identifier, encounter_id, provider_id
                  order by identifier, e.encounter_id');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

