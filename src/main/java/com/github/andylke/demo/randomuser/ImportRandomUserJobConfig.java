package com.github.andylke.demo.randomuser;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties({ImportRandomUserProperties.class})
@Import({ImportRandomUserStepConfig.class})
public class ImportRandomUserJobConfig {

  @Autowired private JobBuilderFactory jobBuilderFactory;

  @Autowired private Step importRandomUserStep;

  @Bean
  public Job importRandomUserJob(ThreadPoolTaskExecutor taskExecutor) {
    return jobBuilderFactory
        .get("importRandomUser")
        .incrementer(new RunIdIncrementer())
        .start(importRandomUserStep)
        .listener(
            new JobExecutionListener() {
              @Override
              public void beforeJob(JobExecution jobExecution) {}

              @Override
              public void afterJob(JobExecution jobExecution) {
                taskExecutor.shutdown();
              }
            })
        .build();
  }
}
