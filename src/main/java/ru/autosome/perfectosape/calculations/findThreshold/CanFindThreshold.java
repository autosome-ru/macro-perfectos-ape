package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.formatters.ResultInfo;
import ru.autosome.perfectosape.motifModels.DiPWM;
import ru.autosome.perfectosape.motifModels.PWM;

// TODO: Make use of strong/weak thresholds or thresholds depending on BoundaryType (it wasn't implemented for bsearch lists
public interface CanFindThreshold {
  class ThresholdInfo extends ResultInfo {
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
    };


    // generate infos for non-disreeted matrix from infos for discreeted matrix
    public ThresholdInfo downscale(Double discretization) {
      if (discretization == null) {
        return this;
      } else {
        return new ThresholdInfo(threshold / discretization, real_pvalue, expected_pvalue);
      }
    }
  }

  ThresholdInfo weakThresholdByPvalue(double pvalue) throws HashOverflowException;
  ThresholdInfo strongThresholdByPvalue(double pvalue) throws HashOverflowException;
  ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) throws HashOverflowException;

  ThresholdInfo[] weakThresholdsByPvalues(double[] pvalues) throws HashOverflowException;
  ThresholdInfo[] strongThresholsdByPvalues(double[] pvalues) throws HashOverflowException;
  ThresholdInfo[] thresholdsByPvalues(double[] pvalues, BoundaryType boundaryType) throws HashOverflowException;

  public static interface Builder {
    public CanFindThreshold build();
  }
  public static interface PWMBuilder extends Builder {
    public Builder applyMotif(PWM pwm);
  }
  public static interface DiPWMBuilder extends Builder {
    public Builder applyMotif(DiPWM dipwm);
  }
}
