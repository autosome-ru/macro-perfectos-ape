package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.io.File;
import java.io.FileNotFoundException;

public class DiPWMFindThresholdBsearch implements CanFindThreshold {
  public static class Builder implements CanFindThreshold.Builder<DiPWM> {
    File pathToThresholds;
    DiPWM dipwm;

    public Builder(File pathToThresholds) {
      this.pathToThresholds = pathToThresholds;
    }

    @Override
    public Builder applyMotif(DiPWM dipwm) {
      this.dipwm = dipwm;
      return this;
    }

    @Override
    public CanFindThreshold build() {
      if (dipwm != null) {
        try {
          File thresholds_file = new File(pathToThresholds, dipwm.name + ".thr");
          PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
          return new DiPWMFindThresholdBsearch(dipwm, pvalueBsearchList);
        } catch (FileNotFoundException e) {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  DiPWM dipwm;
  PvalueBsearchList bsearchList;

  public DiPWMFindThresholdBsearch(DiPWM dipwm, PvalueBsearchList bsearchList) {
    this.dipwm = dipwm;
    this.bsearchList = bsearchList;
  }

  @Override
  public ThresholdInfo weakThresholdByPvalue(double pvalue) throws HashOverflowException {
    PvalueBsearchList.ThresholdPvaluePair info = bsearchList.weakThresholdByPvalue(pvalue);
    double threshold = info.threshold;
    double real_pvalue = info.pvalue;
    return new ThresholdInfo(threshold, real_pvalue, pvalue);
  }

  @Override
  public ThresholdInfo strongThresholdByPvalue(double pvalue) throws HashOverflowException {
    PvalueBsearchList.ThresholdPvaluePair info = bsearchList.strongThresholdInfoByPvalue(pvalue);
    double threshold = info.threshold;
    double real_pvalue = info.pvalue;
    return new ThresholdInfo(threshold, real_pvalue, pvalue);
  }

  @Override
  public ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) throws HashOverflowException {
    if (boundaryType == BoundaryType.LOWER) {
      return strongThresholdByPvalue(pvalue);
    } else {
      return weakThresholdByPvalue(pvalue);
    }
  }

  @Override
  public ThresholdInfo[] weakThresholdsByPvalues(double[] pvalues) throws HashOverflowException {
    ThresholdInfo[] result = new ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = weakThresholdByPvalue(pvalues[i]);
    }
    return result;
  }

  @Override
  public ThresholdInfo[] strongThresholsdByPvalues(double[] pvalues) throws HashOverflowException {
    ThresholdInfo[] result = new ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = strongThresholdByPvalue(pvalues[i]);
    }
    return result;
  }

  @Override
  public ThresholdInfo[] thresholdsByPvalues(double[] pvalues, BoundaryType boundaryType) throws HashOverflowException {
    ThresholdInfo[] result = new ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = thresholdByPvalue(pvalues[i], boundaryType);
    }
    return result;
  }
}
