package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.commons.model.BoundaryType;

import java.util.ArrayList;
import java.util.List;

public class FindThresholdBsearch implements CanFindThreshold  {
  final PvalueBsearchList bsearchList;

  FindThresholdBsearch(PvalueBsearchList bsearchList) {
    this.bsearchList = bsearchList;
  }

  @Override
  public CanFindThreshold.ThresholdInfo weakThresholdByPvalue(double pvalue) {
    PvalueBsearchList.ThresholdPvaluePair info = bsearchList.weakThresholdByPvalue(pvalue);
    double threshold = info.threshold;
    double real_pvalue = info.pvalue;
    return new CanFindThreshold.ThresholdInfo(threshold, real_pvalue, pvalue);
  }

  @Override
  public CanFindThreshold.ThresholdInfo strongThresholdByPvalue(double pvalue) {
    PvalueBsearchList.ThresholdPvaluePair info = bsearchList.strongThresholdInfoByPvalue(pvalue);
    double threshold = info.threshold;
    double real_pvalue = info.pvalue;
    return new CanFindThreshold.ThresholdInfo(threshold, real_pvalue, pvalue);
  }

  @Override
  public CanFindThreshold.ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) {
    if (boundaryType == BoundaryType.LOWER) {
      return strongThresholdByPvalue(pvalue);
    } else {
      return weakThresholdByPvalue(pvalue);
    }
  }

  @Override
  public List<CanFindThreshold.ThresholdInfo> weakThresholdsByPvalues(List<Double> pvalues) {
    List<CanFindThreshold.ThresholdInfo> result = new ArrayList<>();
    for (double pvalue: pvalues) {
      result.add(weakThresholdByPvalue(pvalue));
    }
    return result;
  }

  @Override
  public List<CanFindThreshold.ThresholdInfo> strongThresholsdByPvalues(List<Double> pvalues) {
    List<CanFindThreshold.ThresholdInfo> result = new ArrayList<>();
    for (double pvalue: pvalues) {
      result.add(strongThresholdByPvalue(pvalue));
    }
    return result;
  }

  @Override
  public List<CanFindThreshold.ThresholdInfo> thresholdsByPvalues(List<Double> pvalues, BoundaryType boundaryType) {
    List<CanFindThreshold.ThresholdInfo> result = new ArrayList<>();
    for (Double pvalue: pvalues) {
      result.add(thresholdByPvalue(pvalue, boundaryType));
    }
    return result;
  }
}
