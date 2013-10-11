package ru.autosome.macroape.Calculations;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.OutputInformation;
import ru.autosome.macroape.PWM;
import ru.autosome.macroape.PvalueBsearchList;

import java.util.ArrayList;

// Looks for rough pValue of motif under given threshold
// using a sorted list of predefined threshold-pvalues pairs
// by performing binary search

public class FindPvalueBsearch implements CanFindPvalue {
  public static class Parameters {
    public PWM pwm;
    public BackgroundModel background;
    public PvalueBsearchList bsearchList;

    public Parameters() { }
    public Parameters(PWM pwm, BackgroundModel background, PvalueBsearchList bsearchList) {
      this.pwm = pwm;
      this.background = background;
      this.bsearchList = bsearchList;
    }
  }

  Parameters parameters;

  public FindPvalueBsearch(Parameters parameters) {
    this.parameters = parameters;
  }

  public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds) {
    ArrayList<PvalueInfo> results = new ArrayList<PvalueInfo>();
    for (double threshold : thresholds) {
      results.add(pvalue_by_threshold(threshold));
    }
    return results;
  }

  double vocabularyVolume() {
    return new CountingPWM(parameters.pwm, parameters.background).vocabularyVolume();
  }

  public PvalueInfo pvalue_by_threshold(double threshold) {
    double pvalue = parameters.bsearchList.pvalue_by_threshold(threshold);
    int count = (int) (pvalue * vocabularyVolume());
    return new PvalueInfo(threshold, pvalue, count);
  }

  // TODO: decide which parameters are relevant
  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
    infos.background_parameter("B", "background", parameters.background);

    infos.add_table_parameter("T", "threshold", "threshold");
    if (parameters.background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "number_of_recognized_words");
    }
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }
}

