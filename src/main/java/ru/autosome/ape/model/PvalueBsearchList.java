package ru.autosome.ape.model;

import ru.autosome.commons.model.BoundaryType;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

  public void print_to_stream(Writer fw) throws IOException {
    for (ThresholdPvaluePair info : list) {
      fw.write(info + "\n");
    }
    fw.flush();
  }

  public void save_to_file(File file) throws IOException {
    FileWriter fw = new FileWriter(file);
    print_to_stream(fw);
    fw.close();
  }

  private static List<ThresholdPvaluePair> load_thresholds_list(BufferedReader reader) {
    return reader.lines()
               .map(line -> line.replaceAll("\\s+", "\t").split("\t"))
               .filter(tokens -> tokens.length >= 2)
               .map(tokens -> {
                 double threshold = Double.valueOf(tokens[0]);
                 double pvalue = Double.valueOf(tokens[1]);
                 return new ThresholdPvaluePair(threshold, pvalue);
               }).collect(Collectors.toList());
  }

  public static List<ThresholdPvaluePair> load_thresholds_list(File file) throws FileNotFoundException {
    return load_thresholds_list(new BufferedReader(new FileReader(file)));
  }

  public static PvalueBsearchList load_from_file(File file) throws FileNotFoundException {
    return new PvalueBsearchList(load_thresholds_list(file));
  }
}
