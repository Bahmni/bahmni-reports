select cv.concept_full_name as Concept_Name, count(*) as Count from obs o
	inner join concept_view cv on o.concept_id = cv.concept_id and cv.concept_class_name in (#conceptClassNames#)
    where o.voided = 0 and date(o.obs_datetime) between '#startDate#' AND '#endDate#'
    group by Concept_Name
    order by Concept_Name;