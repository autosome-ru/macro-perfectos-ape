package ru.autosome.commons.model;

// STRONG -- stronger threshold (lower P-value), less recognized words
// WEAK - weak threshold (higher P-value), more recognized words
public enum BoundaryType {
  STRONG, WEAK;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }

  public static BoundaryType fromString(String str) {
    switch (str.toLowerCase()) {
      case "strong":
      case "lower":
        return STRONG;
      case "weak":
      case "upper":
        return WEAK;
      default:
        throw new IllegalArgumentException("Boundary type can be either strong(lower) or weak(upper) but was " + str);
    }
  }
}
