package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.ape.model.ThresholdPvaluePair;
import ru.autosome.commons.model.BoundaryType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FindThresholdBsearch implements CanFindThreshold  {
  final PvalueBsearchList bsearchList;

  public FindThresholdBsearch(PvalueBsearchList bsearchList) {
    this.bsearchList = bsearchList;
  }
  public FindThresholdBsearch(File thresholds_file) throws FileNotFoundException {
    this.bsearchList = PvalueBsearchList.load_from_file(thresholds_file);
  }

  @Override
  public FoundedThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) {
    ThresholdPvaluePair info = bsearchList.thresholdInfoByPvalue(pvalue, boundaryType);
    double threshold = info.threshold;
    double real_pvalue = info.pvalue;
    return new FoundedThresholdInfo(threshold, real_pvalue, pvalue);
  }

  @Override
  public List<FoundedThresholdInfo> thresholdsByPvalues(List<Double> pvalues, BoundaryType boundaryType) {
    List<FoundedThresholdInfo> result = new ArrayList<>();
    for (Double pvalue: pvalues) {
      result.add(thresholdByPvalue(pvalue, boundaryType));
    }
    return result;
  }
}
