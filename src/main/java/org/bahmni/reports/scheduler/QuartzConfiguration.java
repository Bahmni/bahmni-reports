package org.bahmni.reports.scheduler;

import org.apache.log4j.Logger;
import org.bahmni.reports.model.AllDatasources;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfiguration {
    private static final Logger logger = Logger.getLogger(QuartzConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AllDatasources allDatasources;


    @Bean
    public SchedulerFactoryBean scheduler() {
        SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
        quartzScheduler.setSchedulerName("reports-scheduler");
        quartzScheduler.setJobFactory(jobFactory());
        quartzScheduler.setConfigLocation(new ClassPathResource("quartz.properties"));
        quartzScheduler.setDataSource(allDatasources.dataSourceFor("bahmniReports"));
        try {
            quartzScheduler.afterPropertiesSet();
            ReportsScheduler.scheduleCrons(quartzScheduler);
        } catch (Exception e) {
            logger.error("Cannot start scheduler", e);
            throw new RuntimeException("Cannot start scheduler:-", e);
        }
        return quartzScheduler;
    }

    @Bean
    public JobFactory jobFactory() {
        AutowiringSpringBeanJobFactory springBeanJobFactory = new AutowiringSpringBeanJobFactory();
        springBeanJobFactory.setApplicationContext(applicationContext);
        return springBeanJobFactory;
    }

}
