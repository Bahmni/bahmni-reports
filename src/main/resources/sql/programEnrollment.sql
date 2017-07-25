SET SESSION group_concat_max_len = 20000;
SET @sql = NULL;

SELECT GROUP_CONCAT(DISTINCT
                    CONCAT(
                        'MAX(IF(pat.program_attribute_type_id = ''',
                        program_attribute_type_id,
                        ''', IF( pat.datatype LIKE "%Concept%"
                         , o.concept_name ,o.attr_value), NULL)) AS `',
                        description, '`'
                    )
)
INTO @sql
FROM program_attribute_type pat;

SET @sql = CONCAT('SELECT
  o.identifier as \'Patient ID\',
  o.patient_name as \'Patient Name\',
  o.age as \'Age\',
  o.gender as \'Gender\',
  o.program_name as \'Program Name\',
  date(o.date_enrolled) as \'Program Start Date\',
  date(o.date_completed) as \'Program Stop Date\',',
  @sql,
  'FROM
     (SELECT
        pi.identifier,
        CONCAT(pn.given_name, " ", ifnull(pn.family_name,"")) as patient_name,
        floor(datediff(CURDATE(), p.birthdate) / 365) AS age,
        p.gender,
        prog.name as program_name,
        attr.attribute_type_id,
        attr.value_reference as attr_value,
        pp.date_enrolled as date_enrolled,
        pp.date_completed as date_completed,
        pp.patient_id,
        prog.program_id,
        cn.name as concept_name,
        pp.patient_program_id
        FROM  patient_program pp
        JOIN program prog ON pp.program_id = prog.program_id and pp.voided = 0
        and(
            (cast(pp.date_enrolled AS DATE) <=  "#endDate#")
             AND (cast(pp.date_completed AS DATE) >= "#startDate#" OR  pp.date_completed is NULL)
          )
       JOIN person p ON pp.patient_id = p.person_id
       JOIN person_name pn ON p.person_id = pn.person_id
       JOIN patient pa ON pp.patient_id = pa.patient_id
       JOIN patient_identifier pi ON pa.patient_id = pi.patient_id and pi.preferred = 1
       LEFT OUTER JOIN patient_program_attribute attr ON pp.patient_program_id = attr.patient_program_id
       LEFT OUTER JOIN program_attribute_type attr_type ON attr.attribute_type_id = attr_type.program_attribute_type_id
       LEFT OUTER JOIN concept_name cn ON cn.concept_id = attr.value_reference
       ) o
     LEFT OUTER JOIN program_attribute_type pat ON o.attribute_type_id = pat.program_attribute_type_id
     GROUP BY patient_id, patient_program_id
     ORDER BY patient_id, date_enrolled
     ');



PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;