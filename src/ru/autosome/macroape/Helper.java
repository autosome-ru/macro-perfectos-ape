package ru.autosome.macroape;


import java.util.ArrayList;

class Helper {

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

  static void print_help_if_requested(ArrayList<String> argv, String doc) {
    if (argv.isEmpty() || ArrayExtensions.contain(argv, "-h") || ArrayExtensions.contain(argv, "--h")
            || ArrayExtensions.contain(argv, "-help") || ArrayExtensions.contain(argv, "--help")) {
      System.err.println(doc);
      System.exit(1);
    }
  }
}
