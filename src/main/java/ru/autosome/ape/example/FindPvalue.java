package ru.autosome.ape.example;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.motifModel.mono.PWM;

public class FindPvalue {
  static void print_result(CanFindPvalue.PvalueInfo info, BackgroundModel background, int pwmLength) {
    System.out.println( "threshold: " + info.threshold + "\n" +
     "pvalue: " + info.pvalue + "\n" +
     "number of recognized words: " + info.numberOfRecognizedWords(background, pwmLength) + "\n------------\n");
  }

  public static void main(String[] args) {
    PWM pwm = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    BackgroundModel background = new WordwiseBackground();
    double discretization = 10000;
    Integer max_hash_size = null;
    double threshold = 3;
    double[] thresholds = {3,5,7};

    FindPvalueAPE calculator = new FindPvalueAPE<PWM, BackgroundModel>(pwm, background, discretization, max_hash_size);

      // Single threshold
    CanFindPvalue.PvalueInfo info = null;
    try {
      info = calculator.pvalueByThreshold(threshold);
    } catch (HashOverflowException e) {
      e.printStackTrace();
    }
    print_result(info, background, pwm.length());

      // Multiple thresholds
    CanFindPvalue.PvalueInfo[] infos = new CanFindPvalue.PvalueInfo[0];
    try {
      infos = calculator.pvaluesByThresholds(thresholds);
    } catch (HashOverflowException e) {
      e.printStackTrace();
    }
    for (int i = 0; i < infos.length; ++i) {
      print_result(infos[i], background, pwm.length());
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
    ru.autosome.ape.api.FindPvalueAPE.Parameters parameters =
     new ru.autosome.ape.api.FindPvalueAPE.Parameters(pwm_manual_constructed,
                                                              thresholds_2,
                                                              discretization, background, max_hash_size);
    ru.autosome.ape.api.FindPvalueAPE bioumlCalculator = new ru.autosome.ape.api.FindPvalueAPE(parameters);
    CanFindPvalue.PvalueInfo[] infosBiouml = bioumlCalculator.call();
    for (int i = 0; i < infosBiouml.length; ++i) {
      print_result(infosBiouml[i], background, pwm_manual_constructed.length());
    }
  }
}