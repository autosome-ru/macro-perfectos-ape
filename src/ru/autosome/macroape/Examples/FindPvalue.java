package ru.autosome.macroape.Examples;

import ru.autosome.macroape.*;
import ru.autosome.macroape.Calculations.CanFindPvalue;
import ru.autosome.macroape.Calculations.FindPvalueAPE;

import java.util.ArrayList;

public class FindPvalue {
  public static void main(String[] args) {
    PWM pwm = PWM.fromParser(PMParser.from_file_or_stdin("/home/ilya/iogen/hocomoco_ad_uniform/KLF4_f2.pwm"));
    BackgroundModel background = new WordwiseBackground();
    FindPvalueAPE calculator = new FindPvalueAPE(pwm, (double)10000, background, 10000000);

    CanFindPvalue.PvalueInfo info = calculator.pvalue_by_threshold(3);
    System.out.println( "threshold: " + info.threshold + "\n" +
                        "pvalue: " + info.pvalue + "\n" +
                        "number of recognized words: " + info.number_of_recognized_words + "\n------------\n");

    double[] thresholds = {3,5,7};
    CanFindPvalue.PvalueInfo[] infos = calculator.pvalues_by_thresholds(thresholds);
    for (int i = 0; i < thresholds.length; ++i) {
      CanFindPvalue.PvalueInfo single_info = infos[i];
      System.out.println( "threshold: " + single_info.threshold + "\n" +
                           "pvalue: " + single_info.pvalue + "\n" +
                           "number of recognized words: " + single_info.number_of_recognized_words + "\n------------\n");
    }
  }
}
