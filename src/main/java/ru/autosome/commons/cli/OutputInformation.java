package ru.autosome.commons.cli;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.support.StringExtensions;

import java.util.ArrayList;
import java.util.List;

public class OutputInformation {
  public interface Callback<CertainResultInfo extends ResultInfo>  {
    Object run(CertainResultInfo cell);
  }
  private List<ValueWithDescription> parameters;
  private List<ValueWithDescription> resulting_values;
  private List<TabularParameterConfig> columns;

  public List<? extends ResultInfo> data;

  private void initialize() {
    parameters = new ArrayList<ValueWithDescription>();
    resulting_values = new ArrayList<ValueWithDescription>();
    columns = new ArrayList<TabularParameterConfig>();
  }

  public OutputInformation() {
    initialize();
  }

  public void add_parameter(String param_name, String description, Object value) {
    parameters.add(new ValueWithDescription(param_name, description, value));
  }

  public void background_parameter(String param_name, String description, GeneralizedBackgroundModel background) {
    if (!background.is_wordwise()) {
      add_parameter(param_name, description, background.toString());
    }
  }

  public void add_table_parameter(String param_name, String description, String key_in_hash) {
    columns.add(new TabularParameterConfig(param_name, description, key_in_hash));
  }

  public void add_table_parameter(String param_name, String description, String key_in_hash, Callback callback) {
    columns.add(new TabularParameterConfig(param_name, description, key_in_hash, callback));
  }

  public void add_table_parameter_without_description(String param_name, String key_in_hash) {
    columns.add(new TabularParameterConfig(param_name, null, key_in_hash));
  }

  public void add_table_parameter_without_description(String param_name, String key_in_hash, Callback callback) {
    columns.add(new TabularParameterConfig(param_name, null, key_in_hash, callback));
  }

  public void add_resulting_value(String param_name, String description, Object value) {
    resulting_values.add(new ValueWithDescription(param_name, description, value));
  }

  public String report() {
    ValueWithDescriptionFormatter formatter = new TextFormatter();
    List<String> sections = new ArrayList<String>();
    sections.add(formatter.formatParameter(parameters));
    sections.add(formatter.formatResult(resulting_values));
    sections.add(formatter.formatTable(columns, data));

    List<String> results = new ArrayList<String>();
    for (String section: sections) {
      if (!section.isEmpty()) {
        results.add(section);
      }
    }
    return StringExtensions.join(results, "\n#\n");
  }
}
