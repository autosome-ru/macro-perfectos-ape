package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;

import java.util.List;

// TODO: Make use of strong/weak thresholds or thresholds depending on BoundaryType (it wasn't implemented for bsearch lists)
public interface CanFindThreshold {
  class ThresholdInfo {
    public final double threshold;
    public final double real_pvalue;
    public final double expected_pvalue;

    public ThresholdInfo(double threshold, double real_pvalue, double expected_pvalue) {
      this.threshold = threshold;
      this.real_pvalue = real_pvalue;
      this.expected_pvalue = expected_pvalue;
    }

    public double numberOfRecognizedWords(GeneralizedBackgroundModel background, int length) {
      return real_pvalue * Math.pow(background.volume(), length);
    }

    // generate infos for non-discreeted matrix from infos for discreeted matrix
    public ThresholdInfo downscale(Discretizer discretizer) {
      return new ThresholdInfo(discretizer.downscale(threshold), real_pvalue, expected_pvalue);
    }
  }

  ThresholdInfo weakThresholdByPvalue(double pvalue);
  ThresholdInfo strongThresholdByPvalue(double pvalue);
  ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType);

  List<ThresholdInfo> weakThresholdsByPvalues(List<Double> pvalues);
  List<ThresholdInfo> strongThresholsdByPvalues(List<Double> pvalues);
  List<ThresholdInfo> thresholdsByPvalues(List<Double> pvalues, BoundaryType boundaryType);
}
