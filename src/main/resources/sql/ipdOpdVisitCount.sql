select 	ifnull(sum(if(value_reference='OPD' and date_started=date_created,1,0)),0) as New_OPD,
		ifnull(sum(if(value_reference='OPD' and date_started!=date_created,1,0)),0) as Old_OPD,
        ifnull(sum(if(value_reference='OPD',1,0)),0) as Total_OPD,
        ifnull(sum(if(value_reference='IPD' and date_started=date_created,1,0)),0) as New_IPD,
        ifnull(sum(if(value_reference='IPD' and date_started!=date_created,1,0)),0) as Old_IPD,
        ifnull(sum(if(value_reference='IPD',1,0)),0) as Total_IPD
        from
		(select v.visit_id, va.value_reference, p.person_id, date(v.date_started) as date_started, date(p.date_created) as date_created from visit v
			inner join person p on p.person_id=v.patient_id
			inner join visit_attribute va on va.visit_id=v.visit_id
            where date(v.date_started) between '#startDate#' and '#endDate#') as raw_result;