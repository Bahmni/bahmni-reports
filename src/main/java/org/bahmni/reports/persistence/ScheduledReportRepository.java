package org.bahmni.reports.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, String> {

    ScheduledReport findScheduledReportById(@Param("id") String id);

    @Query("SELECT r FROM ScheduledReport r WHERE requestDatetime < :requestDatetime")
    List<ScheduledReport> findByRequestDateTime(@Param("requestDatetime") Date requestDatetime);

    void delete(ScheduledReport report);

    List<ScheduledReport> findScheduledReportsByUser(@Param("user") String user);
}
