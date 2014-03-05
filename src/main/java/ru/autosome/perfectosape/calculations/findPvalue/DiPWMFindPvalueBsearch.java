package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.io.File;
import java.io.FileNotFoundException;

public class DiPWMFindPvalueBsearch implements CanFindPvalue {
  public static class Builder implements CanFindPvalue.DiPWMBuilder {
    File pathToThresholds;
    DiPWM dipwm;

    public Builder(File pathToThresholds) {
      this.pathToThresholds = pathToThresholds;
    }

    @Override
    public CanFindPvalue.Builder applyMotif(DiPWM dipwm) {
      this.dipwm = dipwm;
      return this;
    }

    @Override
    public CanFindPvalue build() {
      if (dipwm != null) {
        try {
          File thresholds_file = new File(pathToThresholds, dipwm.name + ".thr");
          PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
          return new DiPWMFindPvalueBsearch(dipwm, pvalueBsearchList);
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

  public DiPWMFindPvalueBsearch(DiPWM dipwm, PvalueBsearchList bsearchList) {
    this.dipwm = dipwm;
    this.bsearchList = bsearchList;
  }

  @Override
  public CanFindPvalue.PvalueInfo[] pvaluesByThresholds(double[] thresholds) {
    CanFindPvalue.PvalueInfo[] results = new CanFindPvalue.PvalueInfo[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      results[i] = pvalueByThreshold(thresholds[i]);
    }
    return results;
  }

  @Override
  public CanFindPvalue.PvalueInfo pvalueByThreshold(double threshold) {
    double pvalue = bsearchList.pvalue_by_threshold(threshold);
    return new CanFindPvalue.PvalueInfo(threshold, pvalue);
  }

  // TODO: decide which parameters are relevant
  @Override
  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();

    infos.add_table_parameter("T", "threshold", "threshold");
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }
}
