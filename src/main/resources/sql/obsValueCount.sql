select cv1.concept_full_name as "Name",
	concat(coalesce(o.value_text,''),coalesce(o.value_numeric,''),coalesce(cv2.concept_short_name,cv2.concept_full_name,''),coalesce(date(o.value_datetime),'')) as "Value",
	count(*) as "Count"
	from obs o
    inner join concept_view cv1 on o.concept_id = cv1.concept_id
    left join concept_view cv2 on cv2.concept_id = o.value_coded
	where cv1.concept_full_name in (#conceptNames#) and
    (o.value_text is not null or o.value_numeric is not null or o.value_coded is not null or o.value_datetime is not null) and o.voided = 0
	and date(o.obs_datetime) between '#startDate#' and '#endDate#'
	group by Name,Value
	order by Name,Value;