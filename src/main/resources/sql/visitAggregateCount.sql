SELECT
  vt.name AS visit_type,
  sum(if(date(va.date_created) BETWEEN "#startDate#" AND "#endDate#", 1, 0)) AS admitted,
  sum(if((date(va.date_changed) BETWEEN "#startDate#" AND "#endDate#") AND va.value_reference = 'Discharged', 1, 0)) AS discharged
FROM visit_attribute va
  INNER JOIN visit_attribute_type vat ON va.attribute_type_id = vat.visit_attribute_type_id
  INNER JOIN (select DISTINCT (visit.visit_id),visit.voided,visit.visit_type_id from visit
    INNER JOIN encounter e on e.visit_id=visit.visit_id
    #countOnlyTaggedLocationsJoin#
             ) as v on va.visit_id=v.visit_id
  INNER JOIN visit_type vt ON vt.visit_type_id = v.visit_type_id
WHERE vat.name = 'Admission Status'
      AND (date(va.date_created) BETWEEN "#startDate#" AND "#endDate#" OR
           date(va.date_changed) BETWEEN "#startDate#" AND "#endDate#")
      AND va.voided = 0 AND v.voided = 0
      AND vt.name IN (#visitTypes#)
GROUP BY vt.name;