package ru.autosome.ape.calculation.findPvalue;

import gnu.trove.map.TDoubleDoubleMap;
import ru.autosome.ape.calculation.ScoringModelDistributions.ScoringModelDistributions;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.ReportListLayout;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.motifModel.ScoreDistribution;

import java.util.ArrayList;
import java.util.List;

public class FindPvalueAPE<ModelType extends Discretable<ModelType> & HasLength & ScoreDistribution<BackgroundType>,
                          BackgroundType extends GeneralizedBackgroundModel> implements CanFindPvalue {

  final Discretizer discretizer;
  final ModelType motif;
  final BackgroundType background;

  public FindPvalueAPE(ModelType motif, BackgroundType background, Discretizer discretizer) {
    this.motif = motif;
    this.background = background;
    this.discretizer = discretizer;
  }

  @Override
  public List<PvalueInfo> pvaluesByThresholds(List<Double> thresholds) {
    double vocabularyVolume = Math.pow(background.volume(), motif.length());
    ModelType discreted_motif = motif.discrete(discretizer);
    ScoringModelDistributions discretedScoringModel = discreted_motif.scoringModel(background);
    List<Double> upscaled_thresholds = discretizer.upscale(thresholds);
    TDoubleDoubleMap counts = discretedScoringModel.counts_above_thresholds(upscaled_thresholds);

    List<PvalueInfo> infos = new ArrayList<>();
    for (double threshold: thresholds) {
      double upscaled_threshold = discretizer.upscale(threshold);
      double count = counts.get(upscaled_threshold);
      double pvalue = count / vocabularyVolume;
      infos.add(new PvalueInfo(threshold, pvalue));
    }
    return infos;
  }

  @Override
  public PvalueInfo pvalueByThreshold(double threshold) {
    List<Double> thresholds = new ArrayList<>();
    thresholds.add(threshold);
    return pvaluesByThresholds(thresholds).get(0);
  }

  @Override
  public ReportListLayout<PvalueInfo> report_table_layout() {
    ReportListLayout<PvalueInfo> infos = new ReportListLayout<>();
    infos.add_parameter("V", "discretization value", discretizer);
    infos.background_parameter("B", "background", background);

    infos.add_table_parameter("T", "threshold", (PvalueInfo cell) -> cell.threshold);
    if (background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", (PvalueInfo cell) -> {
          double numberOfRecognizedWords = cell.numberOfRecognizedWords(background, motif.length());
          return (long)numberOfRecognizedWords;
        });
    }
    infos.add_table_parameter("P", "P-value", (PvalueInfo cell) -> cell.pvalue);

    return infos;
  }
}
