SELECT
  ''                                       AS something,
  diagnosis_concept_view.concept_full_name AS disease,
  diagnosis_concept_view.icd10_code,
  SUM(IF(person.gender = 'F', 1, 0))       AS female,
  SUM(IF(person.gender = 'M', 1, 0))       AS male,
  SUM(IF(person.gender = 'O', 1, 0))       AS other
FROM diagnosis_concept_view
LEFT OUTER join (select  diagnosis.value_coded, diagnosis.person_id, diagnosis.encounter_id from obs AS diagnosis
						JOIN concept_view AS cv
						ON cv.concept_id = diagnosis.value_coded AND cv.concept_class_name = 'Diagnosis' AND
						cast(diagnosis.obs_datetime AS DATE) BETWEEN '%s' AND '%s'  AND diagnosis.voided = 0
						AND diagnosis.obs_group_id IN (
													select distinct confirmed.obs_id from (
																		SELECT DISTINCT parent.obs_id
																		FROM obs AS parent
																		JOIN concept_view pcv ON pcv.concept_id = parent.concept_id AND
																			 pcv.concept_full_name = 'Visit Diagnoses'
																		LEFT JOIN obs AS child
																		ON child.obs_group_id = parent.obs_id
																			AND child.voided IS FALSE
																		JOIN concept_name AS confirmed
																		ON confirmed.concept_id = child.value_coded AND confirmed.name = 'Confirmed' AND
																		   confirmed.concept_name_type = 'FULLY_SPECIFIED'
																		WHERE parent.voided IS FALSE) AS confirmed
																		WHERE confirmed.obs_id NOT IN (SELECT DISTINCT parent.obs_id
																						FROM obs AS parent
																						JOIN concept_view pcv2
																						ON pcv2.concept_id = parent.concept_id AND
																						   pcv2.concept_full_name = 'Visit Diagnoses'
																						LEFT JOIN obs AS STATUS ON STATUS.obs_group_id = parent.obs_id
																							AND STATUS.voided IS FALSE
																					    JOIN concept_name
																						ON concept_name.concept_id = STATUS.value_coded AND
																							concept_name.name = 'Ruled Out Diagnosis' AND
																							concept_name.concept_name_type = 'FULLY_SPECIFIED'
																		AND parent.voided IS FALSE ))) AS filtered_diagnosis
						JOIN person
						ON person.person_id = filtered_diagnosis.person_id
						JOIN encounter e
						ON e.encounter_id = filtered_diagnosis.encounter_id
						JOIN visit_attribute va on va.visit_id = e.visit_id and va.value_reference in (%s)
ON diagnosis_concept_view.concept_id = filtered_diagnosis.value_coded
GROUP BY disease, diagnosis_concept_view.icd10_code
ORDER BY disease;
