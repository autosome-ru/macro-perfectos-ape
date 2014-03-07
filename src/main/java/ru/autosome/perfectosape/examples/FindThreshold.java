package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindThreshold {
  static void print_result(CanFindThreshold.ThresholdInfo info, BackgroundModel background, int pwmLength) {
    System.out.println( "expected pvalue: " + info.expected_pvalue + "\n" +
                         "threshold: " + info.threshold + "\n" +
                         "actual pvalue: " + info.real_pvalue + "\n" +
                         "number of recognized words: " + info.numberOfRecognizedWords(background, pwmLength) + "\n------------\n");
  }

  public static void main(String[] args) {
    PWM pwm = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    BackgroundModel background = new WordwiseBackground();
    double discretization = 10000;
    BoundaryType pvalue_boundary = BoundaryType.LOWER;
    Integer max_hash_size = null;
    double pvalue = 0.0005;
    double[] pvalues = {0.0001, 0.0005, 0.001};

    CanFindThreshold calculator = new FindThresholdAPE<PWM, BackgroundModel>(pwm, background, discretization, max_hash_size);

    // Single threshold
    CanFindThreshold.ThresholdInfo info = null;
    try {
      info = calculator.thresholdByPvalue(pvalue, pvalue_boundary);
    } catch (HashOverflowException e) {
      e.printStackTrace();
    }
    print_result(info, background, pwm.length());

    // Multiple thresholds
    CanFindThreshold.ThresholdInfo[] infos = new CanFindThreshold.ThresholdInfo[0];
    try {
      infos = calculator.thresholdsByPvalues(pvalues, pvalue_boundary);
    } catch (HashOverflowException e) {
      e.printStackTrace();
    }
    for (int i = 0; i < infos.length; ++i) {
      print_result(infos[i], background, pwm.length());
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
      print_result(infosBiouml[i], background, pwm.length());
    }
  }
}
