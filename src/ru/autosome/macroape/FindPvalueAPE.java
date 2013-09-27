package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.HashMap;

public class FindPvalueAPE implements CanFindPvalue {
    final PWM pwm;
    Double discretization;
    BackgroundModel background;
    Integer max_hash_size;

    public FindPvalueAPE(PWM pwm) {
        this.pwm = pwm;
        this.discretization = 10000.0;
        this.background = new WordwiseBackground();
        this.max_hash_size = 10000000;
    }

    public HashMap<String, Object> parameters() {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("discretization", discretization);
        parameters.put("background", background);
        parameters.put("max_hash_size", max_hash_size);
        return parameters;
    }

    public void set_parameters(HashMap<String, Object> parameters) {
        if (parameters.containsKey("discretization")) {
            discretization = (Double) parameters.get("discretization");
        }
        if (parameters.containsKey("background")) {
            background = (BackgroundModel) parameters.get("background");
        }
        if (parameters.containsKey("max_hash_size")) {
            max_hash_size = (Integer) parameters.get("max_hash_size");
        }
    }

    public void set_discretization(Double discretization) {
        this.discretization = discretization;
    }

    public void set_background(BackgroundModel background) {
        this.background = background;
    }

    public void set_max_hash_size(Integer max_hash_size) {
        this.max_hash_size = max_hash_size;
    }

    // In some cases (when discretization is null) pwm can be altered by background and max_hash_size
    public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds) {
        PWM pwm = this.pwm;
        if (discretization != null) {
            pwm = pwm.discrete(discretization);
        }
        CountingPWM countingPWM = new CountingPWM(pwm, background);
        countingPWM.max_hash_size = max_hash_size; // not int because it can be null


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
}
