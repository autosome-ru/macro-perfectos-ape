package ru.autosome.jMacroape;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PrecalculateThresholdList {
  double discretization;
  double[] background;
  String pvalue_boundary;
  int max_hash_size;
  boolean from_pcm;

  public HashMap<String, Object> parameters() {
    HashMap<String, Object> result = new HashMap<String,Object>();
    result.put("discretization", discretization);
    result.put("background", background);
    result.put("pvalue_boundary", pvalue_boundary);
    result.put("max_hash_size", max_hash_size);
    return result;
  }

  public void calculate_thresholds_for_collection(
          String collection_folder,
          String results_folder,
          double[] pvalues) {
    java.io.File dir = new File(collection_folder);
    java.io.File results_dir = new File(results_folder);
    if (!results_dir.exists()) {
      results_dir.mkdir();
    }
    for(File filename: dir.listFiles()) {
      System.err.println(filename);
      PWM pwm = PWM.new_from_file(filename.getPath(), background, from_pcm);
      FindThreshold calculation = new FindThreshold();
      calculation.set_parameters(parameters());
      ArrayList<ThresholdInfo> infos = calculation.find_thresholds_by_pvalues(pwm, pvalues);
      File result_filename = new File(results_dir + File.separator + "thresholds_" + filename.getName());
      FindPvalueBsearch.new_from_threshold_infos(infos).save_to_file(result_filename.getPath());
    }
  }

  public static void main(String[] args) {
    try {
      double discretization = 1000;
      double[] background = {1, 1, 1, 1};
      String pvalue_boundary = "lower";
      int max_hash_size = 10000000;
      String data_model = "pwm";

      PrecalculateThresholdList calculation = new PrecalculateThresholdList();
      calculation.discretization = discretization;
      calculation.background = background;
      calculation.pvalue_boundary = pvalue_boundary;
      calculation.max_hash_size = max_hash_size;
      calculation.from_pcm = data_model.equals("pcm");

      calculation.calculate_thresholds_for_collection(
              "d:/iogen/hocomoco/v9/hocomoco_ad_uniform",
              "d:/iogen/hocomoco/v9/hocomoco_ad_uniform_thresholds",
              Helper.values_in_range_mul(1E-6, 0.3, 1.1));
    } catch (Exception err) {
      System.err.println("Error:\n" + err);
      System.exit(1);
    }
  }
}
