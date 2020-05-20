package ke.co.proxyapi.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@Slf4j
@ComponentScan(basePackages = {"ke.co.proxyapi.crawler"})
public class Main
{
    public static void main(String args[])
    {
        try
        {
            SpringApplication.run(Main.class, args);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
