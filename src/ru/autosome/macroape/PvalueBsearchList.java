package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.Collections;

public class PvalueBsearchList {
  private ArrayList<ThresholdPvaluePair> list;
  PvalueBsearchList () { }
  PvalueBsearchList (ArrayList<ThresholdPvaluePair> list) {
    this.list = sort_list(list);
  }

  ArrayList<ThresholdPvaluePair> sort_list(ArrayList<ThresholdPvaluePair> infos) {
    Collections.sort(infos);
    ArrayList<ThresholdPvaluePair> sorted_infos;
    sorted_infos = new ArrayList<ThresholdPvaluePair>();
    sorted_infos.add(infos.get(0));
    for (int i = 1; i < infos.size(); ++i) {
      if (!infos.get(i).equals(infos.get(i - 1))) {
        sorted_infos.add(infos.get(i));
      }
    }
    return sorted_infos;
  }

  public double pvalue_by_threshold(double threshold) {
    int index = Collections.binarySearch(list, threshold, ThresholdPvaluePair.double_comparator());
    if (index >= 0) {
      return list.get(index).pvalue;
    }

    int insertion_point = -index - 1;
    if (insertion_point > 0 && insertion_point < list.size()) {
      return Math.sqrt( list.get(insertion_point).pvalue * list.get(insertion_point - 1).pvalue );
    } else if (insertion_point == 0) {
      return list.get(0).pvalue;
    } else {
      return list.get(list.size() - 1).pvalue;
    }
  }



  public void save_to_file(String filename) {
    ThresholdPvaluePair.save_thresholds_list(list, filename);
  }

  public static PvalueBsearchList load_from_file(String filename) {
    return new PvalueBsearchList(ThresholdPvaluePair.load_thresholds_list(filename));
  }
}
