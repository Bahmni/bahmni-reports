select 'Rapid Test for Malaria' as "Malaria_Test", o.value_text as "Result_Type", count(*) as Count 
	from obs o 
    inner join concept_name cn on o.concept_id = cn.concept_id
    where cn.name = "%s" and o.value_text is not null and o.obs_datetime between '%s' and '%s' group by o.value_text

union all

select 'PS for MP' as "Malaria_Test", value_text as "Result_Type", count(*) as Count 
	from obs o 
    inner join concept_name cn on o.concept_id = cn.concept_id
    where cn.name = "%s" and value_text is not null and o.obs_datetime between '%s' and '%s' group by o.value_text

union all

select '' as "Malaria_Test", 'Total' as "Result_Type", count(*) as Count
	from obs o inner join concept_name cn on o.concept_id = cn.concept_id 
	where cn.name in ("%s","%s") and o.value_text is not null and o.obs_datetime between '%s' and '%s';