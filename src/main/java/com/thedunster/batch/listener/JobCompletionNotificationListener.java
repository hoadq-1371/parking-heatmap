package com.thedunster.batch.listener;


import com.thedunster.jpa.entities.CitationEntity;
import com.thedunster.jpa.repository.CitationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Autowired
    CitationRepository citationRepository;

    @Autowired
    public JobCompletionNotificationListener() {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            List<CitationEntity> results = (List<CitationEntity>) citationRepository.findAll();
            log.info("Completed batch job for {} items.", results.size());

        }
    }
}
