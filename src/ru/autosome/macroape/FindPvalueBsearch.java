package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// Looks for rough pValue of motif under given threshold
// using a sorted list of predefined threshold-pvalues pairs
// by performing binary search
public class FindPvalueBsearch implements CanFindPvalue {
    ArrayList<ThresholdPvaluePair> list_of_pvalues_by_thresholds;
    final PWM pwm;
    BackgroundModel background;

    public FindPvalueBsearch(PWM pwm, ArrayList<ThresholdPvaluePair> infos) {
        this.pwm = pwm;
        Collections.sort(infos);
        list_of_pvalues_by_thresholds = new ArrayList<ThresholdPvaluePair>();
        list_of_pvalues_by_thresholds.add(infos.get(0));
        for (int i = 1; i < infos.size(); ++i) {
            if (!infos.get(i).equals(infos.get(i - 1))) {
                list_of_pvalues_by_thresholds.add(infos.get(i));
            }
        }
    }

    public FindPvalueBsearch(PWM pwm) {
        this.pwm = pwm;
        this.background = new WordwiseBackground();
    }

    public HashMap<String, Object> parameters() {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("background", background);
        return parameters;
    }

    public static FindPvalueBsearch new_from_threshold_infos(PWM pwm, ArrayList<ThresholdInfo> infos) {
        ArrayList<ThresholdPvaluePair> pvalue_by_threshold_list = new ArrayList<ThresholdPvaluePair>();
        for (ThresholdInfo info : infos) {
            pvalue_by_threshold_list.add(new ThresholdPvaluePair(info));
        }
        return new FindPvalueBsearch(pwm, pvalue_by_threshold_list);
    }

    public PvalueInfo pvalue_by_threshold(double threshold) {
        int index = Collections.binarySearch(list_of_pvalues_by_thresholds, threshold, ThresholdPvaluePair.double_comparator());
        double pvalue;
        if (index >= 0) {
            pvalue = list_of_pvalues_by_thresholds.get(index).pvalue;
        } else {
            int insertion_point = -index - 1;
            if (insertion_point > 0 && insertion_point < list_of_pvalues_by_thresholds.size()) {
                pvalue = Math.sqrt(list_of_pvalues_by_thresholds.get(insertion_point).pvalue * list_of_pvalues_by_thresholds.get(insertion_point - 1).pvalue);
            } else if (insertion_point == 0) {
                pvalue = list_of_pvalues_by_thresholds.get(0).pvalue;
            } else {
                pvalue = list_of_pvalues_by_thresholds.get(list_of_pvalues_by_thresholds.size() - 1).pvalue;
            }
        }
        return new PvalueInfo(threshold, pvalue, (int) (pvalue * new CountingPWM(pwm, background).vocabularyVolume()));
    }

    public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds) {
        ArrayList<PvalueInfo> results = new ArrayList<PvalueInfo>();
        for (double threshold : thresholds) {
            results.add(pvalue_by_threshold(threshold));
        }
        return results;
    }

    public void save_to_file(String filename) {
        ThresholdPvaluePair.save_thresholds_list(list_of_pvalues_by_thresholds, filename);
    }

    public static FindPvalueBsearch load_from_file(PWM pwm, String filename) {
        return new FindPvalueBsearch(pwm, ThresholdPvaluePair.load_thresholds_list(filename));
    }
}
