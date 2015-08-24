select person_attribute.person_id,#patientAttributesFrom#
from person_attribute
INNER JOIN person_attribute_type ON person_attribute_type.person_attribute_type_id = person_attribute.person_attribute_type_id
LEFT JOIN concept_name person_attribute_cn ON person_attribute.value = person_attribute_cn.concept_id AND person_attribute_cn.concept_name_type = "FULLY_SPECIFIED"
WHERE person_attribute_type.name IN (#patientAttributesWhere#)
GROUP BY person_id