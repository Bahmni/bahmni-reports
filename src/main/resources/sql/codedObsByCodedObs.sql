SELECT
  first_answers.answer_name as first_concept_name,
  second_answers.answer_name as second_concept_name,
  gender.gender as gender,
  rag.name as age_group,
  rag.sort_order as age_group_sort_order,
  sum(CASE WHEN first_concept.answer IS NOT NULL AND second_concept.answer IS NOT NULL AND p.gender IS NOT NULL THEN 1
      ELSE 0 END) as patient_count
FROM
  (SELECT
     ca.answer_concept                                                                AS answer,
     ifnull(answer_concept_short_name.name, answer_concept_fully_specified_name.name) AS answer_name
   FROM concept c
     INNER JOIN concept_datatype cd ON c.datatype_id = cd.concept_datatype_id
     INNER JOIN concept_name question_concept_name ON c.concept_id = question_concept_name.concept_id
                                                      AND question_concept_name.concept_name_type = 'FULLY_SPECIFIED' AND
                                                      question_concept_name.voided IS FALSE
     INNER JOIN concept_answer ca ON c.concept_id = ca.concept_id
     INNER JOIN concept_name answer_concept_fully_specified_name
       ON ca.answer_concept = answer_concept_fully_specified_name.concept_id
          AND answer_concept_fully_specified_name.concept_name_type = 'FULLY_SPECIFIED' AND
          answer_concept_fully_specified_name.voided IS FALSE
     LEFT JOIN concept_name answer_concept_short_name ON ca.answer_concept = answer_concept_short_name.concept_id
                                                         AND answer_concept_short_name.concept_name_type = 'SHORT' AND
                                                         answer_concept_short_name.voided IS FALSE
   WHERE question_concept_name.name = '#firstConcept#' AND cd.name = 'Coded'
   UNION
   SELECT
     answer_concept_fully_specified_name.concept_id                                                                AS answer,
     answer_concept_fully_specified_name.name AS answer_name
   FROM concept c
     INNER JOIN concept_datatype cd ON c.datatype_id = cd.concept_datatype_id
     INNER JOIN concept_name question_concept_name ON c.concept_id = question_concept_name.concept_id
                                                      AND question_concept_name.concept_name_type = 'FULLY_SPECIFIED' AND
                                                      question_concept_name.voided IS FALSE
     INNER JOIN global_property gp ON gp.property in ('concept.true', 'concept.false')
     INNER JOIN concept_name answer_concept_fully_specified_name
       ON answer_concept_fully_specified_name.concept_id = gp.property_value
          AND answer_concept_fully_specified_name.concept_name_type = 'FULLY_SPECIFIED' AND
          answer_concept_fully_specified_name.voided IS FALSE
   WHERE question_concept_name.name = '#firstConcept#' AND cd.name = 'Boolean'
   ORDER BY answer_name DESC) first_answers
  INNER JOIN
  (SELECT
     ca.answer_concept                                                                AS answer,
     ifnull(answer_concept_short_name.name, answer_concept_fully_specified_name.name) AS answer_name
   FROM concept c
     INNER JOIN concept_datatype cd ON c.datatype_id = cd.concept_datatype_id
     INNER JOIN concept_name question_concept_name ON c.concept_id = question_concept_name.concept_id
                                                      AND question_concept_name.concept_name_type = 'FULLY_SPECIFIED' AND
                                                      question_concept_name.voided IS FALSE
     INNER JOIN concept_answer ca ON c.concept_id = ca.concept_id
     INNER JOIN concept_name answer_concept_fully_specified_name
       ON ca.answer_concept = answer_concept_fully_specified_name.concept_id
          AND answer_concept_fully_specified_name.concept_name_type = 'FULLY_SPECIFIED' AND
          answer_concept_fully_specified_name.voided IS FALSE
     LEFT JOIN concept_name answer_concept_short_name ON ca.answer_concept = answer_concept_short_name.concept_id
                                                         AND answer_concept_short_name.concept_name_type = 'SHORT' AND
                                                         answer_concept_short_name.voided IS FALSE
   WHERE question_concept_name.name = '#secondConcept#' AND cd.name = 'Coded'
   UNION
   SELECT
     answer_concept_fully_specified_name.concept_id                                                                AS answer,
     answer_concept_fully_specified_name.name AS answer_name
   FROM concept c
     INNER JOIN concept_datatype cd ON c.datatype_id = cd.concept_datatype_id
     INNER JOIN concept_name question_concept_name ON c.concept_id = question_concept_name.concept_id
                                                      AND question_concept_name.concept_name_type = 'FULLY_SPECIFIED' AND
                                                      question_concept_name.voided IS FALSE
     INNER JOIN global_property gp ON gp.property in ('concept.true', 'concept.false')
     INNER JOIN concept_name answer_concept_fully_specified_name
       ON answer_concept_fully_specified_name.concept_id = gp.property_value
          AND answer_concept_fully_specified_name.concept_name_type = 'FULLY_SPECIFIED' AND
          answer_concept_fully_specified_name.voided IS FALSE
   WHERE question_concept_name.name = '#secondConcept#' AND cd.name = 'Boolean'
   ORDER BY answer_name DESC
  ) second_answers
  INNER JOIN (SELECT 'M' AS gender UNION SELECT 'F' AS gender) gender
  INNER JOIN reporting_age_group rag ON rag.report_group_name = '#reportGroupName#'
  LEFT OUTER JOIN (
                    SELECT DISTINCT
                      o1.person_id,
                      cn2.concept_id AS    answer,
                      cn1.concept_id AS    question,
                      e1.visit_id,
                      max(e1.encounter_datetime) AS datetime
                    FROM obs o1
                      INNER JOIN concept_name cn1
                        ON o1.concept_id = cn1.concept_id AND
                           cn1.concept_name_type = 'FULLY_SPECIFIED' AND cn1.name = '#firstConcept#'
                           AND o1.voided = 0 AND cn1.voided = 0
                      INNER JOIN concept_name cn2
                        ON o1.value_coded = cn2.concept_id
                           AND cn2.concept_name_type = 'FULLY_SPECIFIED'
                           AND cn2.voided = 0
                      INNER JOIN encounter e1
                        ON o1.encounter_id = e1.encounter_id
                    WHERE cast(o1.obs_datetime as date) BETWEEN '#startDate#' AND '#endDate#'
                    GROUP BY o1.person_id, cn2.concept_id, cn1.concept_id, e1.visit_id
                  ) first_concept
    ON first_concept.answer = first_answers.answer
  LEFT OUTER JOIN (
                    SELECT DISTINCT
                      o1.person_id,
                      cn2.concept_id AS answer,
                      cn1.concept_id AS question
                    FROM obs o1
                      INNER JOIN concept_name cn1
                        ON o1.concept_id = cn1.concept_id AND
                           cn1.concept_name_type = 'FULLY_SPECIFIED'
                           AND cn1.name = '#secondConcept#'
                           AND o1.voided = 0 AND cn1.voided = 0
                      INNER JOIN concept_name cn2
                        ON o1.value_coded = cn2.concept_id
                           AND cn2.concept_name_type = 'FULLY_SPECIFIED'
                           AND cn2.voided = 0
                      INNER JOIN encounter e1
                        ON o1.encounter_id = e1.encounter_id
                    WHERE cast(o1.obs_datetime as date) BETWEEN '#startDate#' AND '#endDate#'
                  ) second_concept
    ON second_concept.answer = second_answers.answer
       AND first_concept.person_id = second_concept.person_id
  LEFT OUTER JOIN person p ON first_concept.person_id = p.person_id AND p.gender = gender.gender
                              AND cast(first_concept.datetime AS DATE) BETWEEN (DATE_ADD(
    DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days
    DAY)) AND (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR), INTERVAL
                        rag.max_days DAY))
GROUP BY first_answers.answer_name, second_answers.answer_name, gender.gender, rag.name, rag.sort_order;