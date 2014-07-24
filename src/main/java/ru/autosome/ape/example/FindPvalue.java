package ru.autosome.ape.example;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;

public class FindPvalue {
  static void print_result(CanFindPvalue.PvalueInfo info, BackgroundModel background, int pwmLength) {
    System.out.println( "threshold: " + info.threshold + "\n" +
     "pvalue: " + info.pvalue + "\n" +
     "number of recognized words: " + info.numberOfRecognizedWords(background, pwmLength) + "\n------------\n");
  }

  public static void main(String[] args) {
    PWM pwm = new PWMImporter().loadMotif("test_data/pwm/KLF4_f2.pwm");
    BackgroundModel background = new WordwiseBackground();
    Discretizer discretizer = new Discretizer(10000.0);
    Integer max_hash_size = null;
    double threshold = 3;
    double[] thresholds = {3,5,7};

    FindPvalueAPE calculator = new FindPvalueAPE<PWM, BackgroundModel>(pwm, background, discretizer, max_hash_size);

      // Single threshold
    {
      CanFindPvalue.PvalueInfo info = null;
      try {
        info = calculator.pvalueByThreshold(threshold);
      } catch (HashOverflowException e) {
        e.printStackTrace();
      }
      print_result(info, background, pwm.length());
    }

      // Multiple thresholds
    {
      CanFindPvalue.PvalueInfo[] infos = new CanFindPvalue.PvalueInfo[0];
      try {
        infos = calculator.pvaluesByThresholds(thresholds);
      } catch (HashOverflowException e) {
        e.printStackTrace();
      }
      for (CanFindPvalue.PvalueInfo info : infos) {
        print_result(info, background, pwm.length());
      }
    }
  }
}
