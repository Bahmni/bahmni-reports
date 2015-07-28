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
               coded_diagnosis_concept_name.concept_id leaf_concept_id,
               coded_diagnosis_concept_name.name       leaf_concept_name,
               v.date_started                          visit_date_started,
               v.date_stopped                          visit_date_stopped,
               p.gender                                person_gender,
               p.birthdate                             person_birthdate,
               coded_diagnosis_obs.obs_datetime        obs_datetime,
               vt.name                                 visit_type_name
             FROM
               obs revised_obs
               JOIN concept_name revised_concept
                 ON revised_obs.concept_id = revised_concept.concept_id
                    AND revised_concept.concept_name_type = 'FULLY_SPECIFIED'
                    AND revised_concept.name = 'Bahmni Diagnosis Revised'
                    AND revised_obs.voided IS FALSE
                    AND revised_concept.voided IS FALSE
               JOIN global_property false_global_property
                 ON revised_obs.value_coded = false_global_property.property_value
                    AND false_global_property.property = 'concept.false'
               JOIN obs coded_diagnosis_obs
                 ON revised_obs.obs_group_id = coded_diagnosis_obs.obs_group_id
               JOIN concept_name coded_diagnosis_concept
                 ON coded_diagnosis_obs.concept_id = coded_diagnosis_concept.concept_id
                    AND coded_diagnosis_concept.name = 'Coded Diagnosis'
                    AND coded_diagnosis_concept.concept_name_type = 'FULLY_SPECIFIED'
                    AND coded_diagnosis_concept.voided IS FALSE
               JOIN concept_name coded_diagnosis_concept_name
                 ON coded_diagnosis_obs.value_coded = coded_diagnosis_concept_name.concept_id
                    AND coded_diagnosis_concept_name.concept_name_type = 'FULLY_SPECIFIED'
                    AND coded_diagnosis_concept_name.voided IS FALSE
               JOIN obs diagnosis_status_obs
                 ON revised_obs.obs_group_id = diagnosis_status_obs.obs_group_id
               JOIN concept_name diagnosis_status_concept
                 ON diagnosis_status_obs.concept_id = diagnosis_status_concept.concept_id
                    AND diagnosis_status_concept.concept_name_type = 'FULLY_SPECIFIED'
                    AND diagnosis_status_concept.name = 'Bahmni Diagnosis Status'
                    AND diagnosis_status_obs.voided IS FALSE
                    AND diagnosis_status_concept.voided IS FALSE
                    AND diagnosis_status_obs.value_coded IS NULL
               JOIN obs diagnosis_certainty_obs
                 ON revised_obs.obs_group_id = diagnosis_certainty_obs.obs_group_id
               JOIN concept_name diagnosis_certainty_concept
                 ON diagnosis_certainty_obs.concept_id = diagnosis_certainty_concept.concept_id
                    AND diagnosis_certainty_concept.concept_name_type = 'FULLY_SPECIFIED'
                    AND diagnosis_certainty_concept.name = 'Diagnosis Certainty'
                    AND diagnosis_certainty_obs.voided IS FALSE
                    AND diagnosis_certainty_concept.voided IS FALSE
               JOIN concept_name confirmed_concept
                 ON diagnosis_certainty_obs.value_coded = confirmed_concept.concept_id
                    AND confirmed_concept.concept_name_type = 'FULLY_SPECIFIED'
                    AND confirmed_concept.name = 'Confirmed'
                    AND confirmed_concept.voided IS FALSE
               JOIN encounter e
                 ON revised_obs.encounter_id = e.encounter_id
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