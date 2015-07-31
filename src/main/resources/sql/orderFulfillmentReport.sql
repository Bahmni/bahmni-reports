select o.order_action,cn.name as Concept,ot.name as OrderType,o.date_activated OrderDate, pt.identifier as PatientID,
       CONCAT(pn.given_name, " ", pn.family_name) AS PatientName,
       p.gender as Gender,
       IF(orderobs.obs_datetime is null, "No","Yes") as FulfillmentStatus,
       orderobs.obs_datetime as FulfilmentDate
from orders o
  INNER JOIN concept_name cn on o.concept_id=cn.concept_id and cn.concept_name_type = 'FULLY_SPECIFIED'
  INNER join order_type ot on ot.order_type_id = o.order_type_id
  INNER JOIN patient_identifier pt on pt.patient_id=o.patient_id
  INNER JOIN person_name pn on pn.person_id=pt.patient_id
  INNER JOIN person p on p.person_id = pn.person_id
  LEFT JOIN (select o2.order_id
             from orders o1
               inner join orders o2 on o1.previous_order_id = o2.order_id) previousOrdersToIgnore on o.order_id = previousOrdersToIgnore.order_id
  LEFT JOIN (select order_id,min(obs_datetime) as obs_datetime
             from obs where order_id is not null
             group by order_id) orderobs on o.order_id = orderobs.order_id
where o.date_activated BETWEEN '#startDate#' and '#endDate#' and ot.name in (#orderTypes#) and o.voided=0 and previousOrdersToIgnore.order_id is null and o.order_action not in ('DISCONTINUE')
order by o.date_activated desc;