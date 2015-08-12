SELECT
  state_from_name.name AS state_from_name,
  state_to_name.name AS state_to_name,
  SUM(1) AS count_total
FROM program prog
  INNER JOIN patient_program pp ON pp.program_id=prog.program_id
    AND prog.name='#programName#'
  INNER JOIN patient_state state_to ON state_to.patient_program_id=pp.patient_program_id
    AND state_to.voided=0
  INNER JOIN program_workflow_state pws_to ON pws_to.program_workflow_state_id = state_to.state
  INNER JOIN concept_name state_to_name ON state_to_name.concept_id = pws_to.concept_id
    AND state_to_name.concept_name_type = 'FULLY_SPECIFIED'
  INNER JOIN patient_state state_from ON state_to.date_created = state_from.date_changed
  INNER JOIN program_workflow_state pws_from ON pws_from.program_workflow_state_id = state_from.state
  INNER JOIN concept_name state_from_name ON state_from_name.concept_id=pws_from.concept_id
    AND state_from_name.concept_name_type='FULLY_SPECIFIED'
WHERE state_from.state
  AND DATE(state_to.start_date) BETWEEN DATE('#startDate#') AND DATE('#endDate#')
GROUP BY CONCAT(state_from.state,'-',state_to.state)
ORDER BY state_from_name, state_to_name;
