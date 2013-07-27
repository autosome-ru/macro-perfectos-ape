package ru.autosome.jMacroape;


import java.util.ArrayList;
import java.util.HashMap;

public class Helper {
  public static String threshold_infos_string(ArrayList<HashMap<String, Double>> data, HashMap<String, Object> parameters) {
    OutputInformation infos = new OutputInformation(data);

    infos.add_parameter("V", "discretization value", parameters.get("discretization"));
    infos.add_parameter("PB", "P-value boundary", parameters.get("pvalue_boundary"));
    Double bckgr[] = new Double[4];
    bckgr = (Double[])parameters.get("background");
    infos.background_parameter("B", "background", bckgr);

    infos.add_table_parameter("P", "requested P-value", "expected_pvalue");
    infos.add_table_parameter("AP", "actual P-value", "real_pvalue");

    if (bckgr[0] == 1.0 && bckgr[1] == 1.0 && bckgr[2] == 1.0 && bckgr[3] == 1.0) {
      infos.add_table_parameter("W", "number of recognized words", "recognized_words");
    }
    infos.add_table_parameter("T", "threshold", "threshold");

    return infos.result();
  }
}
