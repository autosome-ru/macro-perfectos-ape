package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.calculations.HashOverflowException;

public class FindThresholdBsearch implements CanFindThreshold  {
  final PvalueBsearchList bsearchList;

  FindThresholdBsearch(PvalueBsearchList bsearchList) {
    this.bsearchList = bsearchList;
  }

  @Override
  public CanFindThreshold.ThresholdInfo weakThresholdByPvalue(double pvalue) throws HashOverflowException {
    PvalueBsearchList.ThresholdPvaluePair info = bsearchList.weakThresholdByPvalue(pvalue);
    double threshold = info.threshold;
    double real_pvalue = info.pvalue;
    return new CanFindThreshold.ThresholdInfo(threshold, real_pvalue, pvalue);
  }

  @Override
  public CanFindThreshold.ThresholdInfo strongThresholdByPvalue(double pvalue) throws HashOverflowException {
    PvalueBsearchList.ThresholdPvaluePair info = bsearchList.strongThresholdInfoByPvalue(pvalue);
    double threshold = info.threshold;
    double real_pvalue = info.pvalue;
    return new CanFindThreshold.ThresholdInfo(threshold, real_pvalue, pvalue);
  }

  @Override
  public CanFindThreshold.ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) throws HashOverflowException {
    if (boundaryType == BoundaryType.LOWER) {
      return strongThresholdByPvalue(pvalue);
    } else {
      return weakThresholdByPvalue(pvalue);
    }
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] weakThresholdsByPvalues(double[] pvalues) throws HashOverflowException {
    CanFindThreshold.ThresholdInfo[] result = new CanFindThreshold.ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = weakThresholdByPvalue(pvalues[i]);
    }
    return result;
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] strongThresholsdByPvalues(double[] pvalues) throws HashOverflowException {
    CanFindThreshold.ThresholdInfo[] result = new CanFindThreshold.ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = strongThresholdByPvalue(pvalues[i]);
    }
    return result;
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] thresholdsByPvalues(double[] pvalues, BoundaryType boundaryType) throws HashOverflowException {
    CanFindThreshold.ThresholdInfo[] result = new CanFindThreshold.ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = thresholdByPvalue(pvalues[i], boundaryType);
    }
    return result;
  }
}
