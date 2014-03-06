package ru.autosome.perfectosape.calculations.findPvalue;

import gnu.trove.map.TDoubleDoubleMap;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.ScoringModelDistibutions;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.calculations.CountingPWM;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindPvalueAPE implements CanFindPvalue {
  public static class Builder implements CanFindPvalue.Builder<PWM> {
    Double discretization;
    BackgroundModel background;
    Integer maxHashSize;
    PWM pwm;

    public Builder(Double discretization, BackgroundModel background, Integer maxHashSize) {
      this.discretization = discretization;
      this.background = background;
      this.maxHashSize = maxHashSize;
    }

    @Override
    public CanFindPvalue.Builder applyMotif(PWM pwm) {
      this.pwm = pwm;
      return this;
    }

    @Override
    public CanFindPvalue build() {
      if (pwm != null) {
        return new FindPvalueAPE(pwm, discretization, background, maxHashSize);
      } else {
        return null;
      }
    }
  }

  PWM pwm;
  Double discretization;
  BackgroundModel background;
  Integer maxHashSize;

  public FindPvalueAPE(PWM pwm, Double discretization, BackgroundModel background, Integer maxHashSize) {
    this.pwm = pwm;
    this.discretization = discretization;
    this.background = background;
    this.maxHashSize = maxHashSize;
  }

  double upscale_threshold(double threshold) {
    if (discretization == null) {
      return threshold;
    } else {
      return threshold * discretization;
    }
  }
  double[] upscaled_thresholds(double[] thresholds) {
    double[] result = new double[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      result[i] = upscale_threshold(thresholds[i]);
    }
    return result;
  }

  PvalueInfo infos_by_count(TDoubleDoubleMap counts, double non_upscaled_threshold) {
    double count = counts.get(upscale_threshold(non_upscaled_threshold));
    double vocabularyVolume = Math.pow(background.volume(), pwm.length());
    double pvalue = count / vocabularyVolume;
    return new PvalueInfo(non_upscaled_threshold, pvalue);
  }

  ScoringModelDistibutions countingPWM() {
    return new CountingPWM(pwm.discrete(discretization), background, maxHashSize);
  }

  @Override
  public PvalueInfo[] pvaluesByThresholds(double[] thresholds) throws HashOverflowException {
    ScoringModelDistibutions countingPWM = countingPWM();
    TDoubleDoubleMap counts = countingPWM.counts_above_thresholds(upscaled_thresholds(thresholds));

    PvalueInfo[] infos = new PvalueInfo[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      infos[i] = infos_by_count(counts, thresholds[i]);
    }
    return infos;
  }

  @Override
  public PvalueInfo pvalueByThreshold(double threshold) throws HashOverflowException {
    double[] thresholds = {threshold};
    return pvaluesByThresholds(thresholds)[0];
  }

  @Override
  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
    infos.add_parameter("V", "discretization value", discretization);
    infos.background_parameter("B", "background", background);

    infos.add_table_parameter("T", "threshold", "threshold");
    if (background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "numberOfRecognizedWords",new OutputInformation.Callback<PvalueInfo>() {
        @Override
        public Object run(PvalueInfo cell) {
          double numberOfRecognizedWords = cell.numberOfRecognizedWords(background, pwm.length());
          return (long)numberOfRecognizedWords;
        }
      });
    }
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }
}
