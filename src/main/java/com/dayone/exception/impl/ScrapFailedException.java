package com.dayone.exception.impl;

import com.dayone.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ScrapFailedException extends AbstractException {

  public ScrapFailedException(String scrapTarget) {
    this.scrapTarget = scrapTarget;
  }

  private String scrapTarget;

  @Override
  public int getStatusCode() {
    return HttpStatus.INTERNAL_SERVER_ERROR.value();
  }

  @Override
  public String getMessage() {
    return scrapTarget + "정보 스크래핑에 실패했습니다.";
  }

}
