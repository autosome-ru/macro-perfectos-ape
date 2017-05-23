package ru.autosome.commons.cli;

public class ParameterDescription<ResultInfo> {
  public final String name;
  public final String description;
  public final java.util.function.Function<ResultInfo, Object> callback;
  public ParameterDescription(String name, String description, java.util.function.Function<ResultInfo, Object> callback) {
    this.name = name;
    this.description = description;
    this.callback = callback;
  }
}
