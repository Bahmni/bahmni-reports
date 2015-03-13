select IF(shortName.name is null ,cn.name,shortName.name) as concept_name,
    rag.name as age_group,
       IF(p.gender = 'F', 1, 0) AS female,
       IF(p.gender = 'M', 1, 0) AS male,
       IF(p.gender = 'O', 1, 0) AS other,
       IF(va.value_reference IN ('Admitted', 'Discharged'), 'IPD', va.value_reference) AS visit_type,
       rag.report_group_name as report_group_name
from obs obs
  inner join concept_name cn on obs.concept_id = cn.concept_id
  inner join encounter e on obs.encounter_id = e.encounter_id
  INNER JOIN visit v on v.visit_id = e.visit_id
  INNER JOIN visit_attribute va on va.visit_id = v.visit_id
  INNER JOIN person p on p.person_id = obs.person_id
  INNER JOIN reporting_age_group rag ON rag.report_group_name = '%s' AND
                                        obs.obs_datetime BETWEEN (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY))
                                        AND (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
  LEFT OUTER JOIN concept_name shortName on shortName.concept_id = cn.concept_id and shortName.concept_name_type = 'SHORT' and shortName.voided = 0
where cn.voided=0 and cast(obs.obs_datetime AS DATE) BETWEEN '%s' AND '%s' and cn.name in (%s) %s
GROUP BY v.visit_id,cn.name