package com.dayone.service;

import com.dayone.exception.impl.AlreadyExistCompanyException;
import com.dayone.exception.impl.NoCompanyException;
import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.scraper.Scraper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CompanyService {

  private final Trie trie;
  private final Scraper yahooFinanceScraper;
  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  public Company save(String ticker) {
    if (companyRepository.existsByTicker(ticker)) {
      throw new AlreadyExistCompanyException();
    }
    return storeCompanyAndDividend(ticker);
  }

  public Page<CompanyEntity> getAllCompany(Pageable pageable) {
    return companyRepository.findAll(pageable);
  }

  private Company storeCompanyAndDividend(String ticker) {
    Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);

    ScrapedResult scrapedResult = yahooFinanceScraper.scrap(company);

    CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));

    List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
        .map(e -> new DividendEntity(companyEntity.getId(), e)).collect(Collectors.toList());

    dividendRepository.saveAll(dividendEntities);

    return company;
  }

  public List<String> getCompanyNamesByKeyword(String keyword) {
    Page<CompanyEntity> companyEntities = companyRepository.findByNameStartingWithIgnoreCase(
        keyword, PageRequest.of(0, 10));
    return companyEntities.stream().map(e -> e.getName()).collect(Collectors.toList());
  }

  public void addAutocompleteKeyword(String keyword) {
    this.trie.put(keyword, null);
  }

  public List<String> autocomplete(String keyword) {
    return (List<String>) this.trie.prefixMap(keyword).keySet()
        .stream()
        .collect(Collectors.toList());
  }

  public void deleteAutocompleteKeyword(String keyword) {
    this.trie.remove(keyword);
  }

  public String deleteCompany(String ticker) {
    CompanyEntity companyEntity = companyRepository.findByTicker(ticker)
        .orElseThrow(() -> new NoCompanyException());

    dividendRepository.deleteAllByCompanyId(companyEntity.getId());
    companyRepository.delete(companyEntity);

    deleteAutocompleteKeyword(companyEntity.getName());
    return companyEntity.getName();
  }

}
