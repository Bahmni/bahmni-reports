SET SESSION group_concat_max_len = 20000;
SET @sql = NULL;

SELECT GROUP_CONCAT(DISTINCT
                    CONCAT(
                        'GROUP_CONCAT(DISTINCT(IF(pat.person_attribute_type_id = ''',
                        person_attribute_type_id,
                        ''', o.attr_value, NULL)) SEPARATOR \',\') AS `',
                        description, '`'
                    )
)
INTO @sql
FROM person_attribute_type pat;

SET @sql = CONCAT('SELECT
  o.identifier as \'Patient ID\',
  o.given_name as \'First Name\',
  o.family_name as \'Family Name\',
  o.age as \'Age\',
  o.gender as \'Gender\',
  o.birthdate as \'Birth Date\',
  DATE(o.death_date) as \'Death Date\',
  o.cause_of_death as \'Cause Of Death\',
  o.address1 as Address1,
  o.address2 as Address2,
  o.address3 as Address3,
  o.city_village as `City/Village`,
  o.state_province as `State/Province`,
  o.county_district as `County/District`,
  o.date_created as \'Registration Date\',',
  @sql,
  'FROM
     (SELECT
        pi.identifier,
        pn.given_name,
        pn.family_name,
        floor(datediff(CURDATE(), p.birthdate) / 365) AS age,
        p.gender,
        p.birthdate,
        p.death_date,
        p.cause_of_death,
        addr.address1,
        addr.address2,
        addr.address3,
        addr.city_village,
        addr.state_province,
        addr.county_district,
        pa.date_created,
        attr.person_attribute_type_id,
        coalesce(person_attribute_cn.concept_short_name, person_attribute_cn.concept_full_name,
                  IF(attr_type.format LIKE "%Boolean%", IF(attr.value="true", "Yes", null) ,attr.value)) as attr_value,
        p.person_id
        FROM  person p
       JOIN patient pa ON p.person_id = pa.patient_id and cast(pa.date_created AS DATE) BETWEEN \'#startDate#\' AND \'#endDate#\'
       JOIN person_name pn ON p.person_id = pn.person_id
       JOIN patient_identifier pi ON pa.patient_id = pi.patient_id AND pi.preferred = 1
       LEFT OUTER JOIN person_address addr ON p.person_id = addr.person_id
       LEFT OUTER JOIN person_attribute attr ON p.person_id = attr.person_id and attr.voided = false
       LEFT OUTER JOIN person_attribute_type attr_type ON attr.person_attribute_type_id = attr_type.person_attribute_type_id
       LEFT JOIN concept_view person_attribute_cn ON attr.value = person_attribute_cn.concept_id AND attr_type.format LIKE "%Concept") o
     LEFT OUTER JOIN person_attribute_type pat ON o.person_attribute_type_id = pat.person_attribute_type_id
       group by person_id');


PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;