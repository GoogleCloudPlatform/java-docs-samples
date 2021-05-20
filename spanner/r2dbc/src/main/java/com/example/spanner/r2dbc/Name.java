package com.example.spanner.r2dbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("NAMES")
public class Name {

  @Id
  @Column("UUID")
  private String uuid;

  @Column("NAME")
  private String name;

  public Name(String uuid, String name) {
    this.uuid = uuid;
    this.name = name;
  }

  public String getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }
}
