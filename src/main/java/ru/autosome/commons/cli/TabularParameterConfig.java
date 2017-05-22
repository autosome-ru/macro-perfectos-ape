package ru.autosome.commons.cli;

public class TabularParameterConfig<ResultInfo> {
  public final String name;
  public final String description;
  public final java.util.function.Function<ResultInfo, Object> callback;
  public TabularParameterConfig(String name, String description, java.util.function.Function<ResultInfo, Object> callback) {
    this.name = name;
    this.description = description;
    this.callback = callback;
  }
}
