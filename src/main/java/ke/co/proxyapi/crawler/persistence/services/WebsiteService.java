package ke.co.proxyapi.crawler.persistence.services;

import ke.co.proxyapi.crawler.persistence.models.Website;
import ke.co.proxyapi.crawler.persistence.repositories.WebsiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class WebsiteService
{
    @Autowired
    private WebsiteRepository websiteRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Website save(Website website)
    {
        Optional<Website> dbWebsite = websiteRepository.findByUrl(website.getUrl());
        if (dbWebsite.isPresent())
        {
            return dbWebsite.get();
        }
        return websiteRepository.save(website);
    }
}
