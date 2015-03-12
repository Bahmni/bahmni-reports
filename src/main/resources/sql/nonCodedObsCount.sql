select cn.name as concept_name,v.visit_id,count(obs.value_numeric),p.gender,p.birthdate,rag.name as age_group,
IF(p.gender = 'F', 1, 0) AS female,
IF(p.gender = 'M', 1, 0) AS male,
va.value_reference as visit_type
from obs obs
  inner join concept_name cn on obs.concept_id = cn.concept_id
  inner join encounter e on obs.encounter_id = e.encounter_id
  INNER JOIN visit v on v.visit_id = e.visit_id
  INNER JOIN visit_attribute va on va.visit_id = v.visit_id
  INNER JOIN person p on p.person_id = obs.person_id
  INNER JOIN reporting_age_group rag ON rag.report_group_name = '%s' AND
                                                    obs.obs_datetime BETWEEN (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY))
                                                    AND (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
where obs.value_coded is null and cast(obs.obs_datetime AS DATE) BETWEEN '%s' AND '%s' and cn.name in (%s)
GROUP BY v.visit_id;