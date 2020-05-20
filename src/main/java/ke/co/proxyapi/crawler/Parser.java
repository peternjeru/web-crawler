package ke.co.proxyapi.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import ke.co.proxyapi.crawler.persistence.models.Website;
import ke.co.proxyapi.crawler.persistence.services.WebsiteService;
import ke.co.proxyapi.crawler.persistence.utils.AppContext;
import ke.co.proxyapi.crawler.persistence.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser implements Runnable {
  private WebsiteService websiteService;

  private List<String> excludeOnSaveDomains;
  private Page page;
  private List<List<String>> searchRegexList;

  public Parser(Page page, List<List<String>> searchRegexList) {
    this.page = page;
    this.searchRegexList = searchRegexList;

    websiteService = AppContext.getApplicationContext().getBean(WebsiteService.class);

    excludeOnSaveDomains = new ArrayList<>();
    excludeOnSaveDomains.add("google.com");
    excludeOnSaveDomains.add("google.co.ke");
    excludeOnSaveDomains.add("googleusercontent.com");
    excludeOnSaveDomains.add("googleusercontent.co.ke");
    excludeOnSaveDomains.add("yahoo.com");
    excludeOnSaveDomains.add("bing.com");
  }

  @Override
  public void run() {
    if (page == null || searchRegexList == null) {
      throw new NullPointerException("Page and Search list not set");
    }

    WebURL url = page.getWebURL();

    if (page.getParseData() instanceof HtmlParseData) {
      HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
      String html = htmlParseData.getHtml();
      boolean save = true;

      for (List<String> stringList : searchRegexList) {
        for (String searchStr : stringList) {
          Pattern pattern = Pattern.compile("\\b(" + searchStr + ")\\b", Pattern.CASE_INSENSITIVE);
          Matcher matcher = pattern.matcher(html);
          if (!matcher.find()) {
            save = false;
            break;
          }
        }

        if (save && !in(url.getDomain())) {
          save(url.getURL());
        }
      }
    }
  }

  private boolean in(String domainHaystack) {
    for (String needle : excludeOnSaveDomains) {
      if (domainHaystack.toLowerCase().endsWith(needle.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private void save(String url) {
    try {
      String hash = StringUtils.hashString(url);
      Website website = Website.builder().processed(false).url(url).urlHash(hash).build();
      websiteService.save(website);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
