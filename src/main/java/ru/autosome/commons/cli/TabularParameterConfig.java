package ru.autosome.commons.cli;

public class TabularParameterConfig<ResultInfo> {
  public final String name;
  public final String description;
  public final ReportLayout.Callback<ResultInfo> callback;
  public TabularParameterConfig(String name, String description, ReportLayout.Callback<ResultInfo> callback) {
    this.name = name;
    this.description = description;
    this.callback = callback;
  }
}
