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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Component
public class Runner implements CommandLineRunner {
  @Autowired private Parser parser;
  @Autowired private WebsiteService websiteService;

  public static final String EXIT = "exit";
  public static final String LIST = "list";
  private final String path = "/Users/peternjeru/crawler";
  private final Integer crawlerNumber = 50;

  @Override
  public void run(String... args) throws Exception {
    try {
      if (!Files.exists(Paths.get(path))) {
        throw new IllegalArgumentException("No such folder: " + path);
      }

      List<List<String>> regexListPrim = new ArrayList<>();

      // This first one will match a search phrase such as "software engineer job kenya 2020" or
      // "software engineer vacancy kenya 2020"
      List<String> list1 = new ArrayList<>();
      list1.add("software engineer");
      list1.add("job|vacancy|position|opportunity");
      list1.add("kenya");
      list1.add("2020");

      // This will match a search phrase such as "software developer job kenya 2020" or "software
      // developer vacancy kenya 2020" e.t.c.,
      // and so on...
      List<String> list2 = new ArrayList<>();
      list2.add("software developer");
      list2.add("job|vacancy|position|opportunity");
      list2.add("kenya");
      list2.add("2020");

      List<String> list3 = new ArrayList<>();
      list3.add("senior software developer");
      list3.add("job|vacancy|position|opportunity");
      list3.add("kenya");
      list3.add("2020");

      List<String> list4 = new ArrayList<>();
      list4.add("senior software engineer");
      list4.add("job|vacancy|position|opportunity");
      list4.add("kenya");
      list4.add("2020");

      regexListPrim.add(list1);
      regexListPrim.add(list2);
      regexListPrim.add(list3);
      regexListPrim.add(list4);

      parser.setSearchRegexList(regexListPrim);

      // how to add exclusion domains (domains whose results should not be saved)
      //            List<String> excludeList = new ArrayList<>();
      //            excludeList.add("google.com");
      //            excludeList.add("google.co.ke");
      //            crawler.addToExcludeList(excludeList);

      CrawlConfig config = new CrawlConfig();
      config.setCrawlStorageFolder(path);
      config.setPolitenessDelay(500);
      config.setIncludeHttpsPages(true);
      config.setRespectNoFollow(false);
      config.setIncludeBinaryContentInCrawling(false);
      config.setShutdownOnEmptyQueue(true);
      config.setUserAgentString(
          "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
      config.setFollowRedirects(true);
      config.setResumableCrawling(true);
      config.setMaxDownloadSize(5242880);
      config.setCleanupDelaySeconds(60);
      PageFetcher pageFetcher = new PageFetcher(config);

      RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
      robotstxtConfig.setEnabled(false);
      RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

      CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
      for (List<String> stringList : regexListPrim) {
        String searchStr =
            String.join("+", stringList).replaceAll("\\|", "+").replaceAll("\\s{1,}", "+");
        log.info("Search: " + searchStr);
        controller.addSeed("https://www.google.com/search?q=" + searchStr);
      }

      CrawlController.WebCrawlerFactory<Parser> factory = () -> parser;

      controller.startNonBlocking(factory, crawlerNumber);
      log.info("Crawling Started");

      // provide way to cleanly shut down the service
      Scanner sc = new Scanner(System.in);
      while (sc.hasNextLine()) {
        String input = sc.nextLine();
        if (input.equalsIgnoreCase(EXIT)) {
          controller.shutdown();
          controller.waitUntilFinish();
          break;
        } else if (input.toLowerCase().startsWith(LIST.toLowerCase())) {
          List<Website> websites;
          String[] vars = input.split(" ");
          if (vars.length > 1) {
            try {
              Integer limit = Integer.parseInt(vars[1]);
              if (limit <= 0) {
                limit = 100;
              }
              websites = websiteService.get(limit);
            } catch (Exception ex) {
              websites = websiteService.get();
            }
          } else {
            websites = websiteService.get();
          }

          for (Website website : websites) {
            System.out.println(website.getUrl());
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
