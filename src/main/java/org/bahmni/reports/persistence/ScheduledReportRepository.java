package org.bahmni.reports.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, String> {

    ScheduledReport findScheduledReportById(@Param("id") String id);
}
