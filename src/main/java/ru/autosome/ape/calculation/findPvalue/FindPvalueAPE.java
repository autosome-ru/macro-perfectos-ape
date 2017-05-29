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

  final FindPvalueExact<ModelType, BackgroundType> pvalueCalculator;
  final Discretizer discretizer;

  public FindPvalueAPE(ModelType motif, BackgroundType background, Discretizer discretizer) {
    this.pvalueCalculator = new FindPvalueExact<>(motif.discrete(discretizer), background);
    this.discretizer = discretizer;
  }

  @Override
  public List<FoundedPvalueInfo> pvaluesByThresholds(List<Double> thresholds) {
    List<FoundedPvalueInfo> infos_upscaled = pvalueCalculator.pvaluesByThresholds(discretizer.upscale(thresholds));

    List<FoundedPvalueInfo> infos = new ArrayList<>();
    for (FoundedPvalueInfo info_upscaled: infos_upscaled) {
      infos.add(info_upscaled.downscale(discretizer));
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
    ReportListLayout<FoundedPvalueInfo> layout = pvalueCalculator.report_table_layout();
    layout.add_parameter("V", "discretization value", discretizer);

    return layout;
  }
}
