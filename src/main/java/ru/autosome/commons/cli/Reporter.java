package ru.autosome.commons.cli;

public interface Reporter<ResultInfo> {
  String report(ResultInfo data, ReportLayout<ResultInfo> layout);
}
