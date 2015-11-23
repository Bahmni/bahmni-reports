SET @conceptSelector = NULL;
SELECT GROUP_CONCAT(DISTINCT
                    CONCAT(
                        'IF(core.obs_name = ''',
                        name,
                        ''', obs_value, NULL) AS `tr_',
                        name, '`'
                    )
)
INTO @conceptSelector
FROM concept_name cn
WHERE cn.name IN (#conceptNameInClause#);

SET @viConceptSelector = NULL;
SELECT GROUP_CONCAT(DISTINCT
                    CONCAT(
                        'IF(vi_obs_name = ''',
                        name,
                        ''', vi_obs_value, NULL) AS `',
                        name, '`'
                    )
)
INTO @viConceptSelector
FROM concept_name cn
WHERE cn.name IN (#visitIndependentConceptInClause#);

SET  @conceptMaxSelector = NULL;
SELECT GROUP_CONCAT(DISTINCT
                    CONCAT(
                        'MAX(`', 'tr_',
                        name,
                        '`) AS ' , '`',name,
                        '` '
                    )
)
INTO @conceptMaxSelector
FROM concept_name cn
WHERE cn.name IN (#conceptNameInClause#);


SELECT GROUP_CONCAT(DISTINCT CONCAT(
    'IF (patient_attribute_name = \'',name,'\',patient_attribute_value, NULL) AS ' , name
))
INTO @patientAttributesSelectClause
FROM person_attribute_type
WHERE name IN (#patientAttributesInClause#);

SET @addressAttributesSql = REPLACE("#addressAttributesInClause#" ,'\'', '');

SET @dateFilterVar = "ProgramEnrollment";
SELECT CASE @dateFilterVar WHEN 'ProgramEnrollment' THEN  'pp.date_enrolled' ELSE ' o.obs_datetime'  END INTO @dateFilterQuery;

SET @ShowObsOnlyForProgramDuration = FALSE ;
SELECT IF(@ShowObsOnlyForProgramDuration AND "#enrolledProgram#" != "",'AND o.obs_datetime BETWEEN pp.date_enrolled AND pp.date_completed','') INTO @obsForProgramDuration;

SET @dateFilterQuery = IF( "#enrolledProgram#" ="" , ' o.obs_datetime'  ,@dateFilterQuery);

SET @sqlCore = CONCAT('SELECT
  p.person_id,
  CONCAT(pname.given_name, \' \', pname.family_name) as patient_name,
  p.gender,
  Floor(Datediff(Date(o.date_created), p.birthdate) / 365) as age,
  ',@addressAttributesSql,',

  pat.name as patient_attribute_name,
  COALESCE(pacn.name,pa.value,NULL) as patient_attribute_value,

  cn.name obs_name,
  coalesce(o.value_numeric, o.value_boolean, o.value_text, o.date_created, e.encounter_datetime, NULL)  as obs_value,

  o.obs_datetime,
  e.visit_id,
  vi.vi_obs_name,
  vi.vi_obs_value
FROM obs as o
JOIN encounter e
  ON o.encounter_id = e.encounter_id
JOIN concept_name cn
  ON o.concept_id = cn.concept_id
  AND cn.name IN (#conceptNameInClauseEscapeQuote#)
  AND cn.concept_name_type = "FULLY_SPECIFIED"
JOIN person p
  ON p.person_id = o.person_id
JOIN person_address address
  ON p.person_id = address.person_id
JOIN person_name pname
  ON p.person_id = pname.person_id
JOIN person_attribute  pa
  ON p.person_id = pa.person_id
JOIN person_attribute_type  pat
  ON pat.person_attribute_type_id = pa.person_attribute_type_id
  AND pat.name IN (#patientAttributesInClauseEscapeQuote#)
LEFT JOIN concept_name pacn
  ON pa.value = pacn.concept_id
  AND pat.format like (\'%concept\')
  AND pacn.concept_name_type like (\'FULLY_SPECIFIED\')
JOIN (
  SELECT
  vi_o.person_id,
  vi_cn.name vi_obs_name,
  coalesce(vi_o.value_numeric, vi_o.value_boolean, vi_o.value_text, vi_o.date_created, vi_e.encounter_datetime, NULL)  as vi_obs_value
  FROM  obs as vi_o
  JOIN encounter vi_e
    ON vi_o.encounter_id = vi_e.encounter_id
  JOIN concept_name vi_cn
    ON vi_o.concept_id = vi_cn.concept_id
    AND vi_cn.name IN (#visitIndependentConceptInClauseEscaped#)
    AND vi_cn.concept_name_type = "FULLY_SPECIFIED"
  GROUP BY vi_o.person_id, vi_cn.name
  ORDER BY vi_o.obs_datetime DESC
) vi
  ON vi.person_id = p.person_id ',
IF( "#enrolledProgram#" ="" ,'' ,
'JOIN patient_program pp
  ON pp.patient_id = p.person_id
JOIN program
  ON program.program_id = pp.program_id
  AND program.name like \'#enrolledProgram#\'' ),'
WHERE ',@dateFilterQuery,' BETWEEN \'#startDate#\' AND \'#endDate#\'
  ',@obsForProgramDuration,'
GROUP BY o.person_id, e.visit_id, cn.name
ORDER BY o.person_id, o.obs_datetime DESC');

SET @sql = CONCAT('SELECT
  core.*,
  ', @conceptSelector,

                  ' FROM
                  (
                    ',@sqlCore,
                  '
                ) core');


SET @sqlWrapper = CONCAT(
    'SELECT
    person_id,
    patient_name,
    gender,
    age,
    visit_id,
    ',@addressAttributesSql,',
    ',@viConceptSelector,
    ',', @conceptMaxSelector,
    ',', @patientAttributesSelectClause,
    ' FROM (
       ',@sql,'
   ) wrapper
   GROUP BY person_id,visit_id'
);

PREPARE stmt FROM @sqlWrapper;
EXECUTE stmt;

