package ru.autosome.perfectosape;

import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.importers.InputExtensions;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// List of pvalue-threshold pairs sorted by threshold ascending
public class PvalueBsearchList {
  public static class ThresholdPvaluePair {
    public final double threshold;
    public final double pvalue;

    ThresholdPvaluePair(double threshold, double pvalue) {
      this.threshold = threshold;
      this.pvalue = pvalue;
    }

    public ThresholdPvaluePair(CanFindThreshold.ThresholdInfo info) {
      this.threshold = info.threshold;
      this.pvalue = info.real_pvalue;
    }

    public static final Comparator thresholdComparator =
     new Comparator<Object>() {
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

    // reversed comparison (thresholds are sorted ascending, so pvalues descending)
    public static final Comparator pvalueComparator =
     new Comparator<Object>() {
       Double val(Object obj) {
         double value;
         if (obj instanceof ThresholdPvaluePair) {
           value = ((ThresholdPvaluePair) obj).pvalue;
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
           return 1;
         } else if (val(o1) > val(o2)) {
           return -1;
         } else return 0;
       }
     };

    @Override
    public boolean equals(Object other) {
      return (other instanceof ThresholdPvaluePair) &&
              threshold == ((ThresholdPvaluePair) other).threshold &&
              pvalue == ((ThresholdPvaluePair) other).pvalue;
    }

    @Override
    public int hashCode() {
      int hash = 1;
      hash  = hash * 17 + ((Double)threshold).hashCode();
      hash  = hash * 31 + ((Double)pvalue).hashCode();
      return hash;
    }

    @Override
    public String toString() {
      return threshold + "\t" + pvalue;
    }
  }


  private final List<ThresholdPvaluePair> list;
  public PvalueBsearchList() {
    this.list = new ArrayList<ThresholdPvaluePair>();
  }
  public PvalueBsearchList(List<ThresholdPvaluePair> infos) {
    Collections.sort(infos, ThresholdPvaluePair.thresholdComparator);
    this.list = without_consequent_duplicates(without_zero_pvalue(infos));
  }

  private List<ThresholdPvaluePair> without_consequent_duplicates(List<ThresholdPvaluePair> infos) {
    List<ThresholdPvaluePair> reduced_infos;
    reduced_infos = new ArrayList<ThresholdPvaluePair>();
    if (infos.isEmpty()) {
      return reduced_infos;
    }
    reduced_infos.add(infos.get(0));
    for (int i = 1; i < infos.size(); ++i) {
      if (!infos.get(i).equals(infos.get(i - 1))) {
        reduced_infos.add(infos.get(i));
      }
    }
    return reduced_infos;
  }

  private List<ThresholdPvaluePair> without_zero_pvalue(List<ThresholdPvaluePair> infos) {
    List<ThresholdPvaluePair> reduced_infos;
    reduced_infos = new ArrayList<ThresholdPvaluePair>();
    for (ThresholdPvaluePair info: infos) {
      if (info.pvalue != 0) {
        reduced_infos.add(info);
      }
    }
    return reduced_infos;
  }

  public double combine_pvalues(double pvalue_1, double pvalue_2) {
    return Math.sqrt(pvalue_1 * pvalue_2);
  }

  public double pvalue_by_threshold(double threshold) {
    int index = Collections.binarySearch(list, threshold, ThresholdPvaluePair.thresholdComparator);
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

  public ThresholdPvaluePair strongThresholdInfoByPvalue(double pvalue) {
    int index = Collections.binarySearch(list, pvalue, ThresholdPvaluePair.pvalueComparator);
    if (index >= 0) {
      return list.get(index);
    }

    int insertion_point = -index - 1;
    if (insertion_point > 0 && insertion_point < list.size()) {
      return list.get(insertion_point);
    } else if (insertion_point == 0) {
      return list.get(0);
    } else {
      return list.get(list.size() - 1);
    }
  }

  public ThresholdPvaluePair weakThresholdByPvalue(double pvalue) {
    int index = Collections.binarySearch(list, pvalue, ThresholdPvaluePair.pvalueComparator);
    if (index >= 0) {
      return list.get(index);
    }

    int insertion_point = -index - 1;
    if (insertion_point > 0 && insertion_point < list.size()) {
      return list.get(insertion_point - 1);
    } else if (insertion_point == 0) {
      return list.get(0);
    } else {
      return list.get(list.size() - 1);
    }
  }


  public void save_to_file(File file) throws IOException {
    FileWriter fw = new FileWriter(file);
    for (ThresholdPvaluePair info : list) {
      fw.write(info + "\n");
    }
    fw.close();
  }

  private static List<ThresholdPvaluePair> load_thresholds_list(List<String> lines) {
    List<ThresholdPvaluePair> result = new ArrayList<ThresholdPvaluePair>();
    for (String s : lines) {
      String[] line_tokens = s.replaceAll("\\s+", "\t").split("\t");
      if (line_tokens.length < 2) continue;
      double threshold = Double.valueOf(line_tokens[0]);
      double pvalue = Double.valueOf(line_tokens[1]);
      result.add(new ThresholdPvaluePair(threshold, pvalue));
    }
    return result;
  }

  public static List<ThresholdPvaluePair> load_thresholds_list(File file) throws FileNotFoundException {
    InputStream reader = new FileInputStream(file);
    List<String> lines = InputExtensions.readLinesFromInputStream(reader);
    return load_thresholds_list(lines);
  }

  public static PvalueBsearchList load_from_file(File file) throws FileNotFoundException {
    return new PvalueBsearchList(load_thresholds_list(file));
  }
}
