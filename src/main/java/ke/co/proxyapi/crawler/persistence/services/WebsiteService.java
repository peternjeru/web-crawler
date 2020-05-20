package ke.co.proxyapi.crawler.persistence.services;

import ke.co.proxyapi.crawler.persistence.models.Website;
import ke.co.proxyapi.crawler.persistence.repositories.WebsiteRepository;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Configurable(autowire = Autowire.BY_TYPE)
public class WebsiteService {
  @Autowired private WebsiteRepository websiteRepository;

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public Website save(Website website) {
    Optional<Website> dbWebsite = websiteRepository.findByUrlHash(website.getUrlHash());
    if (dbWebsite.isPresent()) {
      return dbWebsite.get();
    }
    return websiteRepository.save(website);
  }

  public List<Website> get() {
    Pageable pageable = PageRequest.of(0, 100);
    Page<Website> page = websiteRepository.findAllByOrderByIdDesc(pageable);
    List<Website> content = page.getContent() == null ? new ArrayList<>() : page.getContent();
    return content;
  }

  public List<Website> get(Integer limit) {
    Pageable pageable = PageRequest.of(0, limit);
    Page<Website> page = websiteRepository.findAllByOrderByIdDesc(pageable);
    List<Website> content = page.getContent() == null ? new ArrayList<>() : page.getContent();
    return content;
  }
}
