package ru.autosome.macroape;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// Looks for rough pValue of motif under given threshold
// using a sorted list of predefined threshold-pvalues pairs
// by performing binary search
public class FindPvalueBsearch implements CanFindPvalue {
  ArrayList<ThresholdPvaluePair> list_of_pvalues_by_thresholds;
  final PWM pwm;
  Double discretization;
  double[] background;
  Integer max_hash_size;

  public FindPvalueBsearch(PWM pwm, ArrayList<ThresholdPvaluePair> infos) {
    this.pwm = pwm;
    Collections.sort(infos);
    list_of_pvalues_by_thresholds = new ArrayList<ThresholdPvaluePair>();
    list_of_pvalues_by_thresholds.add(infos.get(0));
    for (int i = 1; i < infos.size(); ++i) {
      if (! infos.get(i).equals( infos.get(i-1) )) {
        list_of_pvalues_by_thresholds.add( infos.get(i) );
      }
    }
  }

  public FindPvalueBsearch(PWM pwm) {
    this.pwm = pwm;
    this.discretization = 10000.0;
    this.background = Helper.wordwise_background();
    this.max_hash_size = 10000000;
  }
  public HashMap<String, Object> parameters() {
    HashMap<String, Object> parameters = new HashMap<String,Object>();
    parameters.put("discretization", discretization);
    parameters.put("background", background);
    parameters.put("max_hash_size", max_hash_size);
    return parameters;
  }
  public void set_parameters(HashMap<String, Object> parameters) {
    if(parameters.containsKey("discretization")) {
      discretization = (Double)parameters.get("discretization");
    }
    if(parameters.containsKey("background")) {
      background = (double[])parameters.get("background");
    }
    if(parameters.containsKey("max_hash_size")) {
      max_hash_size = (Integer)parameters.get("max_hash_size");
    }
  }

  public void set_discretization(Double discretization) { this.discretization = discretization; }
  public void set_background(double[] background) { this.background = background; }
  public void set_max_hash_size(Integer max_hash_size) {this.max_hash_size = max_hash_size; }

  public static FindPvalueBsearch new_from_threshold_infos(PWM pwm, ArrayList<ThresholdInfo> infos) {
    ArrayList<ThresholdPvaluePair> pvalue_by_threshold_list = new ArrayList<ThresholdPvaluePair>();
    for (ThresholdInfo info: infos) {
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
        pvalue = Math.sqrt(list_of_pvalues_by_thresholds.get(insertion_point).pvalue * list_of_pvalues_by_thresholds.get(insertion_point-1).pvalue);
      } else if (insertion_point == 0) {
        pvalue = list_of_pvalues_by_thresholds.get(0).pvalue;
      } else {
        pvalue = list_of_pvalues_by_thresholds.get(list_of_pvalues_by_thresholds.size() - 1).pvalue;
      }
    }
    return new PvalueInfo(threshold, pvalue, (int)(pwm.vocabularyVolume() * pvalue));
  }

  public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds) {
    ArrayList<PvalueInfo> results = new ArrayList<PvalueInfo>();
    for(double threshold: thresholds){
      results.add(pvalue_by_threshold(threshold));
    }
    return results;
  }

  public static ArrayList<ThresholdPvaluePair> load_thresholds_list(ArrayList<String> lines) {
    ArrayList<ThresholdPvaluePair> result = new ArrayList<ThresholdPvaluePair>();
    for(String s: lines) {
      String[] line_tokens = s.replaceAll("\\s+","\t").split("\t");
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
  public static void save_thresholds_list(ArrayList<ThresholdPvaluePair> infos, String filename) {
    try {
      FileWriter fw = new FileWriter(new java.io.File(filename));
      for(ThresholdPvaluePair info: infos){
        fw.write(info + "\n");
      }
      fw.close();
    } catch (IOException err) {
      System.err.println("Error:\n" + err);
    }
  }
  public void save_to_file(String filename) {
    save_thresholds_list(list_of_pvalues_by_thresholds, filename);
  }
  public static FindPvalueBsearch load_from_file(PWM pwm, String filename) {
    return new FindPvalueBsearch(pwm, load_thresholds_list(filename));
  }
}
