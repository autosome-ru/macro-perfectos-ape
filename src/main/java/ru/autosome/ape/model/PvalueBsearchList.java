package ru.autosome.ape.model;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.commons.importer.InputExtensions;
import ru.autosome.commons.model.BoundaryType;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// List of pvalue-threshold pairs sorted by threshold ascending
public class PvalueBsearchList {
  public static class ThresholdPvaluePair {
    public final Double threshold;
    public final Double pvalue;

    public ThresholdPvaluePair(Double threshold, Double pvalue) {
      this.threshold = threshold;
      this.pvalue = pvalue;
    }

    public ThresholdPvaluePair(CanFindThreshold.ThresholdInfo info) {
      this.threshold = info.threshold;
      this.pvalue = info.real_pvalue;
    }

    static final Comparator<ThresholdPvaluePair> thresholdComparator =
        Comparator.comparing(o -> (o.threshold));

    // reversed comparison (thresholds are sorted ascending, so pvalues descending)
    static final Comparator<ThresholdPvaluePair> pvalueComparator =
        Comparator.comparing(o -> (-o.pvalue));


    @Override
    public boolean equals(Object other) {
      if (other instanceof ThresholdPvaluePair) {
        ThresholdPvaluePair otherConv = (ThresholdPvaluePair) other;
        return threshold.equals(otherConv.threshold) && pvalue.equals(otherConv.pvalue);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      int hash = 1;
      hash  = hash * 17 + threshold.hashCode();
      hash  = hash * 31 + pvalue.hashCode();
      return hash;
    }

    @Override
    public String toString() {
      return threshold + "\t" + pvalue;
    }
  }


  private final List<ThresholdPvaluePair> list;
  public PvalueBsearchList() {
    this.list = new ArrayList<>();
  }
  public PvalueBsearchList(List<ThresholdPvaluePair> infos) {
    infos.sort(ThresholdPvaluePair.thresholdComparator);
    this.list = without_consequent_duplicates(without_inf_nan_scores(without_zero_pvalue(infos)));
  }

  private List<ThresholdPvaluePair> without_consequent_duplicates(List<ThresholdPvaluePair> infos) {
    List<ThresholdPvaluePair> reduced_infos;
    reduced_infos = new ArrayList<>();
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
    reduced_infos = new ArrayList<>();
    for (ThresholdPvaluePair info: infos) {
      if (info.pvalue != 0) {
        reduced_infos.add(info);
      }
    }
    return reduced_infos;
  }

  private List<ThresholdPvaluePair> without_inf_nan_scores(List<ThresholdPvaluePair> infos) {
    List<ThresholdPvaluePair> reduced_infos;
    reduced_infos = new ArrayList<>();
    for (ThresholdPvaluePair info: infos) {
      Double score = info.threshold;
      if (!score.isNaN() && !score.isInfinite()) {
        reduced_infos.add(info);
      }
    }
    return reduced_infos;
  }

  public double combine_pvalues(double pvalue_1, double pvalue_2) {
    return Math.sqrt(pvalue_1 * pvalue_2);
  }

  public double pvalue_by_threshold(double threshold) {
    int index = Collections.binarySearch(list, new ThresholdPvaluePair(threshold, null), ThresholdPvaluePair.thresholdComparator);
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

  public ThresholdPvaluePair thresholdInfoByPvalue(double pvalue, BoundaryType boundaryType) {
    if (boundaryType == BoundaryType.STRONG) {
      return strongThresholdInfoByPvalue(pvalue);
    } else {
      return weakThresholdInfoByPvalue(pvalue);
    }
  }

  private ThresholdPvaluePair strongThresholdInfoByPvalue(double pvalue) {
    int index = Collections.binarySearch(list, new ThresholdPvaluePair(null, pvalue), ThresholdPvaluePair.pvalueComparator);
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

  private ThresholdPvaluePair weakThresholdInfoByPvalue(double pvalue) {
    int index = Collections.binarySearch(list, new ThresholdPvaluePair(null, pvalue), ThresholdPvaluePair.pvalueComparator);
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
    List<ThresholdPvaluePair> result = new ArrayList<>();
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
