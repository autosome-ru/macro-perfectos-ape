package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.calculations.CanFindThreshold;
import ru.autosome.perfectosape.calculations.FindThresholdAPE;

public class FindThreshold {
  static void print_result(CanFindThreshold.ThresholdInfo info) {
    System.out.println( "expected pvalue: " + info.expected_pvalue + "\n" +
                         "threshold: " + info.threshold + "\n" +
                         "actual pvalue: " + info.real_pvalue + "\n" +
                         "number of recognized words: " + info.numberOfRecognizedWords + "\n------------\n");
  }

  public static void main(String[] args) {
    PWM pwm = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    BackgroundModel background = new WordwiseBackground();
    double discretization = 10000;
    BoundaryType pvalue_boundary = BoundaryType.LOWER;
    Integer max_hash_size = null;
    double pvalue = 0.0005;
    double[] pvalues = {0.0001, 0.0005, 0.001};

    CanFindThreshold calculator = new FindThresholdAPE(pwm,background,discretization,pvalue_boundary,max_hash_size);

    // Single threshold
    CanFindThreshold.ThresholdInfo info = calculator.find_threshold_by_pvalue(pvalue);
    print_result(info);

    // Multiple thresholds
    CanFindThreshold.ThresholdInfo[] infos = calculator.find_thresholds_by_pvalues(pvalues);
    for (int i = 0; i < infos.length; ++i) {
      print_result(infos[i]);
    }

    // api integration
    ru.autosome.perfectosape.api.FindThresholdAPE.Parameters parameters =
     new ru.autosome.perfectosape.api.FindThresholdAPE.Parameters(pwm,
                                                              pvalues,
                                                              background,
                                                              discretization,pvalue_boundary, max_hash_size);
    ru.autosome.perfectosape.api.FindThresholdAPE bioumlCalculator = new ru.autosome.perfectosape.api.FindThresholdAPE(parameters);
    CanFindThreshold.ThresholdInfo[] infosBiouml = bioumlCalculator.call();
    for (int i = 0; i < infosBiouml.length; ++i) {
      print_result(infosBiouml[i]);
    }
  }
}
