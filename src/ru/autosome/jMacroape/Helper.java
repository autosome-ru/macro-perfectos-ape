package ru.autosome.jMacroape;


import java.util.ArrayList;
import java.util.HashMap;

public class Helper {
  public static String threshold_infos_string(ArrayList<HashMap<String, Double>> data, HashMap<String, Object> parameters) {
    OutputInformation infos = new OutputInformation(data);

    infos.add_parameter("V", "discretization value", parameters.get("discretization"));
    infos.add_parameter("PB", "P-value boundary", parameters.get("pvalue_boundary"));
    double[] bckgr = (double[])parameters.get("background");
    infos.background_parameter("B", "background", bckgr);

    infos.add_table_parameter("P", "requested P-value", "expected_pvalue");
    infos.add_table_parameter("AP", "actual P-value", "real_pvalue");

    if (OutputInformation.is_background_wordwise(bckgr)) {
      infos.add_table_parameter("W", "number of recognized words", "recognized_words");
    }
    infos.add_table_parameter("T", "threshold", "threshold");

    return infos.result();
  }

  public static String find_pvalue_info_string(ArrayList<HashMap<String,Double>> data, HashMap<String,Object> parameters) {
    OutputInformation infos = new OutputInformation(data);
    infos.add_parameter("V", "discretization value", parameters.get("discretization"));
    double[] bckgr = (double[])parameters.get("background");
    infos.background_parameter("B", "background", bckgr);

    infos.add_table_parameter("T", "threshold", "threshold");
    if (OutputInformation.is_background_wordwise(bckgr)) {
      infos.add_table_parameter("W", "number of recognized words", "number_of_recognized_words");
    }
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos.result();
  }

}
