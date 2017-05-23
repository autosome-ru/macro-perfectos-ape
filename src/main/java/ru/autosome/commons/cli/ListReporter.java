package ru.autosome.commons.cli;

import java.util.List;

public interface ListReporter<ResultInfo> {
  String report(List<ResultInfo> data, ReportListLayout<ResultInfo> layout);
}
