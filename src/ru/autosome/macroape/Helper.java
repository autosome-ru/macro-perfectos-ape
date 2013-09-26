package ru.autosome.macroape;


import java.util.ArrayList;
import java.util.HashMap;

public class Helper {

    public static String threshold_infos_string(ArrayList<ThresholdInfo> data, HashMap<String, Object> parameters) {
        OutputInformation infos = new OutputInformation(data);

        infos.add_parameter("V", "discretization value", parameters.get("discretization"));
        infos.add_parameter("PB", "P-value boundary", parameters.get("pvalue_boundary"));
        BackgroundModel bckgr = (BackgroundModel) parameters.get("background");
        infos.background_parameter("B", "background", bckgr);

        infos.add_table_parameter("P", "requested P-value", "expected_pvalue");
        infos.add_table_parameter("AP", "actual P-value", "real_pvalue");

        if (bckgr.is_wordwise()) {
            infos.add_table_parameter("W", "number of recognized words", "recognized_words");
        }
        infos.add_table_parameter("T", "threshold", "threshold");

        return infos.result();
    }

    public static String find_pvalue_info_string(ArrayList<PvalueInfo> data, HashMap<String, Object> parameters) {
        OutputInformation infos = new OutputInformation(data);
        infos.add_parameter("V", "discretization value", parameters.get("discretization"));
        BackgroundModel bckgr = (BackgroundModel) parameters.get("background");
        infos.background_parameter("B", "background", bckgr);

        infos.add_table_parameter("T", "threshold", "threshold");
        if (bckgr.is_wordwise()) {
            infos.add_table_parameter("W", "number of recognized words", "number_of_recognized_words");
        }
        infos.add_table_parameter("P", "P-value", "pvalue");

        return infos.result();
    }

    public static double[] values_in_range_add(double from, double to, double step) {
        ArrayList<Double> results = new ArrayList<Double>();
        for (double x = from; x <= to; x += step) {
            results.add(x);
        }
        return ArrayExtensions.toPrimitiveArray(results);
    }

    public static double[] values_in_range_mul(double from, double to, double mul_step) {
        ArrayList<Double> results = new ArrayList<Double>();
        for (double x = from; x <= to; x *= mul_step) {
            results.add(x);
        }
        return ArrayExtensions.toPrimitiveArray(results);
    }
}
