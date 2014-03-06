package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.io.File;
import java.io.FileNotFoundException;

public class DiPWMFindPvalueBsearch implements CanFindPvalue {
  public static class Builder extends FindPvalueBuilder<DiPWM> {
    File pathToThresholds;

    public Builder(File pathToThresholds) {
      this.pathToThresholds = pathToThresholds;
    }

    @Override
    public CanFindPvalue pvalueCalculator() {
      try {
        File thresholds_file = new File(pathToThresholds, motif.getName() + ".thr");
        PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
        return new DiPWMFindPvalueBsearch(pvalueBsearchList);
      } catch (FileNotFoundException e) {
        return null;
      }
    }
  }

  PvalueBsearchList bsearchList;

  public DiPWMFindPvalueBsearch(PvalueBsearchList bsearchList) {
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
