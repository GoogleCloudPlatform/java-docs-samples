package com.example.spanner;

import jakarta.persistence.Entity;

@Entity
public class TypedPerson extends Person{
  private String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
