package ru.autosome.commons.cli;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.support.StringExtensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputInformation {
  public interface Callback<T extends ResultInfo> {
    Object run(T cell);
  }
  private List<String> table_parameter_descriptions;
  private List<String> parameter_descriptions;
  private List<String> parameter_value_infos;
  private List<String> resulting_value_descriptions;
  private List<String> resulting_value_infos;

  private List<String> table_headers;
  private List<String> table_columns;
  private Map<String, Callback> table_column_callbacks;

  public List<? extends ResultInfo> data;

  private void initialize() {
    table_parameter_descriptions = new ArrayList<String>();

    parameter_descriptions = new ArrayList<String>();
    parameter_value_infos = new ArrayList<String>();

    resulting_value_descriptions = new ArrayList<String>();
    resulting_value_infos = new ArrayList<String>();

    table_headers = new ArrayList<String>();
    table_columns = new ArrayList<String>();
    table_column_callbacks = new HashMap<String, Callback>();
  }

  public OutputInformation() {
    initialize();
  }

  public OutputInformation(List<? extends ResultInfo> data) {
    initialize();
    this.data = data;
  }

  public void add_parameter(String param_name, String description, Object value) {
    parameter_descriptions.add(parameter_description_string(param_name, description));
    parameter_value_infos.add("# " + param_name + " = " + value.toString());
  }

  public void background_parameter(String param_name, String description, GeneralizedBackgroundModel background) {
    if (!background.is_wordwise()) {
      add_parameter(param_name, description, background.toString());
    }
  }

  public void add_table_parameter(String param_name, String description, String key_in_hash) {
    table_parameter_descriptions.add(parameter_description_string(param_name, description));
    add_table_parameter_without_description(param_name, key_in_hash);
  }

  public void add_table_parameter(String param_name, String description, String key_in_hash, Callback callback) {
    table_parameter_descriptions.add(parameter_description_string(param_name, description));
    add_table_parameter_without_description(param_name, key_in_hash, callback);
  }

  public void add_table_parameter_without_description(String param_name, String key_in_hash) {
    table_headers.add(param_name);
    table_columns.add(key_in_hash);
  }

  public void add_table_parameter_without_description(String param_name, String key_in_hash, Callback callback) {
    add_table_parameter_without_description(param_name, key_in_hash);
    table_column_callbacks.put(key_in_hash, callback);
  }

  String parameter_description_string(String param_name, String description) {
    return "# " + param_name + ": " + description;
  }

  public void add_resulting_value(String param_name, String description, Object value) {
    resulting_value_descriptions.add(parameter_description_string(param_name, description));
    resulting_value_infos.add(param_name + "\t" + value);
  }

  public String report() {
    List<List<String>> tmp = new ArrayList<List<String>>();
    tmp.add(parameters_info());
    tmp.add(resulting_values_info());
    tmp.add(resulting_table());

    List<Object> results = new ArrayList<Object>();
    for (List<String> x : tmp) {
      if (!x.isEmpty()) {
        results.add(StringExtensions.join(x, "\n"));
      }
    }
    return StringExtensions.join(results, "\n#\n");
  }

  List<String> parameters_info() {
    List<String> result = new ArrayList<String>();
    for (String x : parameter_descriptions) {
      result.add(x);
    }
    for (String x : parameter_value_infos) {
      result.add(x);
    }
    return result;
  }

  List<String> resulting_values_info() {
    List<String> result = new ArrayList<String>();
    for (String x : resulting_value_descriptions) {
      result.add(x);
    }
    for (String x : resulting_value_infos) {
      result.add(x);
    }
    return result;
  }

  List<String> resulting_table() {
    if (data == null) {
      return new ArrayList<String>();
    } else {
      List<String> result = new ArrayList<String>();
      for (String x : table_parameter_descriptions) {
        result.add(x);
      }
      result.add(header_content());
      List<String> table_content = table_content();
      for (String x : table_content) {
        result.add(x);
      }
      return result;
    }
  }

  String header_content() {
    return "# " + StringExtensions.join(table_headers, "\t");
  }

  String row_content(ResultInfo info) {
    List<String> cell_contents = new ArrayList<String>();
    for (String column_name : table_columns) {
      if (table_column_callbacks.containsKey(column_name)) {
        Callback callback = table_column_callbacks.get(column_name);
        cell_contents.add(callback.run(info).toString());
      } else {
        cell_contents.add(info.get(column_name).toString());
      }
    }
    return StringExtensions.join(cell_contents, "\t");
  }
  List<String> table_content() {
    List<String> result = new ArrayList<String>();
    for (ResultInfo info : data) {
      result.add(row_content(info));
    }
    return result;
  }
}
