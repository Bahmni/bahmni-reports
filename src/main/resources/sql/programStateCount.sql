SELECT
  program.program_id as program_id,
  program.name AS program_name ,
  ps.state AS state_id,
  concept.name AS state_name,
  SUM(CASE WHEN ps.state = pws.program_workflow_state_id THEN 1 ELSE  0 END ) as count_total

FROM program program
INNER JOIN program_workflow pw ON pw.program_id = program.program_id AND program.name="#programName#"
INNER JOIN program_workflow_state pws ON pws.program_workflow_id = pw.program_workflow_id
INNER JOIN concept_name concept ON concept.concept_id = pws.concept_id AND concept.concept_name_type='FULLY_SPECIFIED'
LEFT JOIN patient_program pp ON pp.program_id = program.program_id and pp.voided = 0
LEFT JOIN patient_state ps ON ps.patient_program_id = pp.patient_program_id and DATE(ps.start_date) BETWEEN "#startDate#" AND "#endDate#" and ps.voided = 0
GROUP BY pws.concept_id HAVING pws.concept_id is NOT NULL;