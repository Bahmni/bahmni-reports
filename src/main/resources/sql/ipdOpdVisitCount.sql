select 	ifnull(sum(if(value_reference='OPD' and date_started=date_created,1,0)),0) as New_OPD,
		ifnull(sum(if(value_reference='OPD' and date_started!=date_created,1,0)),0) as Old_OPD,
        ifnull(sum(if(value_reference='OPD',1,0)),0) as Total_OPD,
        ifnull(sum(if(value_reference='IPD' and date_started=date_created,1,0)),0) as New_IPD,
        ifnull(sum(if(value_reference='IPD' and date_started!=date_created,1,0)),0) as Old_IPD,
        ifnull(sum(if(value_reference='IPD',1,0)),0) as Total_IPD
        from
		(select v.visit_id, coalesce(va.value_reference, vt.name) as value_reference, p.person_id, date(v.date_started) as date_started, date(p.date_created) as date_created from visit v
			inner join person p on p.person_id=v.patient_id
			inner join visit_type vt on vt.visit_type_id=v.visit_type_id
			left join (select va1.visit_id, va1.value_reference from visit_attribute va1 inner join visit_attribute_type vat on vat.visit_attribute_type_id=va1.attribute_type_id and vat.name='Visit Status' where va1.voided=0) va on va.visit_id=v.visit_id
            where v.voided=0 and date(v.date_started) between '#startDate#' and '#endDate#') as raw_result;