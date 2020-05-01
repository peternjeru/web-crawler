package ke.co.proxyapi.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
@Getter
@Slf4j
@ComponentScan(basePackages = {"ke.co.proxyapi.crawler"})
public class Main implements CommandLineRunner
{
    @Autowired
    private Crawler crawler;
    public static String EXIT = "exit";
    public static String PRINT = "print";

    public static void main(String args[])
    {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        try
        {
            List<String> regexList = new ArrayList<>();
            regexList.add("clinical officer");
            regexList.add("jobs");
            regexList.add("kenya");

            crawler.setSearchRegexList(regexList);

            CrawlConfig config = new CrawlConfig();
            config.setCrawlStorageFolder("D:\\Logs\\Crawler");
            config.setPolitenessDelay(500);
            config.setIncludeHttpsPages(true);
            config.setRespectNoFollow(false);
            config.setIncludeBinaryContentInCrawling(false);
            config.setShutdownOnEmptyQueue(true);
            config.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
            config.setFollowRedirects(true);
            config.setResumableCrawling(true);
            config.setMaxDownloadSize(5242880);
            PageFetcher pageFetcher = new PageFetcher(config);

            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            robotstxtConfig.setEnabled(false);
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            String searchStr = String.join("+", regexList);
            controller.addSeed("https://www.google.com/search?q=" + searchStr);

            CrawlController.WebCrawlerFactory<Crawler> factory = () -> crawler;

            controller.startNonBlocking(factory, 20);
            log.info("Crawling Started");

            Scanner sc = new Scanner(System.in);
            while(sc.hasNextLine())
            {
                String input = sc.nextLine();
                if(input.equalsIgnoreCase(EXIT))
                {
                    controller.shutdown();
                    controller.waitUntilFinish();
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
