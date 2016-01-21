SELECT
  reference_data.root_concept_name,
  reference_data.header_concept_name,
  reference_data.leaf_concept_name,
  reference_data.agegroup_name,
  reference_data.gender,
  count(observations.leaf_concept_name) AS diagnosis_count
FROM
  (SELECT
     rag.name                     agegroup_name,
     rag.min_years                min_years,
     rag.min_days                 min_days,
     rag.max_years                max_years,
     rag.max_days                 max_days,
     rag.sort_order               sort_order,
     root_concept.name            root_concept_name,
     gender.gender                gender,
     header_concept_name.name     header_concept_name,
     leaf_concept_name.name       leaf_concept_name,
     leaf_concept_name.concept_id leaf_concept_id
   FROM reporting_age_group rag
     JOIN concept_name root_concept
       ON root_concept.concept_name_type = 'FULLY_SPECIFIED'
          AND root_concept.voided IS FALSE
          AND root_concept.name = '#conceptName#'
     JOIN concept_set header_concepts
       ON root_concept.concept_id = header_concepts.concept_set
     JOIN concept_name header_concept_name
       ON header_concepts.concept_id = header_concept_name.concept_id
          AND header_concept_name.voided IS FALSE
          AND header_concept_name.concept_name_type = 'FULLY_SPECIFIED'
     JOIN concept_set leaf_concepts
       ON header_concepts.concept_id = leaf_concepts.concept_set
     JOIN concept_name leaf_concept_name
       ON leaf_concepts.concept_id = leaf_concept_name.concept_id
          AND leaf_concept_name.voided IS FALSE
          AND leaf_concept_name.concept_name_type = 'FULLY_SPECIFIED'
     JOIN (SELECT 'M' AS gender
           UNION SELECT 'F' AS gender
           UNION SELECT 'O' AS gender) gender
   WHERE rag.report_group_name = '#ageGroupName#'
  ) AS reference_data
  LEFT JOIN (SELECT
  cv.concept_id leaf_concept_id,
  cv.concept_full_name       leaf_concept_name,
  v.date_started                          visit_date_started,
  v.date_stopped                          visit_date_stopped,
  p.gender                                person_gender,
  p.birthdate                             person_birthdate,
  diagnosis.obs_datetime        obs_datetime,
  vt.name                                 visit_type_name
FROM obs AS diagnosis
  JOIN concept_view AS cv
    ON cv.concept_id = diagnosis.value_coded AND cv.concept_class_name = 'Diagnosis' AND
       diagnosis.voided IS FALSE
       AND diagnosis.obs_group_id IN (
    SELECT confirmed.obs_id
    FROM (
           SELECT parent.obs_id
           FROM obs AS parent
             JOIN concept_view pcv ON pcv.concept_id = parent.concept_id AND
                                      pcv.concept_full_name = 'Visit Diagnoses'
             LEFT JOIN obs AS child
               ON child.obs_group_id = parent.obs_id
                  AND child.voided IS FALSE
             JOIN concept_name AS confirmed
               ON confirmed.concept_id = child.value_coded AND confirmed.name = 'Confirmed' AND
                  confirmed.concept_name_type = 'FULLY_SPECIFIED'
           WHERE parent.voided IS FALSE ) AS confirmed
    WHERE confirmed.obs_id NOT IN (SELECT parent.obs_id
                                   FROM obs AS parent
                                     JOIN concept_view pcv2 ON pcv2.concept_id = parent.concept_id AND pcv2.concept_full_name = 'Visit Diagnoses'
                                     JOIN (
                                            SELECT obs_group_id
                                            FROM obs AS status
                                              JOIN concept_name ON concept_name.concept_id = status.value_coded AND
                                                                   concept_name.name = 'Ruled Out Diagnosis' AND
                                                                   concept_name.concept_name_type = 'FULLY_SPECIFIED' AND
                                                                   status.voided IS FALSE
                                            UNION
                                            SELECT obs_group_id
                                            FROM obs AS revised
                                              JOIN concept_name revised_concept
                                                ON revised_concept.concept_id = revised.concept_id AND
                                                   revised_concept.name = 'Bahmni Diagnosis Revised' AND
                                                   revised_concept.concept_name_type = 'FULLY_SPECIFIED' AND
                                                   revised.value_coded = (SELECT property_value FROM global_property WHERE property = 'concept.true') AND
                                                   revised.voided IS FALSE
                                          ) revised_and_ruled_out_diagnosis
                                       ON revised_and_ruled_out_diagnosis.obs_group_id = parent.obs_id
                                   WHERE parent.voided IS FALSE))

                  JOIN encounter e
                    ON diagnosis.encounter_id = e.encounter_id
                  #countOnlyTaggedLocationsJoin#
                  JOIN visit v
                    ON e.visit_id = v.visit_id
                  JOIN visit_type vt
                    ON vt.visit_type_id = v.visit_type_id
                  JOIN person p
                    ON v.patient_id = p.person_id
            #observationsWhereClause#
            ) observations
    ON reference_data.leaf_concept_id = observations.leaf_concept_id
       AND observations.obs_datetime
  BETWEEN (DATE_ADD(DATE_ADD(observations.person_birthdate, INTERVAL reference_data.min_years YEAR), INTERVAL
                    reference_data.min_days DAY))
  AND (DATE_ADD(DATE_ADD(observations.person_birthdate, INTERVAL reference_data.max_years YEAR), INTERVAL
                reference_data.max_days DAY))
       AND observations.person_gender = reference_data.gender
GROUP BY reference_data.root_concept_name,
  reference_data.header_concept_name,
  reference_data.leaf_concept_name,
  reference_data.agegroup_name,
  reference_data.gender
ORDER BY reference_data.sort_order ASC;