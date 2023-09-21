SELECT
  cn.name AS "Diagnosis",
  crt.code AS "Terminology Code",
  sum(diagnosisObs.female) AS "Female",
  sum(diagnosisObs.male) AS "Male",
  sum(diagnosisObs.other) AS "Other",
  sum(diagnosisObs.undisclosed) AS "Not disclosed",
  sum(diagnosisObs.female) + sum(diagnosisObs.male) + sum(diagnosisObs.other) + sum(diagnosisObs.undisclosed) AS "Total"
from
  (
    (
      SELECT
        diagnosis.value_coded AS value_coded,
        SUM(
          IF(person.gender = 'F', 1, 0)
        ) AS female,
        SUM(
          IF(person.gender = 'M', 1, 0)
        ) AS male,
        SUM(
          IF(person.gender = 'O', 1, 0)
        ) AS other,
        SUM(
          IF(person.gender = 'U', 1, 0)
        ) AS undisclosed,
        diagnosis.obs_datetime
      from
        obs AS diagnosis
        JOIN person on diagnosis.person_id = person.person_id
        AND person.voided = FALSE
        JOIN concept_view AS cv ON cv.concept_id = diagnosis.value_coded
        AND cv.concept_class_name = 'Diagnosis'
        AND cast(
          CONVERT_TZ(
            diagnosis.obs_datetime, '+00:00',
            '+5:30'
          ) AS DATE
        ) BETWEEN '#startDate#'
        AND '#endDate#'
        AND diagnosis.voided = 0
        AND diagnosis.obs_group_id IN (
          SELECT
            DISTINCT certaintyObs.obs_id
          from
            (
              SELECT
                DISTINCT parent.obs_id
              FROM
                obs AS parent
                JOIN concept_view pcv ON pcv.concept_id = parent.concept_id
                AND pcv.concept_full_name = 'Visit Diagnoses'
                LEFT JOIN obs AS child ON child.obs_group_id = parent.obs_id
                AND child.voided = FALSE
                JOIN concept_name AS certainty ON certainty.concept_id = child.value_coded
                AND (
                  certainty.name = 'Confirmed' || certainty.name = 'Presumed'
                )
                AND certainty.concept_name_type = 'FULLY_SPECIFIED'
              WHERE
                parent.voided IS FALSE
            ) AS certaintyObs
        )
      group by
        diagnosis.value_coded
    )
    UNION
      (
        SELECT
          patient_conditions.condition_coded AS value_coded,
          SUM(
            IF(person.gender = 'F', 1, 0)
          ) AS female,
          SUM(
            IF(person.gender = 'M', 1, 0)
          ) AS male,
          SUM(
            IF(person.gender = 'O', 1, 0)
          ) AS other,
          SUM(
            IF(person.gender = 'U', 1, 0)
          ) AS undisclosed,
          patient_conditions.date_created AS obs_datetime
        FROM
          conditions patient_conditions
          JOIN person on patient_conditions.patient_id = person.person_id
        WHERE
          patient_conditions.clinical_status = 'ACTIVE'
          AND cast(
            CONVERT_TZ(
              patient_conditions.date_created,
              '+00:00', '+5:30'
            ) AS DATE
          ) BETWEEN '#startDate#'
          AND '#endDate#'
          AND patient_conditions.voided = FALSE
          AND person.voided = FALSE
        group by
          patient_conditions.condition_coded
      )
  ) as diagnosisObs
  JOIN concept_name cn on cn.concept_id = diagnosisObs.value_coded
  AND cn.concept_name_type = '#conceptNameDisplayFormat#'
  AND cn.locale = 'en'
  AND cn.voided = FALSE
  INNER JOIN concept_reference_map crm on crm.concept_id = cn.concept_id
  INNER JOIN concept_reference_term crt on crt.concept_reference_term_id = crm.concept_reference_term_id
  and crt.concept_source_id = (
    select
      concept_source_id
    from
      concept_reference_source
    where
      hl7_code like '#conceptSourceCode#'
  )
  INNER JOIN #tempTable# tmp on tmp.code = crt.code
group by
  cn.name;