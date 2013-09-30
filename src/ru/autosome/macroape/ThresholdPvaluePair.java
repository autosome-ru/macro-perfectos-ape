package ru.autosome.macroape;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

class ThresholdPvaluePair implements Comparable {
  private final double threshold;
  public final double pvalue;

  private ThresholdPvaluePair(double threshold, double pvalue) {
    this.threshold = threshold;
    this.pvalue = pvalue;
  }

  ThresholdPvaluePair(ThresholdInfo info) {
    this.threshold = info.threshold;
    this.pvalue = info.real_pvalue;
  }

  public static Comparator double_comparator() {
    return new Comparator<Object>() {
      Double val(Object obj) {
        double value;
        if (obj instanceof ThresholdPvaluePair) {
          value = ((ThresholdPvaluePair) obj).threshold;
        } else if (obj instanceof Double) {
          value = (Double) obj;
        } else {
          throw new ClassCastException("Incorrect type for comparison");
        }
        return value;
      }

      @Override
      public int compare(Object o1, Object o2) {
        if (val(o1) < val(o2)) {
          return -1;
        } else if (val(o1) > val(o2)) {
          return 1;
        } else return 0;
      }
    };

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

  public static void save_thresholds_list(ArrayList<ThresholdPvaluePair> infos, String filename) {
    try {
      FileWriter fw = new FileWriter(new java.io.File(filename));
      for (ThresholdPvaluePair info : infos) {
        fw.write(info + "\n");
      }
      fw.close();
    } catch (IOException err) {
      System.err.println("Error:\n" + err);
    }
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ThresholdPvaluePair)) {
      return false;
    }
    return threshold == ((ThresholdPvaluePair) other).threshold && pvalue == ((ThresholdPvaluePair) other).pvalue;
  }

  public int compareTo(Object other) {
    double other_value;
    if (other instanceof ThresholdPvaluePair) {
      other_value = ((ThresholdPvaluePair) other).threshold;
    } else if (other instanceof Double) {
      other_value = (Double) other;
    } else {
      throw new ClassCastException("Incorrect type for comparison");
    }

    if (threshold > other_value) {
      return 1;
    } else if (threshold < other_value) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public String toString() {
    return threshold + "\t" + pvalue;
  }
}
