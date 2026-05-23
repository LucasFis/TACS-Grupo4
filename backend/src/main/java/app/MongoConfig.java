package app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Configuration
public class MongoConfig {
  @Bean
  @Profile("!test")
  public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
    return new MongoTransactionManager(factory);
  }
}