package ru.autosome.macroape.Calculations;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.OutputInformation;
import ru.autosome.macroape.PWM;

import java.util.ArrayList;
import java.util.HashMap;

public class FindPvalueAPE implements CanFindPvalue {
  PWM pwm;
  Double discretization;
  BackgroundModel background;
  Integer max_hash_size;

  public FindPvalueAPE(PWM pwm, Double discretization, BackgroundModel background, Integer max_hash_size) {
    this.pwm = pwm;
    this.discretization = discretization;
    this.background = background;
    this.max_hash_size = max_hash_size;
  }

  PWM upscaled_pwm() {
    return pwm.discrete(discretization);
  }

  CountingPWM countingPWM(PWM pwm) {
    return new CountingPWM(pwm, background, max_hash_size);
  }

  double upscale_threshold(double threshold) {
    if (discretization == null) {
      return threshold;
    } else {
      return threshold * discretization;
    }
  }
  double[] upscaled_thresholds(double[] thresholds) {
    double[] result = new double[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      result[i] = upscale_threshold(thresholds[i]);
    }
    return result;
  }

  PvalueInfo infos_by_count(HashMap<Double, Double> counts, double non_upscaled_threshold, CountingPWM countingPWM) {
    double count = counts.get(upscale_threshold(non_upscaled_threshold));
    double pvalue = count / countingPWM.vocabularyVolume();
    return new PvalueInfo(non_upscaled_threshold, pvalue, (int) count);
  }
      /*
  public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds) {
    CountingPWM countingPWM = countingPWM(upscaled_pwm());
    HashMap<Double, Double> counts = countingPWM.counts_by_thresholds(upscaled_thresholds(thresholds));

    ArrayList<PvalueInfo> infos = new ArrayList<PvalueInfo>();
    for (double threshold : thresholds) {
      infos.add( infos_by_count(counts, threshold, countingPWM) );
    }
    return infos;
  }
    */
  public PvalueInfo[] pvalues_by_thresholds(double[] thresholds) {
    CountingPWM countingPWM = countingPWM(upscaled_pwm());
    HashMap<Double, Double> counts = countingPWM.counts_by_thresholds(upscaled_thresholds(thresholds));

    PvalueInfo[] infos = new PvalueInfo[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      infos[i] = infos_by_count(counts, thresholds[i], countingPWM);
    }
    return infos;
  }

  public PvalueInfo pvalue_by_threshold(double threshold) {
    double[] thresholds = {threshold};
    return pvalues_by_thresholds(thresholds)[0];
  }

  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
    infos.add_parameter("V", "discretization value", discretization);
    infos.background_parameter("B", "background", background);

    infos.add_table_parameter("T", "threshold", "threshold");
    if (background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "number_of_recognized_words");
    }
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }
}
