package ke.co.proxyapi.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import ke.co.proxyapi.crawler.persistence.models.Website;
import ke.co.proxyapi.crawler.persistence.services.WebsiteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Component
public class Runner implements CommandLineRunner
{
    @Autowired
    private Crawler crawler;

    @Autowired
    private WebsiteService websiteService;

    public static final String EXIT = "exit";
    public static final String LIST = "list";
    public static final String ADD = "add";
    public static final String REMOVE = "remove";

    @Override
    public void run(String... args) throws Exception
    {
        try
        {
            List<String> regexList = new ArrayList<>();
            regexList.add("clinical officer");
//            regexList.add("job");
            regexList.add("kenya");
            regexList.add("2020");

            crawler.setSearchRegexList(regexList);

            //how to add exclusion domains
//            List<String> excludeList = new ArrayList<>();
//            excludeList.add("google.com");
//            excludeList.add("google.co.ke");
//            crawler.addToExcludeList(excludeList);

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

            controller.startNonBlocking(factory, 50);
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
                else if(input.toLowerCase().startsWith(LIST.toLowerCase()))
                {
                    List<Website> websites;
                    String[] vars = input.split(" ");
                    if (vars.length > 1)
                    {
                        try
                        {
                            Integer limit = Integer.parseInt(vars[1]);
                            if (limit <= 0)
                            {
                                limit = 100;
                            }
                            websites = websiteService.get(limit);
                        }
                        catch (Exception ex)
                        {
                            websites = websiteService.get();
                        }
                    }
                    else
                    {
                        websites = websiteService.get();
                    }

                    for (Website website: websites)
                    {
                        System.out.println(website.getUrl());
                    }
                }
                else if (input.toLowerCase().startsWith(ADD))
                {
                    String[] vars = input.split(" ");
                    List<String> searchRegexList = crawler.getSearchRegexList();
                    for(int idx = 1; idx < vars.length; idx++)
                    {
                        searchRegexList.add(vars[idx]);
                        log.info("Added '" + vars[idx] + "'");
                    }
                    crawler.setSearchRegexList(searchRegexList);
                }
                else if (input.toLowerCase().startsWith(REMOVE))
                {
                    String[] vars = input.split(" ");
                    List<String> searchRegexList = crawler.getSearchRegexList();
                    for(int idx = 1; idx < vars.length; idx++)
                    {
                        searchRegexList.remove(vars[idx]);
                        log.info("Removed '" + vars[idx] + "'");
                    }
                    crawler.setSearchRegexList(searchRegexList);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
