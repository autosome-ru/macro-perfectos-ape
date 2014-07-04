package ru.autosome.perfectosape.calculations.findPvalue;

import gnu.trove.map.TDoubleDoubleMap;
import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.ScoringModelDistibutions;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.motifModels.Discretable;
import ru.autosome.perfectosape.motifModels.ScoringModel;

public abstract class FindPvalueByDiscretization <ModelType extends Discretable<ModelType> & ScoringModel, BackgroundType extends GeneralizedBackgroundModel> implements CanFindPvalue {
  final Discretizer discretizer;
  final ModelType motif;
  final BackgroundType background;

  abstract ScoringModelDistibutions discretedScoringModel();

  FindPvalueByDiscretization(ModelType motif, BackgroundType background, Discretizer discretizer) {
    this.motif = motif;
    this.background = background;
    this.discretizer = discretizer;
  }

  PvalueInfo infos_by_count(TDoubleDoubleMap counts, double non_upscaled_threshold) {
    double count = counts.get(discretizer.upscale(non_upscaled_threshold));
    double vocabularyVolume = Math.pow(background.volume(), motif.length());
    double pvalue = count / vocabularyVolume;
    return new PvalueInfo(non_upscaled_threshold, pvalue);
  }

  @Override
  public PvalueInfo[] pvaluesByThresholds(double[] thresholds) throws HashOverflowException {
    TDoubleDoubleMap counts = discretedScoringModel().counts_above_thresholds(discretizer.upscale(thresholds));

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
    infos.add_parameter("V", "discretization value", discretizer);
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
}
