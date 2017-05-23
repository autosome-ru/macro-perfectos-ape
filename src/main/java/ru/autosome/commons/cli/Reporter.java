package ru.autosome.commons.cli;

import java.util.ArrayList;
import java.util.List;

public interface Reporter<ResultInfo> {
  default String report(ReportLayout<ResultInfo> layout) {
    return report(new ArrayList<>(), layout);
  }

  String report(List<ResultInfo> data, ReportLayout<ResultInfo> layout);
}
