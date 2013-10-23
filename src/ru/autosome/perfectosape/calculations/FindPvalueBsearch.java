package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.OutputInformation;
import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.PvalueBsearchList;

// Looks for rough pValue of motif under given threshold
// using a sorted list of predefined threshold-pvalues pairs
// by performing binary search

public class FindPvalueBsearch implements CanFindPvalue {
  PWM pwm;
  BackgroundModel background;
  PvalueBsearchList bsearchList;

  public FindPvalueBsearch(PWM pwm, BackgroundModel background, PvalueBsearchList bsearchList) {
    this.pwm = pwm;
    this.background = background;
    this.bsearchList = bsearchList;
  }

  public PvalueInfo[] pvalues_by_thresholds(double[] thresholds) {
    PvalueInfo[] results = new PvalueInfo[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      results[i] = pvalue_by_threshold(thresholds[i]);
    }
    return results;
  }

  double vocabularyVolume() {
    return new CountingPWM(pwm, background, null).vocabularyVolume();
  }

  public PvalueInfo pvalue_by_threshold(double threshold) {
    double pvalue = bsearchList.pvalue_by_threshold(threshold);
    int count = (int) (pvalue * vocabularyVolume());
    return new PvalueInfo(threshold, pvalue, count);
  }

  // TODO: decide which parameters are relevant
  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
    infos.background_parameter("B", "background", background);

    infos.add_table_parameter("T", "threshold", "threshold");
    if (background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "number_of_recognized_words");
    }
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }
}

