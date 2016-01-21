SELECT
  diagnosis_concept_view.concept_full_name AS disease,
  observed_age_group.name                  AS age_group,
  SUM(IF(person.gender = 'F', 1, 0))       AS female,
  SUM(IF(person.gender = 'M', 1, 0))       AS male,
  SUM(IF(person.gender = 'O', 1, 0))       AS other,
  concept_reference_term_map_view.code as icd10_code,
  observed_age_group.sort_order            AS age_group_sort_order
FROM (select * from reporting_age_group where report_group_name = '#ageGroupName#')
AS observed_age_group
  left JOIN (SELECT
                     diagnosis.value_coded,
                     diagnosis.person_id,
                     diagnosis.encounter_id,
                     diagnosis.obs_datetime
                   FROM obs AS diagnosis
                     JOIN concept_view AS cv
                       ON cv.concept_id = diagnosis.value_coded AND cv.concept_class_name = 'Diagnosis' AND
                          cast(diagnosis.obs_datetime AS DATE) BETWEEN '#startDate#' AND '#endDate#' AND diagnosis.voided IS FALSE
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
                                      WHERE parent.voided IS FALSE))) AS filtered_diagnosis

  JOIN person
    ON person.person_id = filtered_diagnosis.person_id
  JOIN encounter e
    ON e.encounter_id = filtered_diagnosis.encounter_id
  #countOnlyTaggedLocationsJoin#
  JOIN visit_attribute va ON va.visit_id = e.visit_id AND va.value_reference IN (#visitType#)
  LEFT JOIN visit_attribute_type vat on vat.visit_attribute_type_id = va.attribute_type_id AND vat.name = 'Visit Status'
  JOIN concept_view diagnosis_concept_view on diagnosis_concept_view.concept_id = filtered_diagnosis.value_coded
                                  LEFT JOIN concept_reference_term_map_view
                                  ON concept_reference_term_map_view.concept_id = diagnosis_concept_view.concept_id
                                  AND concept_reference_term_map_view.concept_reference_source_name= '#icd10ConceptSource#'
                                  AND concept_reference_term_map_view.concept_map_type_name ='SAME-AS'


on filtered_diagnosis.obs_datetime BETWEEN (DATE_ADD(
           DATE_ADD(person.birthdate, INTERVAL observed_age_group.min_years YEAR), INTERVAL observed_age_group.min_days
           DAY))
       AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.max_years YEAR), INTERVAL
                     observed_age_group.max_days DAY))
GROUP BY disease, concept_reference_term_map_view.code, age_group
ORDER BY disease;