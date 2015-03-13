SELECT
  reporting_age_group.name                                                                               AS age_group,
  IF(question.concept_short_name IS NULL, question.concept_full_name, question.concept_short_name)       AS concept_name,
  IF(visit_type.type IN ('Admitted', 'Discharged'), 'IPD', visit_type.type)                              AS visit,
  IF(answer.concept_short_name IS NULL, answer.concept_full_name, answer.concept_short_name)             AS answer_concept_name,
  reporting_age_group.sort_order                                                                         AS sort_order,
  IF(person.person_id IS NOT NULL AND attr.visit_attribute_id IS NOT NULL AND person.gender = 'F', 1, 0) AS female_count,
  IF(person.person_id IS NOT NULL AND attr.visit_attribute_id IS NOT NULL AND person.gender = 'M', 1, 0) AS male_count,
  IF(person.person_id IS NOT NULL AND attr.visit_attribute_id IS NOT NULL AND person.gender = 'O', 1, 0) AS other_count,
  IF(person.person_id IS NOT NULL AND attr.visit_attribute_id IS NOT NULL, 1, 0)                         AS total_count
FROM
  concept_view AS question
  INNER JOIN concept_answer
    ON question.concept_id = concept_answer.concept_id AND question.concept_full_name IN (%s)
  INNER JOIN concept_view AS answer
    ON answer.concept_id = concept_answer.answer_concept
  INNER JOIN (SELECT 'Admitted' AS 'type'
              UNION SELECT 'Discharged' AS 'type'
              UNION SELECT 'OPD' AS 'type'
              UNION SELECT 'Special OPD' AS 'type'
              UNION SELECT 'EMERGENCY' AS 'type'
              UNION SELECT 'PHARMACY VISIT' AS 'type'
              UNION SELECT 'LAB VISIT' AS 'type') visit_type %s
  INNER JOIN reporting_age_group
    ON reporting_age_group.report_group_name = '%s'
  LEFT JOIN obs ON obs.concept_id = question.concept_id AND obs.value_coded = answer.concept_id AND cast(obs.obs_datetime AS DATE) BETWEEN '%s' AND '%s'
  LEFT JOIN person ON obs.person_id = person.person_id
                      AND cast(obs.obs_datetime AS DATE) BETWEEN (DATE_ADD(
      DATE_ADD(person.birthdate, INTERVAL reporting_age_group.min_years YEAR), INTERVAL reporting_age_group.min_days
      DAY)) AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL reporting_age_group.max_years YEAR), INTERVAL
                          reporting_age_group.max_days DAY))
  LEFT JOIN encounter enc ON enc.encounter_id = obs.encounter_id
  LEFT JOIN visit visit ON enc.visit_id = visit.visit_id
  LEFT JOIN visit_attribute AS attr ON visit.visit_id = attr.visit_id AND attr.value_reference = visit_type.type
  LEFT JOIN visit_attribute_type AS attr_type ON attr_type.visit_attribute_type_id = attr.attribute_type_id AND attr_type.name = 'VisitStatus'
