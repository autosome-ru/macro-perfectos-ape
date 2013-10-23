package ru.autosome.macroape.Examples;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.Calculations.CountingPWM;
import ru.autosome.macroape.Calculations.FindThresholdAPE;
import ru.autosome.macroape.PMParser;
import ru.autosome.macroape.PWM;
import ru.autosome.macroape.WordwiseBackground;

public class FindThreshold {
  static void print_result(CountingPWM.ThresholdInfo info) {
    System.out.println( "expected pvalue: " + info.expected_pvalue + "\n" +
                         "threshold: " + info.threshold + "\n" +
                         "actual pvalue: " + info.real_pvalue + "\n" +
                         "number of recognized words: " + info.recognized_words + "\n------------\n");
  }

  public static void main(String[] args) {
    PWM pwm = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    BackgroundModel background = new WordwiseBackground();
    double discretization = 10000;
    String pvalue_boundary = "lower";
    Integer max_hash_size = null;
    double pvalue = 0.0005;
    double[] pvalues = {0.0001, 0.0005, 0.001};

    FindThresholdAPE calculator = new FindThresholdAPE(pwm,background,discretization,pvalue_boundary,max_hash_size);

    // Single threshold
    CountingPWM.ThresholdInfo info = calculator.find_threshold_by_pvalue(pvalue);
    print_result(info);

    // Multiple thresholds
    CountingPWM.ThresholdInfo[] infos = calculator.find_thresholds_by_pvalues(pvalues);
    for (int i = 0; i < infos.length; ++i) {
      print_result(infos[i]);
    }

    // BioUML integration
    double[] thresholds_2 = {15,16,17};
    ru.autosome.macroape.BioUML.FindThresholdAPE.Parameters parameters =
     new ru.autosome.macroape.BioUML.FindThresholdAPE.Parameters(pwm,
                                                              pvalues,
                                                              background,
                                                              discretization,pvalue_boundary, max_hash_size);
    ru.autosome.macroape.BioUML.FindThresholdAPE bioumlCalculator = new ru.autosome.macroape.BioUML.FindThresholdAPE(parameters);
    CountingPWM.ThresholdInfo[] infosBiouml = bioumlCalculator.launch();
    for (int i = 0; i < infosBiouml.length; ++i) {
      print_result(infosBiouml[i]);
    }
  }
}
