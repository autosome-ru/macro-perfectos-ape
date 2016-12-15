package ru.autosome.ape.example;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;

import java.util.ArrayList;
import java.util.List;

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
    double threshold = 3;
    List<Double> thresholds_list = new ArrayList<Double>();
    thresholds_list.add(3.0);
    thresholds_list.add(5.0);
    thresholds_list.add(6.0);


    FindPvalueAPE<PWM, BackgroundModel> calculator = new FindPvalueAPE<PWM, BackgroundModel>(pwm, background, discretizer);

      // Single threshold
    {
      CanFindPvalue.PvalueInfo info = null;
      info = calculator.pvalueByThreshold(threshold);
      print_result(info, background, pwm.length());
    }

      // Multiple thresholds
    {
      for (CanFindPvalue.PvalueInfo info : calculator.pvaluesByThresholds(thresholds_list)) {
        print_result(info, background, pwm.length());
      }
    }
  }
}
