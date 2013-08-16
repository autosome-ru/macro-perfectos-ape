package ru.autosome.jMacroape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

// Looks for rough pValue of motif under given threshold
// using a sorted list of predefined threshold-pvalues pairs
// by performing binary search
public class FindPvalueBsearch {
  double[][] pvalue_by_threshold;
  static Comparator cache_threshold_comparator;

  public static Comparator threshold_comparator() {
    if(cache_threshold_comparator == null){
      Comparator cmp = new Comparator() {
        public int compare(Object o1, Object o2) {
          double a = ((HashMap<String, Double>)o1).get("threshold");
          double b = ((HashMap<String, Double>)o2).get("threshold");
          if (a < b)
            return -1;
          else if (a > b)
            return 1;
          else return 0;
        }
      };
      cache_threshold_comparator = cmp;
    }
    return cache_threshold_comparator;
  }

  FindPvalueBsearch(ArrayList<HashMap<String, Double>> infos){
    Collections.sort(infos, threshold_comparator());
    pvalue_by_threshold = new double[infos.size()][];
    for (int i = 0; i < infos.size(); ++i) {
      pvalue_by_threshold[i] = new double[2];
      pvalue_by_threshold[i][0] = infos.get(i).get("threshold");
      pvalue_by_threshold[i][1] = infos.get(i).get("pvalue");
    }
  }
}
