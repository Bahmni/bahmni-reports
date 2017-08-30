select pi.identifier,
  CONCAT(pn.given_name, " ", ifnull(pn.family_name, "")) AS PatientName,
  floor(datediff(CURDATE(), p.birthdate) / 365) AS age,
  date(pp.date_enrolled) as date_enrolled,
  cn.name as state_name,
  ps.start_date,
  ps.end_date,
  date(pp.date_completed) as date_completed,
  ifnull(cn_short_name.name, cn2.name) as outcome
from program prog
  join program_workflow pw on prog.program_id = pw.program_id and prog.name='#programName#'
  join program_workflow_state pws on pw.program_workflow_id = pws.program_workflow_id
  join concept_name cn on pws.concept_id = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED'
  join patient_program pp on prog.program_id = pp.program_id and date(pp.date_enrolled) between '#startDate#' and '#endDate#'
  join patient_state ps on pp.patient_program_id = ps.patient_program_id and pws.program_workflow_state_id = ps.state and ps.voided=0
  join person p on pp.patient_id = p.person_id
  join patient_identifier pi on pp.patient_id = pi.patient_id AND pi.preferred = 1
  join person_name pn ON pp.patient_id = pn.person_id
  left join concept_name cn2 on pp.outcome_concept_id = cn2.concept_id and cn2.concept_name_type='FULLY_SPECIFIED'
  left join concept_name cn_short_name on pp.outcome_concept_id = cn_short_name.concept_id and cn_short_name.concept_name_type='SHORT'
order by pi.identifier, ps.date_created;