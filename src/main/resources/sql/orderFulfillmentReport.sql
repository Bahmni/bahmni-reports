SELECT
  o.order_action,
  o.order_id,
  cn.name                                        AS Concept,
  ot.name                                        AS OrderType,
  o.date_activated                                  OrderDate,
  pt.identifier                                  AS PatientID,
  CONCAT(pn.given_name, " ", ifnull(pn.family_name,""))     AS PatientName,
  p.gender                                       AS Gender,
  min(associatedObs.obs_datetime)                 AS FulfilmentDate,
  IF(associatedObs.obs_datetime IS NULL, "No", "Yes") AS FulfillmentStatus
FROM orders o
  INNER JOIN concept_name cn ON o.concept_id = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED'
  INNER JOIN order_type ot ON ot.order_type_id = o.order_type_id
  INNER JOIN patient_identifier pt ON pt.patient_id = o.patient_id AND pt.preferred = 1
  INNER JOIN person_name pn ON pn.person_id = pt.patient_id
  INNER JOIN person p ON p.person_id = pn.person_id
  LEFT JOIN (select o2.order_id
              from orders o1
                inner join orders o2 on o1.previous_order_id = o2.order_id) previousOrdersToIgnore on o.order_id = previousOrdersToIgnore.order_id
  LEFT JOIN obs associatedObs on o.order_id = associatedObs.order_id
WHERE DATE(o.date_activated) BETWEEN '#startDate#' AND '#endDate#' AND ot.name IN (#orderTypes#) AND o.voided = 0 AND
      o.order_action NOT IN ('DISCONTINUE') and previousOrdersToIgnore.order_id is null
GROUP BY o.order_id
ORDER BY OrderType,FulfillmentStatus,OrderDate,Concept;