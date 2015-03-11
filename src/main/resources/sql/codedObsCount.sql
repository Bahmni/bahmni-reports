SELECT
  reporting_age_group.name                                                                         AS age_group,
  IF(question.concept_short_name IS NULL, question.concept_full_name, question.concept_short_name) AS concept_name,
  answer.concept_full_name                                                                         AS answer_concept_name,
  reporting_age_group.sort_order                                                                   AS sort_order,
  IF(reporting_age_group.id IS NULL, 0, SUM(IF(person.gender = 'F', 1, 0)))                        AS female_count,
  IF(reporting_age_group.id IS NULL, 0, SUM(IF(person.gender = 'M', 1, 0)))                        AS male_count,
  IF(reporting_age_group.id IS NULL, 0, SUM(IF(person.gender = 'O', 1, 0)))                        AS other_count,
  COUNT(obs.obs_id)                                                                                AS total_count
FROM
  reporting_age_group
  JOIN
  concept_view AS question ON question.concept_full_name IN (%s) AND question.concept_datatype_name = 'Coded'
  INNER JOIN concept_answer
    ON question.concept_id = concept_answer.concept_id
  INNER JOIN concept_view AS answer
    ON answer.concept_id = concept_answer.answer_concept
  LEFT JOIN obs
  INNER JOIN person ON obs.person_id = person.person_id
    ON cast(obs.obs_datetime AS DATE) BETWEEN '%s' AND '%s' AND obs.concept_id = question.concept_id AND
       obs.value_coded = concept_answer.answer_concept
       AND cast(obs.obs_datetime AS DATE) BETWEEN (DATE_ADD(
      DATE_ADD(person.birthdate, INTERVAL reporting_age_group.min_years YEAR), INTERVAL reporting_age_group.min_days
      DAY)) AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL reporting_age_group.max_years YEAR), INTERVAL
                          reporting_age_group.max_days DAY))
WHERE reporting_age_group.report_group_name = '%s'
GROUP BY concept_name, answer.concept_id, reporting_age_group.id
ORDER BY answer.concept_id, reporting_age_group.sort_order;
