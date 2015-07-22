package org.bahmni.reports.report;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;


public class ObsCountReport extends Report<ObsCountConfig>{

    private static final Logger logger = Logger.getLogger(ObsCountReport.class);

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        String conceptNames = this.getConfig().getConceptNames();
        ConnectionDetails connectionDetails = new ConnectionDetails(bahmniReportsProperties.getOpenmrsRootUrl() + "/session", bahmniReportsProperties.getOpenmrsServiceUser(),
                bahmniReportsProperties.getOpenmrsServicePassword(), bahmniReportsProperties.getOpenmrsConnectionTimeout(), bahmniReportsProperties.getOpenmrsReplyTimeout());
        HttpClient httpClient = new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));
        try {
            ConceptDataTypes conceptDataType = ConceptUtil.getConceptDataType(getConceptStringArray(conceptNames), httpClient, bahmniReportsProperties.getOpenmrsRootUrl());
            switch (conceptDataType){
                case Boolean:
                    return new BooleanConceptsCountTemplate();
                case Coded:
                    return new CodedObsCountTemplate();
                default:
                    return new ObsCountTemplate();
            }
        } catch (ConceptDataTypeException e) {
            logger.error(e);
        }
        return null;
    }

    private String[] getConceptStringArray(String concept) {
        if(StringUtils.isNotBlank(concept)) {
            if (concept.startsWith("'")) {
                concept = concept.substring(1);
            }
            if (concept.endsWith("'")) {
                concept = concept.substring(0, concept.lastIndexOf("'"));
            }
            String[] strings = concept.split("'\\s*,\\s*'");
            String out[] = new String[strings.length];
            int i = 0;
            for (String string : strings) {
                out[i++] = "'" + string + "'";
            }
            return out;
        }
        return new String[0];
    }
}
