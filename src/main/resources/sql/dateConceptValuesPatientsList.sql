SELECT t1.identifier, t1.given_name, t1.family_name, t1.gender, t1.age, DATE(t2.value_datetime) AS date_value
FROM
	(SELECT o.person_id, o.encounter_id, pi.identifier, p.gender,floor(datediff(o.obs_datetime,p.birthdate)/365) as age,
		pn.given_name, pn.family_name
		FROM obs o
		INNER JOIN concept_view cv ON o.concept_id = cv.concept_id
        INNER JOIN person p on o.person_id = p.person_id
        INNER JOIN person_name pn ON o.person_id = pn.person_id
		INNER JOIN patient_identifier pi on o.person_id = pi.patient_id and pi.preferred = 1
		AND cv.concept_full_name = '#templateName#') t1

	INNER JOIN

	(SELECT cv.concept_full_name, o.value_datetime, o.person_id, o.encounter_id, o.obs_datetime, o.obs_group_id
		FROM obs o INNER JOIN concept_view cv on o.concept_id = cv.concept_id
        AND cv.concept_full_name = #conceptNames#
		WHERE date(o.value_datetime) BETWEEN '#startDate#' AND '#endDate#' and o.voided = 0) t2

ON t1.person_id = t2.person_id AND t1.encounter_id = t2.encounter_id
GROUP BY t1.identifier, date_value
ORDER BY t1.identifier, date_value;