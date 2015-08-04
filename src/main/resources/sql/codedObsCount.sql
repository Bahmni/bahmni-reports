SELECT *
FROM (SELECT
        base.age_group                                                AS age_group,
        base.sort_order                                               AS sort_order,
        base.value_reference                                          AS visit_type,
        base.answer_concept_name                                      AS answer_concept_name,
        base.concept_name                                             AS concept_name,
        final.concept_id,
        final.female                                                  AS female,
        final.male                                                    AS male,
        final.other                                                   AS other,
        final.male + final.other + final.female                       AS total
      FROM
        (SELECT
           rag.name                   AS age_group,
           rag.sort_order,
           answer.concept_id          AS answer_concept_id,
           IF(answer.concept_short_name IS NOT NULL, answer.concept_short_name, answer.concept_full_name) AS answer_concept_name,
           question.concept_id        AS concept_id,
           IF(question.concept_short_name IS NOT NULL, question.concept_short_name, question.concept_full_name) AS concept_name,
           visit_type.visit_type_name AS value_reference
         FROM reporting_age_group rag
           JOIN concept_view question ON question.concept_full_name IN (#conceptNames#)
           JOIN concept_answer ON concept_answer.concept_id = question.concept_id
           INNER JOIN concept_view answer ON concept_answer.answer_concept = answer.concept_id
           JOIN (SELECT DISTINCT value_reference AS visit_type_name
                 FROM visit_attribute va
                 WHERE va.visit_attribute_id IS NOT NULL #visitFilter#) visit_type
         WHERE rag.report_group_name = "#reportGroupName#"
        ) base
        LEFT OUTER JOIN
        (SELECT
           concept_id,
           answer_concept_id,
           age_group,
           visit_type,
           sum(female) AS female,
           sum(male)   AS male,
           sum(other)  AS other,
           sort_order
         FROM (SELECT
                 question.concept_id           AS concept_id,
                 answer.concept_id             AS answer_concept_id,
                 rag.name                      AS age_group,
                 va.value_reference            AS visit_type,
                 #countOncePerPatientClause#(IF(p.gender = 'F', 1, 0)) AS female,
                 #countOncePerPatientClause#(IF(p.gender = 'M', 1, 0)) AS male,
                 #countOncePerPatientClause#(IF(p.gender = 'O', 1, 0)) AS other,
                 rag.sort_order                AS sort_order
               FROM obs obs
                 INNER JOIN concept_view question ON obs.concept_id = question.concept_id
                 INNER JOIN concept_view answer ON obs.value_coded = answer.concept_id
                 INNER JOIN encounter e ON obs.encounter_id = e.encounter_id
                 INNER JOIN visit v ON v.visit_id = e.visit_id
                 INNER JOIN visit_attribute va ON va.visit_id = v.visit_id
                 INNER JOIN visit_attribute_type vat ON vat.visit_attribute_type_id = va.attribute_type_id AND vat.name = 'Visit Status'
                 INNER JOIN person p ON p.person_id = obs.person_id
                 INNER JOIN reporting_age_group rag ON DATE(#endDateField#) BETWEEN (DATE_ADD(
                     DATE_ADD(birthdate, INTERVAL rag.min_years YEAR), INTERVAL rag.min_days DAY)) AND (DATE_ADD(
                     DATE_ADD(birthdate, INTERVAL rag.max_years YEAR), INTERVAL rag.max_days DAY))
                                                       AND rag.report_group_name = "#reportGroupName#"
               WHERE
                 question.concept_full_name IN (#conceptNames#)
                 AND cast(#endDateField# AS DATE) BETWEEN '#startDate#' AND '#endDate#'
                 AND obs.voided IS FALSE
               GROUP BY question.concept_id, answer.concept_id, va.value_reference, rag.name, rag.sort_order, p.person_id) interim_result
         GROUP BY concept_id, age_group, visit_type, answer_concept_id
         ORDER BY concept_id, interim_result.sort_order
        ) final ON final.age_group = base.age_group AND final.concept_id = base.concept_id AND final.visit_type = base.value_reference AND final.answer_concept_id = base.answer_concept_id
      ORDER BY base.sort_order ASC) AS result;
