SELECT
  ''                                       AS something,
  diagnosis_concept_view.concept_full_name AS disease,
  observed_age_group.name                  AS age_group,
  SUM(IF(person.gender = 'F', 1, 0))       AS female,
  SUM(IF(person.gender = 'M', 1, 0))       AS male,
  SUM(IF(person.gender = 'O', 1, 0))       AS other,
  diagnosis_concept_view.icd10_code        AS "ICD Code",
  observed_age_group.sort_order            AS age_group_sort_order
FROM diagnosis_concept_view
  JOIN reporting_age_group AS observed_age_group ON observed_age_group.report_group_name = '%s'
  LEFT OUTER JOIN (SELECT
                     diagnosis.value_coded,
                     diagnosis.person_id,
                     diagnosis.encounter_id,
                     diagnosis.obs_datetime
                   FROM obs AS diagnosis
                     JOIN concept_view AS cv
                       ON cv.concept_id = diagnosis.value_coded AND cv.concept_class_name = 'Diagnosis' AND
                          cast(diagnosis.obs_datetime AS DATE) BETWEEN '%s' AND '%s' AND diagnosis.voided = 0
                          AND diagnosis.obs_group_id IN (
                       SELECT confirmed.obs_id
                       FROM (
                              SELECT parent.obs_id
                              FROM obs AS parent
                                JOIN concept_view pcv ON pcv.concept_id = parent.concept_id AND
                                                         pcv.concept_full_name = 'Visit Diagnoses'
                                LEFT JOIN obs AS child
                                  ON child.obs_group_id = parent.obs_id
                                JOIN concept_name AS confirmed
                                  ON confirmed.concept_id = child.value_coded AND confirmed.name = 'Confirmed' AND
                                     confirmed.concept_name_type = 'FULLY_SPECIFIED') AS confirmed
                       WHERE confirmed.obs_id NOT IN (SELECT parent.obs_id
                                                      FROM obs AS parent
                                                        JOIN concept_view pcv2
                                                          ON pcv2.concept_id = parent.concept_id AND
                                                             pcv2.concept_full_name = 'Visit Diagnoses'
                                                        LEFT JOIN obs AS STATUS ON STATUS.obs_group_id = parent.obs_id
                                                        JOIN concept_name
                                                          ON concept_name.concept_id = STATUS.value_coded AND
                                                             concept_name.name = 'Ruled Out Diagnosis' AND
                                                             concept_name.concept_name_type =
                                                             'FULLY_SPECIFIED'))) AS filtered_diagnosis

  JOIN person
    ON person.person_id = filtered_diagnosis.person_id
  JOIN encounter e
    ON e.encounter_id = filtered_diagnosis.encounter_id
  JOIN visit_attribute va ON va.visit_id = e.visit_id AND va.value_reference IN (%s)

    ON diagnosis_concept_view.concept_id = filtered_diagnosis.value_coded
       AND
       filtered_diagnosis.obs_datetime BETWEEN (DATE_ADD(
           DATE_ADD(person.birthdate, INTERVAL observed_age_group.min_years YEAR), INTERVAL observed_age_group.min_days
           DAY))
       AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.max_years YEAR), INTERVAL
                     observed_age_group.max_days DAY))
GROUP BY disease, diagnosis_concept_view.icd10_code, age_group
ORDER BY disease;