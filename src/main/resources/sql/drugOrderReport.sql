SELECT
  p.patientID   AS patientId,
  p.patientName AS patientName,
  p.gender      AS gender,
  p.age         AS age,
  p.user        AS user,
  p.name        AS drugName,
  p.dose        AS dose,
  p.units       AS unit,
  p.frequency   AS frequency,
  p.duration    AS duration,
  p.route       AS route,
  p.startDate   AS startDate,
  p.stopDate    AS stopDate,
  p.quantity    AS quantity
FROM
  (SELECT
     patient_identifier.identifier                                                                     AS patientID,
     concat(person_name.given_name, ' ', ifnull(person_name.family_name,''))                                      AS patientName,
     person.gender                                                                                     AS gender,
     floor(datediff(CURDATE(), person.birthdate) / 365)                                                AS age,
     concat(pro.given_name, ' ', ifnull(pro.family_name,''))                                                      AS user,
     IF(drug_order.drug_non_coded IS NULL, drug.name, drug_order.drug_non_coded)                       AS name,
     IF(drug_order.dose IS NULL, drug_order.dosing_instructions, drug_order.dose)                      AS dose,
     dcn.concept_full_name                                                                             AS units,
     fre.concept_full_name                                                                             AS frequency,
     concat(drug_order.duration, ' ', du.concept_full_name)                                            AS duration,
     rou.concept_full_name                                                                             AS route,
     IF(Date(orders.scheduled_date) IS NULL, Date(orders.date_activated), Date(orders.scheduled_date)) AS startDate,
     IF(Date(orders.date_stopped) IS NULL, Date(orders.auto_expire_date), Date(orders.date_stopped))   AS stopDate,
     concat(drug_order.quantity, ' ', tqu.concept_full_name)                                           AS quantity
   FROM drug_order
     LEFT JOIN orders ON orders.order_id = drug_order.order_id AND orders.order_action != "DISCONTINUE"
     LEFT JOIN drug ON drug.drug_id = drug_order.drug_inventory_id
     LEFT JOIN concept_view dcn ON dcn.concept_id = drug_order.dose_units
     LEFT JOIN concept_view tqu ON tqu.concept_id = drug_order.quantity_units
     LEFT JOIN concept_view rou ON rou.concept_id = drug_order.route
     LEFT JOIN concept_view du ON du.concept_id = drug_order.duration_units
     LEFT JOIN order_frequency ON order_frequency.order_frequency_id = drug_order.frequency
     LEFT JOIN concept_view fre ON order_frequency.concept_id = fre.concept_id
     LEFT JOIN person_name ON person_name.person_id = orders.patient_id
     LEFT JOIN person ON person.person_id = orders.patient_id
     LEFT JOIN patient_identifier ON patient_identifier.patient_id = orders.patient_id AND patient_identifier.preferred = 1
     LEFT JOIN encounter_provider ON encounter_provider.encounter_id = orders.encounter_id
     LEFT JOIN provider ON provider.provider_id = encounter_provider.provider_id
     LEFT JOIN person_name pro ON pro.person_id = provider.person_id
  ) p
WHERE
  p.startDate <= "#endDate#"
  AND
  (p.stopDate >= "#startDate#" OR p.stopDate IS NULL)
ORDER BY patientId;
