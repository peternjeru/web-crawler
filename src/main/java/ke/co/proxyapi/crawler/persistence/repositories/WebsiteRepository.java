package ke.co.proxyapi.crawler.persistence.repositories;

import ke.co.proxyapi.crawler.persistence.models.Website;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebsiteRepository extends PagingAndSortingRepository<Website, Long> {
  Optional<Website> findByUrlHash(String urlHash);

  Page<Website> findAllByOrderByIdDesc(Pageable pageable);
}
