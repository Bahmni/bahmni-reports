SET SESSION group_concat_max_len = 100000;
SET @conceptSourceId = NULL;
SELECT concept_source_id from concept_reference_source WHERE name = "#conceptSourceName#" into @conceptSourceId;

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
WHERE cn.concept_name_type = "FULLY_SPECIFIED" AND cn.name IN (#conceptNameInClause#);

SET @viConceptSelector = NULL;
SELECT GROUP_CONCAT(DISTINCT
                    CONCAT(
                        'IF(cn.name = ''',
                        name,
                        ''', coalesce(',IF (@conceptSourceId IS NULL, '', 'CRT.code,'),'cans.name, o.value_numeric, o.value_text, o.value_datetime, o.date_created, e.encounter_datetime','), NULL) AS `vi_tr_',
                        name, '`'
                    )
)
INTO @viConceptSelector
FROM concept_name cn
WHERE cn.concept_name_type = "FULLY_SPECIFIED" AND cn.name IN (#visitIndependentConceptInClause#);

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
WHERE cn.concept_name_type = "FULLY_SPECIFIED" AND  cn.name IN (#conceptNameInClause#);



SET  @viConceptMaxSelector = NULL;
SELECT GROUP_CONCAT(DISTINCT
                    CONCAT(
                        'MAX(`', 'vi_tr_',
                        name,
                        '`) AS ' , '`latest_',name,
                        '` '
                    )
)
INTO @viConceptMaxSelector
FROM concept_name cn
WHERE cn.concept_name_type = "FULLY_SPECIFIED" AND cn.name IN (#visitIndependentConceptInClause#);


SELECT GROUP_CONCAT(DISTINCT CONCAT(
    'IF (pat.name = \'',name,'\',COALESCE(pacn.name,pa.value,NULL), NULL) AS `pat_tr_' , name , '`'
))
INTO @patientAttributesSelectClause
FROM person_attribute_type
WHERE name IN (#patientAttributesInClause#);


SELECT GROUP_CONCAT(DISTINCT CONCAT(
   'MAX(`pat_tr_',name,'`) AS `' , name, '`'
))
INTO @patientAttributesMaxSelectClause
FROM person_attribute_type
WHERE name IN (#patientAttributesInClause#);

SET @addressAttributesSql = REPLACE("#addressAttributesInClause#" ,'\'', '');

SET @dateFilterVar = "#applyDateRangeFor#";
SELECT CASE @dateFilterVar WHEN 'ProgramEnrollment' THEN  'pp.date_enrolled' ELSE ' o.obs_datetime'  END INTO @dateFilterQuery;

SET @ShowObsOnlyForProgramDuration = #showObsOnlyForProgramDuration# ;
SELECT IF(@ShowObsOnlyForProgramDuration AND ("#enrolledProgram#" != ""),'AND o.obs_datetime BETWEEN pp.date_enrolled AND IF (pp.date_completed IS NULL, NOW(), pp.date_completed)','') INTO @obsForProgramDuration;

SET @dateFilterQuery = IF( "#enrolledProgram#" ="" , ' o.obs_datetime '  ,@dateFilterQuery);

SET @conceptRefMapSql = " LEFT JOIN (SELECT CRM.concept_id,  CRT.code FROM (SELECT * from concept_reference_term  WHERE concept_source_id = @conceptSourceId) as CRT INNER JOIN concept_reference_map as CRM ON CRT.concept_reference_term_id = CRM.concept_reference_term_id) CRT ON cans.concept_id = CRT.concept_id ";

SET @sqlCore = CONCAT('SELECT * from(
  SELECT
  pi.identifier,
  p.person_id,
  CONCAT(pname.given_name, \' \', pname.family_name) as patient_name,
  p.gender,
  Floor(Datediff(Date(o.date_created), p.birthdate) / 365) as age,
  ',@addressAttributesSql,',

  cn.name obs_name,
  coalesce(',IF (@conceptSourceId IS NULL, '', 'CRT.code,'),'cans.name, o.value_numeric, o.value_text, o.value_datetime, o.date_created, e.encounter_datetime',')  as obs_value,
  o.obs_datetime,
  e.visit_id ',
  IF(@conceptSourceId IS NULL, '', ',CRT.code'),
' FROM obs as o
JOIN encounter e
  ON o.encounter_id = e.encounter_id
JOIN concept_name cn
  ON o.concept_id = cn.concept_id
  AND cn.name IN (#conceptNameInClauseEscapeQuote#)
  AND cn.concept_name_type = "FULLY_SPECIFIED"
JOIN person p
  ON p.person_id = o.person_id
JOIN patient_identifier pi
  ON pi.patient_id = p.person_id
  AND pi.preferred = 1
LEFT JOIN person_address address
  ON p.person_id = address.person_id
JOIN person_name pname
  ON p.person_id = pname.person_id
LEFT JOIN concept_name as cans
  ON cans.concept_id = o.value_coded
  AND cans.concept_name_type = "FULLY_SPECIFIED"',
  IF(@conceptSourceId IS NULL, '', @conceptRefMapSql),
IF( "#enrolledProgram#" ="" ,'' ,
' JOIN patient_program pp
  ON pp.patient_id = p.person_id
JOIN program
  ON program.program_id = pp.program_id
  AND program.name like \'#enrolledProgram#\'' ),'
WHERE o.voided IS FALSE
 AND ',@dateFilterQuery,' BETWEEN \'#startDate# 00:00:00\'  AND \'#endDate# 23:59:59\'
  ',@obsForProgramDuration,'
ORDER BY o.person_id, o.obs_datetime DESC, o.obs_id DESC
) as temp
GROUP BY person_id, visit_id, obs_name
');


SET @sqlCoreVi = CONCAT('SELECT * from (
  SELECT
  o.person_id patient_id,
  cn.name observation_name,
  ',@viConceptSelector,'
FROM obs as o
JOIN encounter e
  ON o.encounter_id = e.encounter_id
JOIN concept_name cn
  ON o.concept_id = cn.concept_id
  AND cn.name IN (#visitIndependentConceptInClauseEscaped#)
  AND cn.concept_name_type = "FULLY_SPECIFIED"
  LEFT JOIN concept_name cans ON o.value_coded = cans.concept_id',
  IF(@conceptSourceId IS NULL, '', @conceptRefMapSql),
                        IF( "#enrolledProgram#" ="" ,'' ,
                            ' JOIN patient_program pp
                              ON pp.patient_id = o.person_id
                            JOIN program
                              ON program.program_id = pp.program_id
                              AND program.name like \'#enrolledProgram#\'' ),'
WHERE o.voided IS FALSE
AND ',@dateFilterQuery,' BETWEEN \'#startDate# 00:00:00\' AND \'#endDate# 23:59:59\'
  ',@obsForProgramDuration,'
ORDER BY o.person_id, o.obs_datetime DESC, o.obs_id DESC
) as temp2
GROUP BY patient_id,  observation_name');






SET @sql = CONCAT('SELECT
  core.*,
  ',@patientAttributesSelectClause,',
  vi_core.*,
  ', @conceptSelector,

                  ' FROM
                  (
                    ',@sqlCore,
                  '
                ) core
          LEFT JOIN (
          ',@sqlCoreVi,'
          ) vi_core  ON  core.person_id = vi_core.patient_id

  LEFT JOIN person_attribute  pa
    ON core.person_id = pa.person_id
  LEFT JOIN person_attribute_type  pat
    ON pat.person_attribute_type_id = pa.person_attribute_type_id
    AND pat.name IN (#patientAttributesInClauseEscapeQuote#)
  LEFT JOIN concept_name pacn
    ON pa.value = pacn.concept_id
    AND pat.format like (\'%concept\')
    AND pacn.concept_name_type like (\'FULLY_SPECIFIED\')
    ');


SET @sqlWrapper = CONCAT(
    'SELECT
    identifier,
    patient_name,
    gender,
    age,
    visit_id,
    ',@addressAttributesSql,
    ',', @conceptMaxSelector,
    ',', @viConceptMaxSelector,
    ',', @patientAttributesMaxSelectClause,
    ' FROM (
       ',@sql,'
   ) wrapper
   GROUP BY person_id,visit_id'
);

PREPARE stmt FROM @sqlWrapper;
EXECUTE stmt;
