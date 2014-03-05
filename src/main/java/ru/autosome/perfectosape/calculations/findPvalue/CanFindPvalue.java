package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.formatters.ResultInfo;
import ru.autosome.perfectosape.motifModels.DiPWM;
import ru.autosome.perfectosape.motifModels.PWM;

public interface CanFindPvalue {
  public static class PvalueInfo extends ResultInfo {
    public final double threshold;
    public final double pvalue;

    public PvalueInfo(double threshold, double pvalue) {
      this.threshold = threshold;
      this.pvalue = pvalue;
    }

    public double numberOfRecognizedWords(GeneralizedBackgroundModel background, int length) {
      return pvalue * Math.pow(background.volume(), length);
    };
  }

  public OutputInformation report_table_layout();

  public PvalueInfo[] pvaluesByThresholds(double[] thresholds) throws HashOverflowException;

  public PvalueInfo pvalueByThreshold(double threshold) throws HashOverflowException;

  public static interface Builder {
    public CanFindPvalue build();
  }
  public static interface PWMBuilder extends Builder {
    public Builder applyMotif(PWM pwm);
  }
  public static interface DiPWMBuilder extends Builder {
    public Builder applyMotif(DiPWM dipwm);
  }
}
