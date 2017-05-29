package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Discretizer;

public class FoundedPvalueInfo {
  public final double threshold;
  public final double pvalue;

  public FoundedPvalueInfo(double threshold, double pvalue) {
    this.threshold = threshold;
    this.pvalue = pvalue;
  }

  public double numberOfRecognizedWords(GeneralizedBackgroundModel background, int length) {
    return pvalue * Math.pow(background.volume(), length);
  }

  public FoundedPvalueInfo downscale(Discretizer discretizer) {
    return new FoundedPvalueInfo(discretizer.downscale(threshold), pvalue);
  }
}
