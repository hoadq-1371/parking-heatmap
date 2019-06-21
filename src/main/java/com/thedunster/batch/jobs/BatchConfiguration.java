package com.thedunster.batch.jobs;

import com.socrata.api.Soda2Consumer;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.SodaError;
import com.socrata.model.soql.SoqlQuery;
import com.thedunster.batch.listener.JobCompletionNotificationListener;
import com.thedunster.batch.processor.SodaCitationProcessor;
import com.thedunster.common.SodaCitation;
import com.thedunster.jpa.entities.CitationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public EntityManagerFactory entityManagerFactory;

    /**
     * Default to 0 if system property is not given.
     */
    @Value("${soda.query.limit:0}")
    public Integer queryLimit;

    /**
     * Batch reader. Reads from SODA api to get data.
     * @return
     */
    @Bean
    public ListItemReader<SodaCitation> reader() {
        final String method = "reader()";

        Soda2Consumer consumer = Soda2Consumer.newConsumer("https://data.baltimorecity.gov/");
        log.info("{}: Loaded query limit property: {}", method, queryLimit);
        SoqlQuery query = new SoqlQueryBuilder()
                .setWhereClause("violdate >'2016-03-01T00:00:00' AND violdate < '2016-04-01T00:00:00'")
                .setLimit(queryLimit)
                .build();

        List<SodaCitation> sodaCitationJsonsObjectList = sodaCall(method, consumer, query);

        ListItemReader<SodaCitation> reader = null;
        if (sodaCitationJsonsObjectList != null) {
            log.info("{}: Pop gave me ${}", method, sodaCitationJsonsObjectList.size());
            reader = new ListItemReader<>(sodaCitationJsonsObjectList);
        } else {
            log.info("{}: Pop told me nothing.", method);
        }
        return reader;
    }

    /**
     * Performs call to SODA.
     * @param method
     * @param consumer
     * @param query
     * @return
     */
    private List<SodaCitation> sodaCall(String method, Soda2Consumer consumer, SoqlQuery query) {
        List<SodaCitation> sodaCitationJsonsObjectList = null;
        try {
            sodaCitationJsonsObjectList = consumer.query("n4ma-fj3m", query, SodaCitation.LIST_TYPE);
            if (sodaCitationJsonsObjectList != null) {
                log.info("Soda Call successful with {} objects", sodaCitationJsonsObjectList.size());
            } else {
                log.info("Soda Call returned blank");
            }
        } catch (SodaError sodaError) {
            log.error("{}: SodaError Caught: {}", method, sodaError);
        } catch (InterruptedException e) {
            log.error("{}: Exception Caught: {}", method, e);
        }
        return sodaCitationJsonsObjectList;
    }

    @Bean
    public SodaCitationProcessor processor() {
        return new SodaCitationProcessor();
    }

    @Bean
    public JpaItemWriter<CitationEntity> writer() {
        JpaItemWriter<CitationEntity> writer = new JpaItemWriter<CitationEntity>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionNotificationListener();
    }

    @Bean
    public Job importViolationJobs() {
        return jobBuilderFactory.get("importViolationJobs")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<SodaCitation, CitationEntity>chunk(1)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }


}