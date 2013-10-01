package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.HashMap;

public class FindPvalueAPE implements CanFindPvalue {
  FindPvalueAPEParameters parameters;

  public FindPvalueAPE(FindPvalueAPEParameters parameters) {
    this.parameters = parameters;
    /*this.pwm = pwm;
    this.discretization = 10000.0;
    this.background = new WordwiseBackground();
    this.max_hash_size = 10000000;*/
  }


  PWM upscaled_pwm() {
    return parameters.pwm.discrete(parameters.discretization);
  }

  CountingPWM countingPWM(PWM pwm) {
    CountingPWM countingPWM = new CountingPWM(pwm, parameters.background);
    countingPWM.max_hash_size = parameters.max_hash_size;
    return countingPWM;
  }

  double upscale_threshold(double threshold) {
    if (parameters.discretization == null) {
      return threshold;
    } else {
      return threshold * parameters.discretization;
    }
  }
  double[] upscaled_thresholds() {
    double[] result = new double[parameters.thresholds.length];
    for (int i = 0; i < parameters.thresholds.length; ++i) {
        result[i] = upscale_threshold(parameters.thresholds[i]);
    }
    return result;
  }

  PvalueInfo infos_by_count(HashMap<Double, Double> counts, double non_upscaled_threshold, CountingPWM countingPWM) {
    double count = counts.get(upscale_threshold(non_upscaled_threshold));
    double pvalue = count / countingPWM.vocabularyVolume();
    return new PvalueInfo(non_upscaled_threshold, pvalue, (int) count);
  }

  public ArrayList<PvalueInfo> pvalues_by_thresholds() {
    CountingPWM countingPWM = countingPWM(upscaled_pwm());
    HashMap<Double, Double> counts = countingPWM.counts_by_thresholds(upscaled_thresholds());

    ArrayList<PvalueInfo> infos = new ArrayList<PvalueInfo>();
    for (double threshold : parameters.thresholds) {
      infos.add( infos_by_count(counts, threshold, countingPWM) );
    }
    return infos;
  }

  /*public PvalueInfo pvalue_by_threshold(double threshold) {
    double[] thresholds = {threshold};
    return pvalues_by_thresholds(thresholds).get(0);
  } */

  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
    infos.add_parameter("V", "discretization value", parameters.discretization);
    infos.background_parameter("B", "background", parameters.background);

    infos.add_table_parameter("T", "threshold", "threshold");
    if (parameters.background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "number_of_recognized_words");
    }
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }
}
