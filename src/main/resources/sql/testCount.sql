SELECT DISTINCT
  ts.name       AS department,
  t.name        AS test,
  count(r.id)   AS total_count,
  CASE WHEN t.id IN (SELECT test_id
                     FROM test_result
                     WHERE tst_rslt_type = 'D') THEN (sum(CASE WHEN d.id IS NOT NULL THEN 1
                                                          ELSE 0 END))
  ELSE NULL END AS positive,
  CASE WHEN t.id IN (SELECT test_id
                     FROM test_result
                     WHERE tst_rslt_type = 'D') THEN sum(CASE WHEN d1.id IS NOT NULL THEN 1
                                                         ELSE 0 END)
  ELSE NULL END AS negative
FROM test_section ts
  INNER JOIN test t ON ts.id = t.test_section_id AND t.is_active = 'Y'
  LEFT OUTER JOIN analysis a ON t.id = a.test_id
  LEFT OUTER JOIN result r ON a.id = r.analysis_id and r.lastupdated BETWEEN '%s' and '%s'
  LEFT OUTER JOIN result r1 ON r1.result_type = 'D' and r1.value != '' and r.id=r1.id
  LEFT OUTER JOIN test_result tr ON tr.test_id = t.id AND tr.tst_rslt_type = 'D' AND tr.id = r1.test_result_id
  LEFT OUTER JOIN dictionary d ON d.id = r1.value :: INTEGER AND d.dict_entry LIKE '%%positive%%' OR
                                  d.dict_entry LIKE '%%+%%' AND tr.value :: INTEGER = d.id
  LEFT OUTER JOIN dictionary d1 ON d1.id = r1.value :: INTEGER AND d1.dict_entry LIKE '%%negative%%' OR
                                   d1.dict_entry LIKE '%%-%%' AND tr.value :: INTEGER = d1.id
GROUP BY ts.name, t.name, t.id
order by ts.name;