package ru.autosome.ape.model;

import ru.autosome.commons.importer.InputExtensions;
import ru.autosome.commons.model.BoundaryType;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// List of pvalue-threshold pairs sorted by threshold ascending
public class PvalueBsearchList {
  private final List<ThresholdPvaluePair> list;

  public PvalueBsearchList(List<ThresholdPvaluePair> infos) {
    this.list = infos.stream()
                    .filter((ThresholdPvaluePair info) -> info.pvalue != 0)
                    .filter((ThresholdPvaluePair info) -> {
                      Double score = info.threshold;
                      return !score.isNaN() && !score.isInfinite();
                    })
                    .distinct()
                    .sorted(ThresholdPvaluePair.thresholdComparator)
                    .collect(Collectors.toList());
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
