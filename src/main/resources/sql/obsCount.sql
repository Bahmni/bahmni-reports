select base.age_group,
  base.sort_order,
  IF(base.value_reference IN ('Admitted', 'Discharged'), 'IPD', base.value_reference) AS visit_type,
  IF(shortName.name is null ,base.concept_name,shortName.name) as concept_name,
  final.concept_id,
  final.female,
  final.male,
  final.other,
  final.datetime,
  final.birthdate,
  final.age_group,
  final.gender,
  final.sort_order
from
  (select rag.name as age_group, cn.concept_id,cn.name as concept_name, rag.sort_order,va.value_reference
   from reporting_age_group rag, concept_name cn, (select distinct value_reference from visit_attribute va where va.visit_attribute_id is not null %s) va
   where rag.report_group_name = '%s' and cn.name in (%s) and cn.concept_name_type = 'FULLY_SPECIFIED') base
  left outer join
  (select cn.name as concept_name,
          cn.concept_id as concept_id,
          IF(p.gender = 'F', 1, 0) AS female,
          IF(p.gender = 'M', 1, 0) AS male,
          IF(p.gender = 'O', 1, 0) AS other,
          va.value_reference,
     v.date_stopped as datetime,
     p.birthdate,
          rag.name as age_group,
     rag.sort_order,
          p.gender as gender
   from obs obs
     inner join concept_name cn on obs.concept_id = cn.concept_id and cn.concept_name_type = 'FULLY_SPECIFIED' and obs.voided = 0 and cn.voided = 0
     inner join encounter e on obs.encounter_id = e.encounter_id
     INNER JOIN visit v on v.visit_id = e.visit_id
     INNER JOIN visit_attribute va on va.visit_id = v.visit_id
     INNER JOIN person p on p.person_id = obs.person_id
     inner join reporting_age_group rag ON DATE(v.date_stopped) BETWEEN (DATE_ADD(DATE_ADD(birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY)) AND (DATE_ADD(DATE_ADD(birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
      and rag.report_group_name='%s'
   where cn.name in (%s) and cast(v.date_stopped AS DATE) BETWEEN '%s' AND '%s' %s AND obs.voided IS FALSE 
   GROUP BY v.visit_id,cn.name) final on final.age_group = base.age_group and final.concept_id = base.concept_id and final.value_reference = base.value_reference
  left outer join concept_name shortName on shortName.concept_id = base.concept_id and shortName.concept_name_type = 'SHORT' and shortName.voided = 0
order by base.sort_order asc;
