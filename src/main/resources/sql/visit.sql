set session group_concat_max_len = 20000;
SET @sql = NULL;
SET @person_attributes = NULL;
SET @visit_attributes = NULL;

SELECT
  GROUP_CONCAT(
      CONCAT('MAX(IF(person_attribute_type.name = \'', name, '\', IFNULL(concept_name.name, person_attribute.value), NULL)) as \'', name, '\''))
into @person_attributes
FROM person_attribute_type where name in (#personAttributes#);

SELECT
  GROUP_CONCAT(
      CONCAT('MAX(IF(visit_attribute_type.name = \'', name, '\', visit_attribute.value_reference , NULL)) as \'', name, '\''))
into @visit_attributes
FROM visit_attribute_type where name in (#visitAttributes#);


SET @sql = CONCAT('SELECT pi.identifier, CONCAT(pn.given_name, " ", ifnull(pn.family_name,"")) AS "Patient Name", p.gender as Gender,'
, @visit_attributes, ',', @person_attributes,
',date(v.date_started) as date_started
  from visit v
  inner join person p on p.person_id=v.patient_id
  inner join patient pa ON p.person_id = pa.patient_id
  inner join person_name pn ON p.person_id = pn.person_id
  inner join patient_identifier pi ON pa.patient_id = pi.patient_id  AND pi.preferred = 1
  left outer join visit_attribute on visit_attribute.visit_id=v.visit_id
  left outer JOIN visit_attribute_type on visit_attribute_type.visit_attribute_type_id = visit_attribute.attribute_type_id
  left outer join person_attribute on p.person_id = person_attribute.person_id and person_attribute.voided is false
  left outer join person_attribute_type person_attribute_type on person_attribute.person_attribute_type_id = person_attribute_type.person_attribute_type_id
  left outer join concept_name
  on person_attribute_type.format = "org.openmrs.Concept" and person_attribute.value = concept_name.concept_id
    and concept_name.concept_name_type = "SHORT" and concept_name.voided is false
  where date(v.date_started) between "#startDate#" AND "#endDate#" GROUP BY v.visit_id;');


PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

