select 	test.identifier as patient_id,
		test.given_name as first_name,
        test.family_name as last_name,
        test.gender as gender,
        test.age as age,
        test.concept_full_name as test_name,
        concat(coalesce(test.value_text,''),coalesce(test.value_numeric,''),coalesce(test.value_coded,'')) as test_result,
        if(abnormal.value_coded = 1,'Abnormal','Normal') as abnormality
        from
		(select pi.identifier,
			pn.given_name, pn.family_name, p.gender,
			floor(datediff(o.obs_datetime,p.birthdate)/365) as age,
            cv.concept_full_name,
            o.obs_group_id,
            o.value_text,
            o.value_numeric,
            cv2.concept_full_name as value_coded
			from obs o
			inner join concept_view cv on cv.concept_id = o.concept_id
			left join concept_view cv2 on cv2.concept_id = o.value_coded
			inner join person p on p.person_id = o.person_id
			inner join person_name pn on pn.person_id = o.person_id
			inner join patient_identifier pi on pi.patient_id = o.person_id
			where cv.concept_full_name in (%s)
			and o.voided = 0
			and date(o.obs_datetime) between '%s' and '%s') test

        left join

        (select o.obs_group_id,o.value_coded
			from obs o
            inner join concept_view cv on cv.concept_id = o.concept_id
			where cv.concept_full_name = 'LAB_ABNORMAL' and o.value_coded in (if('abnormal' in (%s),1,2),if('normal' in (%s),2,1)) and o.voided = 0
				and date(o.obs_datetime) between '%s' and '%s') abnormal

        on test.obs_group_id = abnormal.obs_group_id
        -- Dont consider the obs (parent obs for lab results observation) when there is no test result for it
        where concat(coalesce(test.value_text,''),coalesce(test.value_numeric,''),coalesce(test.value_coded,'')) != ''
        group by patient_id,test_name,test_result
        order by patient_id;