package ru.autosome.commons.model;

// LOWER -- strong threshold, less recognized words
// UPPER - weak threshold, more recognized words
public enum BoundaryType {LOWER, UPPER;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
