package ru.autosome.commons.cli;

public class TabularParameterConfig<CertainResultInfo extends ResultInfo> {
  public final String name;
  public final String description;
  public final String key_in_hash;
  public final ReportLayout.Callback callback;
  public TabularParameterConfig(String name, String description, String key_in_hash, ReportLayout.Callback<CertainResultInfo> callback) {
    this.name = name;
    this.description = description;
    this.key_in_hash = key_in_hash;
    this.callback = callback;
  }
  public TabularParameterConfig(String name, String description, String key_in_hash) {
    this.name = name;
    this.description = description;
    this.key_in_hash = key_in_hash;
    this.callback = null;
  }
}
