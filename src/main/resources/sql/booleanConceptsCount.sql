-- noinspection SqlNoDataSourceInspection
SELECT *
FROM (SELECT
        base.age_group,
        person_id,
        concept_id,
        base.concept_name,
        base.value_boolean,
        female,
        male,
        other,
        result.encounter_id,
        male+other+female AS total
      FROM
        (SELECT
           rag.name AS age_group,
           cn.name  AS concept_name,
           bool.value_boolean
         FROM reporting_age_group rag, concept_name cn, (SELECT 'True' AS value_boolean
                                                         UNION SELECT 'False' AS value_boolean) bool
         WHERE rag.report_group_name = '#ageGroupName#' AND cn.name IN (#conceptNames#)) base
        LEFT OUTER JOIN
        (SELECT
           cn.name,
           cbvn.name                AS value_boolean,
           IF(p.gender = 'F', 1, 0) AS female,
           IF(p.gender = 'M', 1, 0) AS male,
           IF(p.gender = 'O', 1, 0) AS other,
           rag.name                 AS rag_age_group,
           p.person_id,
           obs.concept_id,
           #endDateField# datetime,
           encounter.encounter_id
         FROM obs obs
           INNER JOIN concept c ON obs.concept_id = c.concept_id AND obs.voided = 0
           INNER JOIN concept_name cn
             ON cn.concept_id = c.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.name IN (#conceptNames#)
           INNER JOIN concept_datatype cd ON c.datatype_id = cd.concept_datatype_id AND cd.name = 'Boolean'
           INNER JOIN concept_name cbvn ON obs.value_coded = cbvn.concept_id AND cbvn.name IN ('True', 'False')
           INNER JOIN person p ON p.person_id = obs.person_id
           INNER JOIN encounter encounter ON encounter.encounter_id = obs.encounter_id
           INNER JOIN visit v ON encounter.visit_id = v.visit_id
           INNER JOIN reporting_age_group rag ON #endDateField# BETWEEN (DATE_ADD(
               DATE_ADD(p.birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY))
                                                 AND (DATE_ADD(DATE_ADD(p.birthdate, INTERVAL rag.max_years YEAR),
                                                               INTERVAL rag.max_days DAY))
                                                 AND rag.report_group_name = '#ageGroupName#'
         WHERE cast(#endDateField# AS DATE) BETWEEN '#startDate#' AND '#endDate#'
               AND obs.voided IS FALSE) result
          ON base.age_group = result.rag_age_group
             AND base.concept_name = result.name
             AND
             result.value_boolean = base.value_boolean
      ORDER BY result.datetime DESC) AS result_data