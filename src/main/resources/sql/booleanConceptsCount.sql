select cov.concept_full_name as concept_name,
cov.value_concept_full_name as value_boolean,
p.gender,p.birthdate,rag.name as age_group,
IF(p.gender = 'F', 1, 0) AS female,
IF(p.gender = 'M', 1, 0) AS male
from coded_obs_view cov
  INNER JOIN person p on p.person_id = cov.person_id
  INNER JOIN reporting_age_group rag ON rag.report_group_name = '%s' AND
                                        cov.obs_datetime BETWEEN (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY))
                                        AND (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
where cov.value_concept_full_name in ("True", "False") and cast(cov.obs_datetime AS DATE) BETWEEN '%s' AND '%s' and cov.concept_full_name in (%s);