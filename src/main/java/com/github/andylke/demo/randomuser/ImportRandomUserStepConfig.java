package com.github.andylke.demo.randomuser;

import com.github.andylke.demo.user.User;
import com.github.andylke.demo.user.UserRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ImportRandomUserStepConfig {

  public static final String RANDOM_USER_FILE_PATH = "target/random-user.csv";

  public static final String[] RANDOM_USER_FIELD_NAMES =
      new String[] {
        "gender",
        "name.title",
        "name.first",
        "name.last",
        "email",
        "login.uuid",
        "login.username",
        "login.password",
        "login.salt",
        "login.md5",
        "login.sha1",
        "login.sha256",
        "nat"
      };

  @Autowired private StepBuilderFactory stepBuilderFactory;

  @Autowired private UserRepository userRepository;

  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    return new TaskExecutorBuilder().corePoolSize(50).maxPoolSize(50).queueCapacity(100).build();
  }

  @Bean
  public Step importRandomUserStep(ThreadPoolTaskExecutor taskExecutor) {
    return stepBuilderFactory
        .get("importRandomUserStep")
        .<RandomUser, User>chunk(50)
        .reader(randomUserFileReader())
        .processor(randomUserToUserProcessor())
        .writer(userWriter())
        .taskExecutor(taskExecutor)
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<? extends RandomUser> randomUserFileReader() {
    return new FlatFileItemReaderBuilder<RandomUser>()
        .name("randomUserFileReader")
        .resource(new FileSystemResource(RANDOM_USER_FILE_PATH))
        .linesToSkip(1)
        .delimited()
        .names(RANDOM_USER_FIELD_NAMES)
        .targetType(RandomUser.class)
        .build();
  }

  @Bean
  public RandomUserToUserProcessor randomUserToUserProcessor() {
    return new RandomUserToUserProcessor();
  }

  @Bean
  @StepScope
  public RepositoryItemWriter<? super User> userWriter() {
    return new RepositoryItemWriterBuilder<User>().repository(userRepository).build();
  }
}
