package com.dayone.service;

import com.dayone.exception.impl.NoCompanyException;
import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.CacheKey;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
  public ScrapedResult getScrapedResultByCompanyName(String companyName) {

    CompanyEntity companyEntity = companyRepository.findByName(companyName)
        .orElseThrow(NoCompanyException::new);

    List<DividendEntity> dividendEntities = dividendRepository.findAllByCompanyId(
        companyEntity.getId());

    List<Dividend> dividends = dividendEntities.stream()
        .map(e -> new Dividend(e.getDate(), e.getDividend()))
        .collect(Collectors.toList());

    return new ScrapedResult(new Company(companyEntity), dividends);
  }
}
