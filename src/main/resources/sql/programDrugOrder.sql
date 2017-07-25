set session group_concat_max_len = 20000;
SET @sql = NULL;

SET @patientAttributesSql = '#patientAttributes#';

SET @programAttributesSql = '#programAttributes#';

SET @programNamesSql = '#programNamesInClauseEscapeQuote#';

SET @medicationAsNeeded = '"(PRN)"';

SET @patientAttributesJoinSql = ' LEFT JOIN person_attribute pa  on pp.patient_id= pa.person_id
                          LEFT JOIN person_attribute_type prat on prat.person_attribute_type_id = pa.person_attribute_type_id and prat.name in (#patientAttributesInClauseEscapeQuote#)
                          LEFT JOIN concept_view person_attribute_cn ON pa.value = person_attribute_cn.concept_id AND prat.format LIKE "%Concept%"';

SET @patientAttributesSelectClause = 'prat.name  as patient_attribute_name,
                       coalesce(person_attribute_cn.concept_short_name, person_attribute_cn.concept_full_name, pa.value) as patient_attribute_value,';


SET @programAttributesJoinSql= ' LEFT JOIN patient_program_attribute pg_attr ON pp.patient_program_id = pg_attr.patient_program_id
      LEFT JOIN program_attribute_type pg_attr_type ON pg_attr.attribute_type_id = pg_attr_type.program_attribute_type_id and pg_attr_type.name in (#programAttributesInClauseEscapeQuote#)
      LEFT JOIN concept_view pg_attr_cn ON pg_attr.value_reference = pg_attr_cn.concept_id AND pg_attr_type.datatype LIKE "%Concept%"';


SET @programNamesConditionSql = ' AND (prog.name in (#programNamesInClauseEscapeQuote#))';

SET @programAttributesSelectClause = 'pg_attr_type.name  as program_attribute_name,
                       coalesce(pg_attr_cn.concept_short_name, pg_attr_cn.concept_full_name, pg_attr.value_reference) as program_attribute_value,';

SET @programAttributesTypejoin = ' LEFT OUTER JOIN program_attribute_type pat ON o.attribute_type_id = pat.program_attribute_type_id ';

SET @patientAttributesTypejoin = ' LEFT OUTER JOIN person_attribute_type prat ON o.person_attribute_type_id = prat.person_attribute_type_id  ';



SET @sql = CONCAT('SELECT
  o.identifier as patientId,
  o.patient_name as patientName,
  o.age as age,
  o.gender as gender,
  o.programName,
  o.drugName,
  IF(o.as_needed = 1, CONCAT(o.dose,', @medicationAsNeeded ,'), o.dose) as dose,
  o.units as unit,
  o.route,
  o.frequency,
  o.duration,
  o.quantity,
  o.startDate,',
  IF(@patientAttributesSql = '', '', CONCAT(@patientAttributesSql, ',')),
  IF(@programAttributesSql = '', '', CONCAT(@programAttributesSql, ',')),'
  o.stopDate
   FROM
                     (SELECT
                        pi.identifier,
                        CONCAT(pn.given_name, "" "", ifnull(pn.family_name,"")) as patient_name,
                        floor(datediff(CURDATE(), p.birthdate) / 365) AS age,
                        p.gender,
                        IF(drug.name is NULL,drug_order.drug_non_coded, drug.name) as drugName,
                        IF(drug_order.dose IS NULL , drug_order.dosing_instructions, drug_order.dose) AS dose,
                        drug_order.as_needed,
                        dcn.concept_full_name as units,
                        rou.concept_full_name as route,
                        fre.concept_full_name as frequency,
                        concat(drug_order.duration ,'' '', du.concept_full_name) as duration,
                         IF(orders.auto_expire_date IS NULL,'' '' ,concat(drug_order.quantity, qu.concept_full_name)) as quantity,
                        IF(Date(orders.scheduled_date) IS NULL, orders.date_activated, orders.scheduled_date) as startDate,
                        IF(Date(orders.date_stopped) is NULL, orders.auto_expire_date,orders.date_stopped) as stopDate,',
                        IF(@patientAttributesSql = '', '', @patientAttributesSelectClause),
                        IF(@programAttributesSql = '', '', @programAttributesSelectClause),'
                        pp.patient_program_id,
                        prog.name as programName,'
                         ,IF(@programAttributesSql = '','','pg_attr.attribute_type_id,'),'
                        pp.patient_id,'
                        ,IF(@patientAttributesSql = '','','prat.person_attribute_type_id,'),'
                       prog.program_id,
                       orders.order_id,
                       pp.date_enrolled
                        FROM  patient_program pp
                        JOIN program prog ON pp.program_id = prog.program_id and pp.voided = 0'
                         ,IF(@programNamesSql = '', '', @programNamesConditionSql),'

                       JOIN person p ON pp.patient_id = p.person_id
                       JOIN person_name pn ON p.person_id = pn.person_id
                       JOIN patient pt ON pp.patient_id = pt.patient_id
                       JOIN patient_identifier pi ON pt.patient_id = pi.patient_id and pi.preferred = 1',
                         IF(@patientAttributesSql = '', '', @patientAttributesJoinSql),
                         IF(@programAttributesSql = '', '', @programAttributesJoinSql),'

                          JOIN  episode_patient_program epp on pp.patient_program_id = epp.patient_program_id
                          JOIN episode_encounter ee on ee.episode_id = epp.episode_id
                          JOIN orders orders ON orders.patient_id = pp.patient_id and orders.encounter_id = ee.encounter_id and orders.voided = 0 and (orders.order_action) != "DISCONTINUE"
                          LEFT JOIN drug_order drug_order ON drug_order.order_id = orders.order_id
                          LEFT JOIN drug ON drug.concept_id = orders.concept_id
                          LEFT JOIN concept_view dcn  ON dcn.concept_id = drug_order.dose_units
                          LEFT JOIN concept_view qu ON qu.concept_id = drug_order.quantity_units
                          LEFT JOIN concept_view rou  ON rou.concept_id = drug_order.route
                          LEFT JOIN concept_view du  ON du.concept_id = drug_order.duration_units
                          LEFT JOIN order_frequency ON order_frequency.order_frequency_id = drug_order.frequency
                          LEFT JOIN concept_view fre  ON order_frequency.concept_id = fre.concept_id
                     WHERE (
                       (
                      (cast(pp.date_enrolled AS DATE) <=  \'#endDate#\')
                      AND (cast(pp.date_completed AS DATE) >= \'#startDate#\' OR  pp.date_completed is NULL)
                     ))) o',
                      IF(@patientAttributesSql = '','',@patientAttributesTypeJoin),
                      IF(@programAttributesSql = '','',@programAttributesTypeJoin), '
                     GROUP BY patient_id, program_id, order_id
                     ORDER BY patient_id, date_enrolled');


PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;





