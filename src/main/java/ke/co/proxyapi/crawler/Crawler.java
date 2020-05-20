package ke.co.proxyapi.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import ke.co.proxyapi.crawler.persistence.models.Website;
import ke.co.proxyapi.crawler.persistence.services.WebsiteService;
import ke.co.proxyapi.crawler.persistence.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Setter
@Getter
public class Crawler extends WebCrawler
{
    @Autowired
    private WebsiteService websiteService;
    private List<String> searchRegexList = new ArrayList<>();
    private List<String> autoIncludeDomains = new ArrayList<>();
    private List<String> excludeOnSaveDomains = new ArrayList<>();

    public Crawler()
    {
        excludeOnSaveDomains.add("google.com");
        excludeOnSaveDomains.add("google.co.ke");
        excludeOnSaveDomains.add("googleusercontent.com");
        excludeOnSaveDomains.add("googleusercontent.co.ke");
        excludeOnSaveDomains.add("yahoo.com");
        excludeOnSaveDomains.add("bing.com");
    }

    public void addToExcludeList(List<String> excludeList)
    {
        excludeOnSaveDomains.addAll(excludeList);
    }

    /**
     * Checks whether a page is worth visiting or not.By default, JS and CSS files are skipped as they don't contain
     * the necessary data we need. It also automatically allows domains included in 'autoIncludeDomains' list.
     *
     *
     * @param referringPage
     * @param url
     * @return
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url)
    {
        if(url.toString().endsWith(".css") || url.toString().endsWith(".js"))
        {
            return false;
        }

        for (String domain: autoIncludeDomains)
        {
            if(domain.equals(url.getDomain()))
            {
                return true;
            }
        }
        return super.shouldVisit(referringPage, url);
    }

    /**
     * Parses the HTML from the page and checks whether the criteria set in the 'searchRegexList' are found. If so,
     * the page is saved to Database. If not, its skipped. By default, Google, Yahoo and Bing pages are not saved and
     * are included in the 'excludeOnSaveDomains' list as they are search results, not actual data we need.
     *
     * @param page
     */
    @Override
    public void visit(Page page)
    {
        if (searchRegexList.size() <= 0)
        {
            return;
        }

        Page currPage = page;
        WebURL url = currPage.getWebURL();

        if (currPage.getParseData() instanceof HtmlParseData)
        {
            HtmlParseData htmlParseData = (HtmlParseData) currPage.getParseData();
            String html = htmlParseData.getHtml();
            boolean save = true;

            for (String searchStr: searchRegexList)
            {
                Pattern pattern = Pattern.compile("\\b(" + searchStr + ")\\b", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(html);
                if(!matcher.find())
                {
                    save = false;
                    break;
                }
            }

            if(save && !in(url.getDomain()))
            {
                save(url.getURL());
            }
        }
    }

    private boolean in(String domainHaystack)
    {
        for (String needle: excludeOnSaveDomains)
        {
            if (domainHaystack.toLowerCase().endsWith(needle.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    private void save(String url)
    {
        try
        {
            String hash = StringUtils.hashString(url);
            Website website = Website.builder()
                    .processed(false)
                    .url(url)
                    .urlHash(hash)
                    .build();
            websiteService.save(website);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
