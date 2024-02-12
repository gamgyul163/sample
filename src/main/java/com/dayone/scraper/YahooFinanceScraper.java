package com.dayone.scraper;

import com.dayone.exception.impl.NoCompanyException;
import com.dayone.exception.impl.ScrapFailedException;
import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.Month;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class YahooFinanceScraper implements Scraper {

  private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
  private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

  private static final long START_TIME = 86400;   // 60 * 60 * 24

  private String scrapTarget;

  @Override
  public ScrapedResult scrap(Company company) {
    log.info("scraping dividends is started");
    scrapTarget = "배당금";
    var scrapResult = new ScrapedResult();
    scrapResult.setCompany(company);

    try {
      long now = System.currentTimeMillis() / 1000;

      String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
      Connection connection = Jsoup.connect(url);
      Document document = connection.get();

      Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
      Element tableEle = parsingDivs.get(0);

      Element tbody = tableEle.children().get(1);

      List<Dividend> dividends = new ArrayList<>();
      for (Element e : tbody.children()) {
        String txt = e.text();
        if (!txt.endsWith("Dividend")) {
          continue;
        }
        String[] splits = txt.split(" ");
        int month = Month.strToNumber(splits[0]);
        int day = Integer.valueOf(splits[1].replace(",", ""));
        int year = Integer.valueOf(splits[2]);
        String dividend = splits[3];

        if (month < 0) {
          throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
        }
        dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
      }
      scrapResult.setDividends(dividends);

    } catch (Exception e) {
      throw new ScrapFailedException(scrapTarget);
    }
    log.info("scraping dividends is ended");
    return scrapResult;
  }

  @Override
  public Company scrapCompanyByTicker(String ticker) {
    log.info("scraping company is started");
    scrapTarget = "회사";
    String url = String.format(SUMMARY_URL, ticker, ticker);

    try {
      Document document = Jsoup.connect(url).get();
      Element titleEle = document.getElementsByTag("h1").get(0);
      String title = titleEle.text().split(" - ")[1].trim();

      log.info("scraping company is ended");
      return new Company(ticker, title);
    } catch (IndexOutOfBoundsException e) {
      throw new NoCompanyException();
    } catch (Exception e) {
      throw new ScrapFailedException(scrapTarget);
    }
  }
}
