package ru.autosome.ape.example;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;

import java.util.ArrayList;
import java.util.List;

public class FindThreshold {
  static void print_result(CanFindThreshold.ThresholdInfo info, BackgroundModel background, int pwmLength) {
    System.out.println( "expected pvalue: " + info.expected_pvalue + "\n" +
                         "threshold: " + info.threshold + "\n" +
                         "actual pvalue: " + info.real_pvalue + "\n" +
                         "number of recognized words: " + info.numberOfRecognizedWords(background, pwmLength) + "\n------------\n");
  }

  public static void main(String[] args) {
    PWM pwm = new PWMImporter().loadMotif("test_data/pwm/KLF4_f2.pwm");
    BackgroundModel background = new WordwiseBackground();
    Discretizer discretizer = new Discretizer(10000.0);
    BoundaryType pvalue_boundary = BoundaryType.LOWER;
    double pvalue = 0.0005;
    List<Double> pvalues = new ArrayList<Double>();
    pvalues.add(0.0001);
    pvalues.add(0.0005);
    pvalues.add(0.001);

    CanFindThreshold calculator = new FindThresholdAPE<PWM, BackgroundModel>(pwm, background, discretizer);

    // Single threshold
    {
      CanFindThreshold.ThresholdInfo info = null;
      info = calculator.thresholdByPvalue(pvalue, pvalue_boundary);
      print_result(info, background, pwm.length());
    }
    // Multiple thresholds
    {
      for (CanFindThreshold.ThresholdInfo info : calculator.thresholdsByPvalues(pvalues, pvalue_boundary)) {
        print_result(info, background, pwm.length());
      }
    }
  }
}
