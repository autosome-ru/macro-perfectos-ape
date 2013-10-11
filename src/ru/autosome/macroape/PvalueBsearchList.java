package ru.autosome.macroape;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class PvalueBsearchList {
  private ArrayList<ThresholdPvaluePair> list;
  PvalueBsearchList () { }
  public PvalueBsearchList(ArrayList<ThresholdPvaluePair> list) {
    this.list = sort_list(list);
  }

  ArrayList<ThresholdPvaluePair> without_consequent_duplicates(ArrayList<ThresholdPvaluePair> infos) {
    ArrayList<ThresholdPvaluePair> reduced_infos;
    reduced_infos = new ArrayList<ThresholdPvaluePair>();
    reduced_infos.add(infos.get(0));
    for (int i = 1; i < infos.size(); ++i) {
      if (!infos.get(i).equals(infos.get(i - 1))) {
        reduced_infos.add(infos.get(i));
      }
    }
    return reduced_infos;
  }

  ArrayList<ThresholdPvaluePair> sort_list(ArrayList<ThresholdPvaluePair> infos) {
    Collections.sort(infos);
    return without_consequent_duplicates(infos);
  }

  public double combine_pvalues(double pvalue_1, double pvalue_2) {
    return Math.sqrt(pvalue_1 * pvalue_2);
  }

  public double pvalue_by_threshold(double threshold) {
    int index = Collections.binarySearch(list, threshold, ThresholdPvaluePair.double_comparator());
    if (index >= 0) {
      return list.get(index).pvalue;
    }

    int insertion_point = -index - 1;
    if (insertion_point > 0 && insertion_point < list.size()) {
      return combine_pvalues(list.get(insertion_point).pvalue,
                             list.get(insertion_point - 1).pvalue);
    } else if (insertion_point == 0) {
      return list.get(0).pvalue;
    } else {
      return list.get(list.size() - 1).pvalue;
    }
  }

  public void save_to_file(String filename) {
    try {
      FileWriter fw = new FileWriter(new java.io.File(filename));
      for (ThresholdPvaluePair info : list) {
        fw.write(info + "\n");
      }
      fw.close();
    } catch (IOException err) {
      System.err.println("Error:\n" + err);
    }
  }

  private static ArrayList<ThresholdPvaluePair> load_thresholds_list(ArrayList<String> lines) {
    ArrayList<ThresholdPvaluePair> result = new ArrayList<ThresholdPvaluePair>();
    for (String s : lines) {
      String[] line_tokens = s.replaceAll("\\s+", "\t").split("\t");
      if (line_tokens.length < 2) continue;
      double threshold = Double.valueOf(line_tokens[0]);
      double pvalue = Double.valueOf(line_tokens[1]);
      result.add(new ThresholdPvaluePair(threshold, pvalue));
    }
    return result;
  }

  public static ArrayList<ThresholdPvaluePair> load_thresholds_list(String filename) {
    try {
      InputStream reader = new FileInputStream(filename);
      ArrayList<String> lines = InputExtensions.readLinesFromInputStream(reader);
      return load_thresholds_list(lines);
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public static PvalueBsearchList load_from_file(String filename) {
    return new PvalueBsearchList(load_thresholds_list(filename));
  }
}
