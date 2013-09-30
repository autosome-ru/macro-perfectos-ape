package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.HashMap;

public class FindPvalueAPE implements CanFindPvalue {
    private final PWM pwm;
    Double discretization;
    BackgroundModel background;
    Integer max_hash_size;

    public FindPvalueAPE(PWM pwm) {
        this.pwm = pwm;
        this.discretization = 10000.0;
        this.background = new WordwiseBackground();
        this.max_hash_size = 10000000;
    }

    // In some cases (when discretization is null) pwm can be altered by background and max_hash_size
    public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds) {
        PWM pwm = this.pwm;
        if (discretization != null) {
            pwm = pwm.discrete(discretization);
        }
        CountingPWM countingPWM = new CountingPWM(pwm, background);
        countingPWM.max_hash_size = max_hash_size;


        double[] thresholds_discreeted = new double[thresholds.length];
        for (int i = 0; i < thresholds.length; ++i) {
            if (discretization != null) {
                thresholds_discreeted[i] = thresholds[i] * discretization;
            } else {
                thresholds_discreeted[i] = thresholds[i];
            }
        }

        HashMap<Double, Double> counts = countingPWM.counts_by_thresholds(thresholds_discreeted);
        ArrayList<PvalueInfo> infos = new ArrayList<PvalueInfo>();
        for (double threshold : thresholds) {
            double count;
            if (discretization != null) {
                count = counts.get(threshold * discretization);
            } else {
                count = counts.get(threshold);
            }
            double pvalue = count / countingPWM.vocabularyVolume();

            infos.add(new PvalueInfo(threshold, pvalue, (int) count));
        }
        return infos;
    }

    public PvalueInfo pvalue_by_threshold(double threshold) {
        double[] thresholds = {threshold};
        return pvalues_by_thresholds(thresholds).get(0);
    }

    public OutputInformation report_table_layout() {
        OutputInformation infos = new OutputInformation();
        infos.add_parameter("V", "discretization value", discretization);
        infos.background_parameter("B", "background", background);

        infos.add_table_parameter("T", "threshold", "threshold");
        if (background.is_wordwise()) {
            infos.add_table_parameter("W", "number of recognized words", "number_of_recognized_words");
        }
        infos.add_table_parameter("P", "P-value", "pvalue");

        return infos;
    }
}
