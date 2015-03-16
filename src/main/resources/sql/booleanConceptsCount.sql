select cov.concept_full_name as concept_name,cov.value_concept_full_name as value_boolean,p.gender,p.birthdate,rag.name as age_group,
IF(p.gender = 'F', 1, 0) AS female,
IF(p.gender = 'M', 1, 0) AS male,
IF(p.gender = 'O', 1, 0) AS other
from coded_obs_view cov
  inner JOIN person p on p.person_id = cov.person_id
                               and cast(cov.obs_datetime AS DATE) BETWEEN '%s' AND '%s'
                               and cov.value_concept_full_name in ("True", "False")
                               and cov.concept_full_name in (%s)
   Right outer JOIN reporting_age_group rag ON
                                        cov.obs_datetime BETWEEN (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY))
                                        AND (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
where rag.report_group_name = '%s';