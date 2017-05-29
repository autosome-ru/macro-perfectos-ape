package ru.autosome.ape.calculation.findPvalue;

import gnu.trove.map.TDoubleDoubleMap;
import ru.autosome.ape.calculation.ScoringModelDistributions.ScoringModelDistributions;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.ReportListLayout;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.motifModel.ScoreDistribution;

import java.util.ArrayList;
import java.util.List;

public class FindPvalueExact<ModelType extends HasLength & ScoreDistribution<BackgroundType>,
                              BackgroundType extends GeneralizedBackgroundModel> implements CanFindPvalue {

  final ModelType motif;
  final BackgroundType background;

  public FindPvalueExact(ModelType motif, BackgroundType background) {
    this.motif = motif;
    this.background = background;
  }

  @Override
  public List<FoundedPvalueInfo> pvaluesByThresholds(List<Double> thresholds) {
    double vocabularyVolume = Math.pow(background.volume(), motif.length());
    ScoringModelDistributions scoringModel = motif.scoringModel(background);
    TDoubleDoubleMap counts = scoringModel.counts_above_thresholds(thresholds);

    List<FoundedPvalueInfo> infos = new ArrayList<>();
    for (double threshold: thresholds) {
      double count = counts.get(threshold);
      double pvalue = count / vocabularyVolume;
      infos.add(new FoundedPvalueInfo(threshold, pvalue));
    }
    return infos;
  }

  @Override
  public FoundedPvalueInfo pvalueByThreshold(double threshold) {
    List<Double> thresholds = new ArrayList<>();
    thresholds.add(threshold);
    return pvaluesByThresholds(thresholds).get(0);
  }

  @Override
  public ReportListLayout<FoundedPvalueInfo> report_table_layout() {
    ReportListLayout<FoundedPvalueInfo> layout = new ReportListLayout<>();
    layout.background_parameter("B", "background", background);

    layout.add_table_parameter("T", "threshold", (FoundedPvalueInfo cell) -> cell.threshold);
    if (background.is_wordwise()) {
      layout.add_table_parameter("W", "number of recognized words", (FoundedPvalueInfo cell) -> {
        double numberOfRecognizedWords = cell.numberOfRecognizedWords(background, motif.length());
        return (long)numberOfRecognizedWords;
      });
    }
    layout.add_table_parameter("P", "P-value", (FoundedPvalueInfo cell) -> cell.pvalue);

    return layout;
  }
}
