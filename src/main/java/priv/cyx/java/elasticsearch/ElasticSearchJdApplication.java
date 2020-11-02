package priv.cyx.java.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
public class ElasticSearchJdApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticSearchJdApplication.class, args);
    }

}
