SELECT
    patIdentifier As "Patient ID",
    concat(coalesce(givenName,''),' ', coalesce(middleName,'') ,' ',coalesce(familyName,'') ) as 'Patient Name',
        Dob AS "Date Of Birth",
    gender AS Gender,
    cn.name AS "Diagnosis or Condition",
    dateOfDiagnosis AS "Date And Time",
    crt.code AS "Terminology Code"
from
    (
        (
            SELECT
                diagnosis.value_coded AS value_coded,
                person.person_id As pid,
                person.gender AS gender,
                pn.given_name As givenName,
                pn.middle_name As middleName,
                pn.family_name As familyName,
                person.birthdate As Dob,
                pti.identifier As patIdentifier,
                diagnosis.obs_datetime As dateOfDiagnosis
            from
                obs AS diagnosis
                    JOIN person on diagnosis.person_id = person.person_id
                    AND person.voided = FALSE
                    JOIN person_name AS pn on person.person_id = pn.person_id
                    JOIN patient_identifier AS pti on person.person_id = pti.patient_id AND pti.preferred = 1
                    AND identifier_type = (select patient_identifier_type_id from patient_identifier_type where name = 'Patient Identifier')
                    JOIN concept_view AS cv ON cv.concept_id = diagnosis.value_coded
                    AND cv.concept_class_name = 'Diagnosis'
                    AND cast(
                                                       CONVERT_TZ(
                                                               diagnosis.obs_datetime, '+00:00',
                                                               '+5:30'
                                                           ) AS DATE
                                                   ) BETWEEN '2023-06-01'
                                                   AND '2023-06-14'
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
                person.person_id AS pid,
                person.gender As gender,
                pn.given_name As givenName,
                pn.middle_name As middleName,
                pn.family_name As familyName,
                person.birthdate As Dob,
                pti.identifier As patIdentifier,
                patient_conditions.date_created AS obs_datetime
            FROM
                conditions patient_conditions
                    JOIN person on patient_conditions.patient_id = person.person_id
                    JOIN patient_identifier AS pti on person.person_id = pti.patient_id AND pti.preferred = 1
                    AND identifier_type = (select patient_identifier_type_id from patient_identifier_type where name = 'Patient Identifier')
                    JOIN person_name AS pn on person.person_id = pn.person_id
            WHERE
                    patient_conditions.clinical_status = 'ACTIVE'
              AND cast(
                    CONVERT_TZ(
                            patient_conditions.date_created,
                            '+00:00', '+5:30'
                        ) AS DATE
                ) BETWEEN '2023-06-01'
                AND '2023-06-14'
              AND patient_conditions.voided = FALSE
              AND person.voided = FALSE
            group by
                patient_conditions.condition_coded
        )
    ) as diagnosisObs
        JOIN concept_name cn on cn.concept_id = diagnosisObs.value_coded
        AND cn.concept_name_type = 'SHORT'
        AND cn.locale = 'en'
        AND cn.voided = FALSE
        INNER JOIN concept_reference_map crm on crm.concept_id = cn.concept_id
        INNER JOIN concept_reference_term crt on crt.concept_reference_term_id = crm.concept_reference_term_id
        and crt.concept_source_id = 2
group by
    cn.name;