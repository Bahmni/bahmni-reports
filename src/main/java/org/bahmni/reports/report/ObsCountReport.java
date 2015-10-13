package org.bahmni.reports.report;

import java.util.List;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ObsCountConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.BooleanConceptsCountTemplate;
import org.bahmni.reports.template.CodedObsCountTemplate;
import org.bahmni.reports.template.ObsCountTemplate;
import org.bahmni.reports.util.ConceptDataTypeException;
import org.bahmni.reports.util.ConceptDataTypes;
import org.bahmni.reports.util.ConceptUtil;

public class ObsCountReport extends Report<ObsCountConfig> {

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        List<String> conceptNames = this.getConfig().getConceptNames();
        try {
            ConceptDataTypes conceptDataType = ConceptUtil.getConceptDataType(conceptNames, getHttpClient(), bahmniReportsProperties
                    .getOpenmrsRootUrl());
            switch (conceptDataType) {
                case Boolean:
                    return new BooleanConceptsCountTemplate();
                case Coded:
                    return new CodedObsCountTemplate();
                default:
                    return new ObsCountTemplate();
            }
        } catch (ConceptDataTypeException e) {
            throw new RuntimeException(e);
        }
    }

}
