package ru.autosome.ape.calculation.findPvalue;

import gnu.trove.map.TDoubleDoubleMap;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistributions;

public abstract class FindPvalueByDiscretization <ModelType extends Discretable<ModelType> & HasLength,
                                                  BackgroundType extends GeneralizedBackgroundModel> implements CanFindPvalue {

  final Discretizer discretizer;
  final ModelType motif;
  final BackgroundType background;

  abstract ScoringModelDistributions discretedScoringModel();

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
  public PvalueInfo[] pvaluesByThresholds(double[] thresholds) {
    TDoubleDoubleMap counts = discretedScoringModel().counts_above_thresholds(discretizer.upscale(thresholds));

    PvalueInfo[] infos = new PvalueInfo[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      infos[i] = infos_by_count(counts, thresholds[i]);
    }
    return infos;
  }

  @Override
  public PvalueInfo pvalueByThreshold(double threshold) {
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
