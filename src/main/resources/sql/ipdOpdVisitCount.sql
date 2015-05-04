select 	sum(if(value_reference='OPD' and date_started=date_created,1,0)) as New_OPD,
		sum(if(value_reference='OPD' and date_started!=date_created,1,0)) as Old_OPD,
        sum(if(value_reference='OPD',1,0)) as Total_OPD,
        sum(if(value_reference='IPD' and date_started=date_created,1,0)) as New_IPD,
        sum(if(value_reference='IPD' and date_started!=date_created,1,0)) as Old_IPD,
        sum(if(value_reference='IPD',1,0)) as Total_IPD
        from
		(select v.visit_id, va.value_reference, p.person_id, v.date_started, p.date_created from visit v
			inner join person p on p.person_id=v.patient_id
			inner join visit_attribute va on va.visit_id=v.visit_id
            where v.date_started between '%s' and '%s') as raw_result;