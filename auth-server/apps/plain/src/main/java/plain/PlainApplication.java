package plain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

/**
 *
 * @author Shawn Deng
 * @date 2021-09-14 13:35:50
 */
@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)
public class PlainApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlainApplication.class, args);
    }
}
