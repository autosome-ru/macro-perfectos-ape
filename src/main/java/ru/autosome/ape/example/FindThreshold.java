package ru.autosome.ape.example;

import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.motifModel.mono.PWM;

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
    {
      CanFindThreshold.ThresholdInfo info = null;
      try {
        info = calculator.thresholdByPvalue(pvalue, pvalue_boundary);
      } catch (HashOverflowException e) {
        e.printStackTrace();
      }
      print_result(info, background, pwm.length());
    }
    // Multiple thresholds
    {
      CanFindThreshold.ThresholdInfo[] infos = new CanFindThreshold.ThresholdInfo[0];
      try {
        infos = calculator.thresholdsByPvalues(pvalues, pvalue_boundary);
      } catch (HashOverflowException e) {
        e.printStackTrace();
      }
      for (CanFindThreshold.ThresholdInfo info : infos) {
        print_result(info, background, pwm.length());
      }
    }
    // api integration
    ru.autosome.ape.api.FindThresholdAPE.Parameters parameters =
     new ru.autosome.ape.api.FindThresholdAPE.Parameters(pwm,
                                                              pvalues,
                                                              background,
                                                              discretization,pvalue_boundary, max_hash_size);
    ru.autosome.ape.api.FindThresholdAPE bioumlCalculator = new ru.autosome.ape.api.FindThresholdAPE(parameters);
    CanFindThreshold.ThresholdInfo[] infosBiouml = bioumlCalculator.call();
    for (CanFindThreshold.ThresholdInfo bioumlInfo : infosBiouml) {
      print_result(bioumlInfo, background, pwm.length());
    }
  }
}
