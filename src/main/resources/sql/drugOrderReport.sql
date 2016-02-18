select  p.patientID as patientId,
        p.patientName as patientName,
        p.gender as  gender,
        p.age as age,
        p.user as user,
        p.name as drugName,
        p.dose as dose,
        p.units as unit,
        p.frequency as frequency,
        p.duration as duration,
        p.route as route,
        p.startDate as startDate,
        p.stopDate as stopDate,
        p.quantity as quantity from
  (select person.gender,floor(datediff(CURDATE(), person.birthdate) / 365) AS age,IF(drug_order.dose IS NULL , drug_order.dosing_instructions, drug_order.dose) AS dose, dcn.concept_full_name as units,rou.concept_full_name as route,drug_order.dose_units,fre.concept_full_name as frequency,
                        concat(drug_order.duration ,' ', du.concept_full_name) as duration,concat(drug_order.quantity,' ', dcn.concept_full_name) as quantity, orders.patient_id, IF(Date(orders.scheduled_date) IS NULL, orders.date_activated, orders.scheduled_date) as startDate,orders.concept_id,
                        concat(person_name.given_name, ' ', person_name.family_name) as patientName,
                        provider.name as user,
                        patient_identifier.identifier as patientID,
                        IF(Date(orders.date_stopped) is NULL, orders.auto_expire_date,orders.date_stopped) as stopDate,
                        IF(drug_order.drug_non_coded is NULL,drug.name,drug_order.drug_non_coded) as name from drug_order
    LEFT JOIN  orders  ON orders.order_id = drug_order.order_id
    LEFT JOIN drug ON drug.drug_id = drug_order.drug_inventory_id
    LEFT JOIN concept_view dcn  ON dcn.concept_id = drug_order.dose_units
    LEFT JOIN concept_view rou  ON rou.concept_id = drug_order.route
    LEFT JOIN concept_view du  ON du.concept_id = drug_order.duration_units
    LEFT JOIN order_frequency ON order_frequency.order_frequency_id = drug_order.frequency
    LEFT JOIN concept_view fre  ON order_frequency.concept_id = fre.concept_id
    LEFT JOIN person_name ON person_name.person_id = orders.patient_id
    LEFT JOIN person ON person.person_id = orders.patient_id
    LEFT JOIN patient_identifier ON patient_identifier.patient_id = orders.patient_id
    LEFT JOIN encounter_provider ON encounter_provider.encounter_id = orders.encounter_id
    LEFT JOIN provider ON provider.provider_id = encounter_provider.provider_id
   where
     (
       IF(Date(orders.scheduled_date) IS NULL , orders.date_activated, orders.scheduled_date ) < "#endDate#"
       AND
       ((orders.auto_expire_date > "#startDate#"
         OR
         orders.date_stopped > "#startDate#"
         OR
         (Date(orders.date_stopped) IS NULL) AND (Date(orders.auto_expire_date) IS NULL)
       ))
       AND
       (orders.order_action) != "DISCONTINUE"
     ))p ORDER BY p.patientID;