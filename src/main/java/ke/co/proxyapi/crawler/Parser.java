package ke.co.proxyapi.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import ke.co.proxyapi.crawler.persistence.services.WebsiteService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Setter
@Getter
public class Parser extends WebCrawler {
  @Autowired private WebsiteService websiteService;
  private List<List<String>> searchRegexList = new ArrayList<>();
  private List<String> inclusionDomains = new ArrayList<>();
  private List<String> exclusionDomains = new ArrayList<>();

  public Parser() {
    exclusionDomains.add("google.com");
    exclusionDomains.add("google.co.ke");
    exclusionDomains.add("googleusercontent.com");
    exclusionDomains.add("googleusercontent.co.ke");
    exclusionDomains.add("yahoo.com");
    exclusionDomains.add("bing.com");
  }

  public void addToExcludeList(List<String> excludeList) {
    exclusionDomains.addAll(excludeList);
  }

  /**
   * Checks whether a page is worth visiting or not.By default, JS and CSS files are skipped as they
   * don't contain the necessary data we need. It also automatically allows domains included in
   * 'autoIncludeDomains' list.
   *
   * @param referringPage
   * @param url
   * @return
   */
  @Override
  public boolean shouldVisit(Page referringPage, WebURL url) {
    if (url.toString().endsWith(".css") || url.toString().endsWith(".js")) {
      return false;
    }

    for (String domain : inclusionDomains) {
      if (domain.equals(url.getDomain())) {
        return true;
      }
    }
    return super.shouldVisit(referringPage, url);
  }

  /**
   * Parses the HTML from the page and checks whether the criteria set in the 'searchRegexList' are
   * found. If so, the page is saved to Database. If not, its skipped. By default, Google, Yahoo and
   * Bing pages are not saved and are included in the 'excludeOnSaveDomains' list as they are search
   * results, not actual data we need.
   *
   * @param page
   */
  @Override
  public void visit(Page page) {
    if (searchRegexList.size() <= 0) {
      return;
    }

    // execute the actual crawling on a separate thread. The current thread quickly hands off the
    // time-consuming parsing
    // to a separate thread and can pick another URL to check in the meantime
    Crawler crawler = new Crawler(page, searchRegexList);
    (new Thread(crawler)).start();
  }
}
