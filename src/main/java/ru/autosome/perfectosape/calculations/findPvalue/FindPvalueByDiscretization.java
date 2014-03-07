package ru.autosome.perfectosape.calculations.findPvalue;

import gnu.trove.map.TDoubleDoubleMap;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.ScoringModelDistibutions;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.motifModels.Discretable;
import ru.autosome.perfectosape.motifModels.PWM;
import ru.autosome.perfectosape.motifModels.ScoringModel;

public abstract class FindPvalueByDiscretization <ModelType extends Discretable<ModelType> & ScoringModel, BackgroundType extends GeneralizedBackgroundModel> implements CanFindPvalue {
  Double discretization; // if discretization is null - it's not applied
  ModelType motif;
  BackgroundType background;

  abstract ScoringModelDistibutions countingPWM();

  FindPvalueByDiscretization(ModelType motif, BackgroundType background, Double discretization) {
    this.motif = motif;
    this.background = background;
    this.discretization = discretization;
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
    double vocabularyVolume = Math.pow(background.volume(), motif.length());
    double pvalue = count / vocabularyVolume;
    return new PvalueInfo(non_upscaled_threshold, pvalue);
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
          double numberOfRecognizedWords = cell.numberOfRecognizedWords(background, motif.length());
          return (long)numberOfRecognizedWords;
        }
      });
    }
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }

  public static class Builder extends FindPvalueBuilder<PWM> {
    Double discretization;
    BackgroundModel background;
    Integer maxHashSize;

    public Builder(Double discretization, BackgroundModel background, Integer maxHashSize) {
      this.discretization = discretization;
      this.background = background;
      this.maxHashSize = maxHashSize;
    }

    @Override
    public CanFindPvalue pvalueCalculator() {
      return new FindPvalueAPE(motif, background, discretization, maxHashSize);
    }
  }
}
