select base.numeric_value_range, base.report_age_group,
		ifnull(sum(IF(base.gender = 'F', 1, 0)),0) AS female,
		ifnull(sum(IF(base.gender = 'M', 1, 0)),0) AS male,
        ifnull(sum(IF(base.gender = 'O', 1, 0)),0) AS other,
        ifnull(sum(IF(base.gender in ('F','M','O'), 1, 0)),0) AS total
	from
	(select p.person_id as person_id, rcr.name as numeric_value_range, cn.concept_id, rcr.sort_order as rcr_sort_order, rag.sort_order as rag_sort_order, o.value_numeric, rag.name as report_age_group, p.birthdate, if(#countOncePerPatient#, max(o.obs_datetime), o.obs_datetime) as obs_date, p.gender from obs o
		inner join concept_name cn on o.concept_id=cn.concept_id
		inner join person p on p.person_id = o.person_id
        inner join reporting_concept_range rcr on (rcr.low_value <= o.value_numeric and o.value_numeric < rcr.high_value)
			and rcr.concept_name = '#rangeGroupName#'
        inner join reporting_age_group rag ON DATE(o.obs_datetime) BETWEEN (DATE_ADD(DATE_ADD(birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY))
			AND (DATE_ADD(DATE_ADD(birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
		and rag.report_group_name = '#ageGroupName#'
		where cn.name in (#conceptNames#) and cn.concept_name_type="FULLY_SPECIFIED" and o.value_numeric is not null
			and o.voided = 0
		and date(o.obs_datetime) between '#startDate#' and '#endDate#'
    group by if(#countOncePerPatient#, o.person_id, concat(o.person_id, o.obs_datetime))) base
		group by base.report_age_group, base.numeric_value_range
    order by base.rag_sort_order, base.rcr_sort_order;