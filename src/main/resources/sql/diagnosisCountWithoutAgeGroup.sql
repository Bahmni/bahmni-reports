SELECT
  ''                                       AS something,
  diagnosis_concept_view.concept_full_name AS disease,
  diagnosis_concept_view.icd10_code,
  SUM(IF(person.gender = 'F', 1, 0))       AS female,
  SUM(IF(person.gender = 'M', 1, 0))       AS male
FROM diagnosis_concept_view
join obs AS diagnosis on diagnosis_concept_view.concept_id = diagnosis.value_coded
  JOIN concept_view
    ON concept_view.concept_id = diagnosis.value_coded AND concept_view.concept_class_name = 'Diagnosis' AND
		(cast(diagnosis.obs_datetime AS DATE) BETWEEN '%s' AND '%s') AND
       diagnosis.obs_group_id IN (
select distinct confirmed.obs_id from
  (SELECT DISTINCT parent.obs_id
   FROM	
     obs AS parent JOIN concept_view ON concept_view.concept_id = parent.concept_id AND
                                        concept_view.concept_full_name =
                                        'Visit Diagnoses'
     LEFT JOIN obs AS child ON child.obs_group_id = parent.obs_id
     JOIN concept_name AS confirmed
       ON confirmed.concept_id = child.value_coded AND confirmed.name = 'Confirmed' AND
          confirmed.concept_name_type = 'FULLY_SPECIFIED') as confirmed
  where confirmed.obs_id not in (SELECT DISTINCT parent.obs_id
        FROM obs AS parent JOIN concept_view
            ON concept_view.concept_id = parent.concept_id AND concept_view.concept_full_name = 'Visit Diagnoses'
          LEFT JOIN obs AS STATUS ON STATUS.obs_group_id = parent.obs_id
          JOIN concept_name
            ON concept_name.concept_id = STATUS.value_coded AND concept_name.name = 'Ruled Out Diagnosis' AND
               concept_name.concept_name_type = 'FULLY_SPECIFIED')) 
JOIN person on person.person_id = diagnosis.person_id
join encounter e on e.encounter_id = diagnosis.encounter_id join visit_attribute va on va.visit_id = e.visit_id and va.value_reference in (%s)
group by disease, diagnosis_concept_view.icd10_code
order by disease;
