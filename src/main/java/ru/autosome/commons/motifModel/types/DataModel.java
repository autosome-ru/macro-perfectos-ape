package ru.autosome.commons.motifModel.types;

public enum DataModel {
  PCM, PPM, PWM ;

  public static DataModel fromString(String s) {
    if (s.toUpperCase().equals("PWM")) {
      return PWM;
    } else if (s.toUpperCase().equals("PCM")) {
      return PCM;
    } else if (s.toUpperCase().equals("PPM")) {
      return PPM;
    } else {
      throw new IllegalArgumentException("Unknown data model");
    }
  }
}
