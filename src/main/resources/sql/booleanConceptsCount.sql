select base.age_group, person_id, base.concept_name, base.value_boolean, female, male, other, result.visit_id from
(select rag.name as age_group, cn.name as concept_name, bool.value_boolean  from reporting_age_group rag, concept_name cn,(select 'True' as value_boolean union select 'False' as value_boolean) bool  where rag.report_group_name = '%s' and cn.name in (%s) )
  base
left outer join
(select cn.name, cbvn.name as value_boolean,
IF(p.gender = 'F', 1, 0) AS female,
IF(p.gender = 'M', 1, 0) AS male,
IF(p.gender = 'O', 1, 0) AS other,
rag.name as rag_age_group,
p.person_id,
encounter.visit_id
 from obs obs
  inner join concept c on obs.concept_id = c.concept_id
  inner join concept_name cn on cn.concept_id = c.concept_id and cn.concept_name_type = 'FULLY_SPECIFIED' and cn.name in (%s)
  inner join concept_datatype cd on c.datatype_id = cd.concept_datatype_id and cd.name = 'Boolean'
  inner join concept_name cbvn on obs.value_coded = cbvn.concept_id and cbvn.name in ('True', 'False')
  inner join person p on p.person_id = obs.person_id
  inner join reporting_age_group rag ON obs.obs_datetime BETWEEN (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY))
                                      AND (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
                                      AND rag.report_group_name = '%s'
  LEFT JOIN encounter encounter on encounter.encounter_id = obs.encounter_id
where cast(obs.obs_datetime AS DATE) BETWEEN '%s' AND '%s') result on base.age_group = result.rag_age_group and base.concept_name = result.name and result.value_boolean = base.value_boolean
GROUP BY base.age_group, person_id, base.concept_name, base.value_boolean, female, male, other, result.visit_id