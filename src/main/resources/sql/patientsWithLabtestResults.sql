select result.identifier as patient_id,
	result.given_name as first_name,
	result.family_name as last_name,
	result.gender as gender,
	result.age as age,
    result.test_date as test_date,
    result.concept_full_name as test_name,
    result.test_result as test_result,
    result.test_outcome as test_outcome
	from 
	(select test.identifier, test.given_name, test.family_name, test.gender, test.age, date(test.obs_datetime) as test_date, test.concept_full_name,
        concat(coalesce(test.value_text,''),coalesce(test.value_numeric,''),coalesce(test.value_coded,'')) as test_result,
        if(abnormal.value_coded = 1,'Abnormal','Normal') as test_outcome
        from
		(select pi.identifier,
			pn.given_name, pn.family_name, p.gender,
			floor(datediff(o.obs_datetime,p.birthdate)/365) as age,
            cv.concept_full_name,
            o.obs_group_id,
            o.value_text,
            o.value_numeric,
            cv2.concept_full_name as value_coded,
            o.obs_datetime,
            o.person_id
			from obs o
			inner join concept_view cv on cv.concept_id = o.concept_id
			left join concept_view cv2 on cv2.concept_id = o.value_coded
			inner join person p on p.person_id = o.person_id
			inner join person_name pn on pn.person_id = o.person_id
			inner join patient_identifier pi on pi.patient_id = o.person_id AND pi.preferred = 1
			where cv.concept_full_name in (#conceptNames#)
			and o.voided = 0
			and date(o.obs_datetime) between '#startDate#' and '#endDate#') test

        left join

        (select o.obs_id, o.obs_group_id,o.value_coded,cv.concept_full_name
			from obs o
            inner join concept_view cv on cv.concept_id = o.concept_id
			where cv.concept_full_name = 'LAB_ABNORMAL' and o.voided = 0
				and date(o.obs_datetime) between '#startDate#' and '#endDate#') abnormal

        on test.obs_group_id = abnormal.obs_group_id
        -- Dont consider the obs (parent obs for lab results observation) when there is no test result for it
        where concat(coalesce(test.value_text,''),coalesce(test.value_numeric,''),coalesce(test.value_coded,'')) != '') result
        where result.test_outcome in (#testOutcome#)
        group by patient_id,test_name,test_result
        order by test_outcome,patient_id;