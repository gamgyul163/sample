package com.dayone.scheduler;

import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.CacheKey;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.scraper.Scraper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  private final Scraper yahooFinanceScraper;

  @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
  @Scheduled(cron = "${scheduler.scrap.yahoo}")
  public void yahooFinanceScheduling() {
    log.info("scraping scheduler is started");
    List<CompanyEntity> companyEntities = this.companyRepository.findAll();

    for (var companyEntity : companyEntities) {
      log.info("scraping scheduler is started -> " + companyEntity.getName());
      ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new Company(companyEntity));

      scrapedResult.getDividends().stream()
          .map(e -> new DividendEntity(companyEntity.getId(), e))
          .forEach(e -> {
            if (!dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate())) {
              dividendRepository.save(e);
            }
          });
      try {
        Thread.sleep(3000); // 3 seconds
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    log.info("scraping scheduler is ended");
  }
}
