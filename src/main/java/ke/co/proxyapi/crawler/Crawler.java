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

    @Override
    public void visit(Page page)
    {
        if (searchRegexList.size() <= 0)
        {
            return;
        }

        Page currPage = page;
        WebURL url = currPage.getWebURL();
        log.info("Current URL: " + url.getURL());
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

            if(save)
            {
                save(url.getURL());
            }
        }
    }

    public void save(String url)
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
