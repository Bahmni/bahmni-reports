SELECT
  a.age_group           age_group,
  a.gender              gender,
  a.concept_name        concept_name,
  a.visit               visit,
  a.answer_concept_name answer_concept_name,
  a.sort_order          sort_order,
  sum(CASE WHEN a.visit_id IS NOT NULL AND a.value_reference IS NOT NULL AND a.gender IS NOT NULL THEN 1
      ELSE 0 END)       total_count
FROM
  (SELECT DISTINCT
     reporting_age_group.name                                                  AS age_group,
     gender.gender                                                             AS gender,
     IF(question.concept_short_name IS NULL, question.concept_full_name,
        question.concept_short_name)                                           AS concept_name,
     IF(visit_type.type IN ('Admitted', 'Discharged'), 'IPD', visit_type.type) AS visit,
     IF(answer.concept_short_name IS NULL, answer.concept_full_name,
        answer.concept_short_name)                                             AS answer_concept_name,
     reporting_age_group.sort_order                                            AS sort_order,
     visit.visit_id                                                            AS visit_id,
     attr.value_reference                                                      AS value_reference
   FROM
     concept_view AS question
     INNER JOIN concept_answer
       ON question.concept_id = concept_answer.concept_id AND question.concept_full_name IN (#conceptNames#)
     INNER JOIN concept_view AS answer
       ON answer.concept_id = concept_answer.answer_concept
     INNER JOIN (SELECT DISTINCT value_reference AS type
                 FROM visit_attribute) visit_type #visitFilter#
     INNER JOIN reporting_age_group
       ON reporting_age_group.report_group_name = '#reportGroupName#'
     INNER JOIN (SELECT 'M' as gender UNION SELECT 'F' AS gender UNION SELECT 'O' AS gender) as gender
     LEFT JOIN obs ON obs.concept_id = question.concept_id AND obs.value_coded = answer.concept_id
     LEFT JOIN person ON obs.person_id = person.person_id AND person.gender = gender.gender
     LEFT JOIN encounter enc ON enc.encounter_id = obs.encounter_id
     LEFT JOIN visit visit ON enc.visit_id = visit.visit_id
                              AND cast(visit.date_started AS DATE) BETWEEN (DATE_ADD(
       DATE_ADD(person.birthdate, INTERVAL reporting_age_group.min_years YEAR), INTERVAL reporting_age_group.min_days
       DAY)) AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL reporting_age_group.max_years YEAR), INTERVAL
                           reporting_age_group.max_days DAY)) AND cast(visit.date_started AS DATE) BETWEEN '#startDate#' AND '#endDate#'
     LEFT JOIN visit_attribute AS attr ON visit.visit_id = attr.visit_id AND attr.value_reference = visit_type.type
     LEFT JOIN visit_attribute_type AS attr_type
       ON attr_type.visit_attribute_type_id = attr.attribute_type_id AND attr_type.name = 'VisitStatus'
   ORDER BY age_group, gender, concept_name, visit, answer_concept_name, sort_order, visit_id) a
GROUP BY a.age_group, a.gender, a.concept_name, a.visit, a.answer_concept_name, a.sort_order;

