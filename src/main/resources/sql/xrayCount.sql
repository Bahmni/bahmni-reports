select xray.concept_full_name as xray_type, count(*) as count from
(select o.obs_id, cv.concept_full_name from obs o
	inner join concept_view cv on o.concept_id = cv.concept_id and cv.concept_class_name = 'Radiology'
    where o.obs_datetime between '%s' and '%s'
	having o.obs_id in
    (select o.obs_group_id from obs o inner join concept_view cv on o.concept_id = cv.concept_id and cv.concept_full_name = 'Document')) xray
    group by xray.concept_full_name
    order by xray.concept_full_name;
