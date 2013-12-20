package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.calculations.CanFindPvalue;

public class FindPvalueDinucleotide {
  static void print_result(CanFindPvalue.PvalueInfo info) {
    System.out.println( "threshold: " + info.threshold + "\n" +
                         "pvalue: " + info.pvalue + "\n" +
                         "number of recognized words: " + info.number_of_recognized_words + "\n------------\n");
  }

  public static void main(String[] args) {
    PWM pwm = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    BackgroundModel background = new WordwiseBackground();
    double discretization = 10000;
    Integer max_hash_size = null;
    double threshold = 3;
    double[] thresholds = {3,5,7};

    Sequence word = new Sequence("ACAGTGACAA");
    DiPWM dipwm = DiPWM.fromPWM(pwm);
    System.out.println(pwm.score(word));
    System.out.println(dipwm.score(word));
/*
    FindPvalueAPE calculator = new FindPvalueAPE(pwm, discretization, background, max_hash_size);

    // Single threshold
    CanFindPvalue.PvalueInfo info = calculator.pvalue_by_threshold(threshold);
    print_result(info);

    // Multiple thresholds
    CanFindPvalue.PvalueInfo[] infos = calculator.pvalues_by_thresholds(thresholds);
    for (int i = 0; i < infos.length; ++i) {
      print_result(infos[i]);
    }

    // api integration
    double[][] matrix_cAVNCT = { {1.0, 2.0, 1.0, 1.0},
                                {10.5, -3.0, 0.0, 0.0},
                                {5.0, 5.0, 5.0, -10.0},
                                {0.0, 0.0, 0.0, 0.0},
                                {-1.0, 10.5, -1.0, 0.0},
                                {0.0, 0.0, 0.0, 2.0}};
    PWM pwm_manual_constructed = new PWM(matrix_cAVNCT, "PWM for cAVNCt consensus sequence");
    double[] thresholds_2 = {15,16,17};
    ru.autosome.perfectosape.api.FindPvalueAPE.Parameters parameters =
     new ru.autosome.perfectosape.api.FindPvalueAPE.Parameters(pwm_manual_constructed,
                                                               thresholds_2,
                                                               discretization, background, max_hash_size);
    ru.autosome.perfectosape.api.FindPvalueAPE bioumlCalculator = new ru.autosome.perfectosape.api.FindPvalueAPE(parameters);
    CanFindPvalue.PvalueInfo[] infosBiouml = bioumlCalculator.call();
    for (int i = 0; i < infosBiouml.length; ++i) {
      print_result(infosBiouml[i]);
    }
    */
  }
}
