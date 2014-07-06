package ru.autosome.commons.model;

public enum BoundaryType {LOWER, UPPER;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
