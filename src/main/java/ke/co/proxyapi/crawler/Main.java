package ke.co.proxyapi.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@Slf4j
@SpringBootApplication
@Configuration
@EnableSpringConfigured
public class Main {
  public static void main(String args[]) {
    try {
      SpringApplication.run(Main.class, args);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
