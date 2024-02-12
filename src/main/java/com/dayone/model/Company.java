package com.dayone.model;

import com.dayone.persist.entity.CompanyEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

  private String ticker;
  private String name;

  public Company(CompanyEntity companyEntity) {
    this.ticker = companyEntity.getTicker();
    this.name = companyEntity.getName();
  }
}
