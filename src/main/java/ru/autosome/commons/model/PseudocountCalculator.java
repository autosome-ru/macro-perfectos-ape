package ru.autosome.commons.model;

public abstract class PseudocountCalculator {
  abstract public Double calculatePseudocount(double count);
  static public final PseudocountCalculator logPseudocount = new PseudocountCalculator() {
    @Override
    public Double calculatePseudocount(double count) {
      return Math.log(Math.max(count, 2));
    }
  };
  static public final PseudocountCalculator sqrtPseudocount = new PseudocountCalculator() {
    @Override
    public Double calculatePseudocount(double count) {
      return Math.sqrt(count);
    }
  };
  static public PseudocountCalculator constPseudocount(final double pseudocount){
    return new PseudocountCalculator() {
      @Override
      public Double calculatePseudocount(double count) {
        return pseudocount;
      }
    };
  }
  static public PseudocountCalculator fromString(String s) {
    if (s.toLowerCase().equals("log")) {
      return logPseudocount;
    } else if (s.toLowerCase().equals("sqrt")) {
      return sqrtPseudocount;
    } else {
      return constPseudocount(Double.valueOf(s));
    }
  }
}
