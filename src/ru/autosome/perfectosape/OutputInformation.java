package ru.autosome.perfectosape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OutputInformation {
  public interface Callback {
    String run(Object cell);
  }
  private ArrayList<String> table_parameter_descriptions;
  private ArrayList<String> parameter_descriptions;
  private ArrayList<String> parameter_value_infos;
  private ArrayList<String> resulting_value_descriptions;
  private ArrayList<String> resulting_value_infos;

  private ArrayList<String> table_headers;
  private ArrayList<String> table_columns;
  private Map<String, Callback> table_column_callbacks;

  public ArrayList<? extends ResultInfo> data;

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

  OutputInformation(ArrayList<? extends ResultInfo> data) {
    initialize();
    this.data = data;
  }

  public void add_parameter(String param_name, String description, Object value) {
    parameter_descriptions.add(parameter_description_string(param_name, description));
    parameter_value_infos.add("# " + param_name + " = " + value.toString());
  }

  public void background_parameter(String param_name, String description, BackgroundModel background) {
    if (!background.is_wordwise()) {
      add_parameter(param_name, description, background.toString());
    }
  }

  public void add_table_parameter(String param_name, String description, String key_in_hash) {
    table_parameter_descriptions.add(parameter_description_string(param_name, description));
    add_table_parameter_without_description(param_name, key_in_hash);
  }

  void add_table_parameter(String param_name, String description, String key_in_hash, Callback callback) {
    table_parameter_descriptions.add(parameter_description_string(param_name, description));
    add_table_parameter_without_description(param_name, key_in_hash, callback);
  }

  void add_table_parameter_without_description(String param_name, String key_in_hash) {
    table_headers.add(param_name);
    table_columns.add(key_in_hash);
  }

  void add_table_parameter_without_description(String param_name, String key_in_hash, Callback callback) {
    add_table_parameter_without_description(param_name, key_in_hash);
    table_column_callbacks.put(key_in_hash, callback);
  }

  String parameter_description_string(String param_name, String description) {
    return "# " + param_name + ": " + description;
  }

  public String report() {
    ArrayList<ArrayList<String>> tmp = new ArrayList<ArrayList<String>>();
    tmp.add(parameters_info());
    tmp.add(resulting_values_info());
    tmp.add(resulting_table());

    ArrayList<Object> results = new ArrayList<Object>();
    for (ArrayList<String> x : tmp) {
      if (!x.isEmpty()) {
        results.add(StringExtensions.join(x, "\n"));
      }
    }
    return StringExtensions.join(results, "\n#\n");
  }

  ArrayList<String> parameters_info() {
    ArrayList<String> result = new ArrayList<String>();
    for (String x : parameter_descriptions) {
      result.add(x);
    }
    for (String x : parameter_value_infos) {
      result.add(x);
    }
    return result;
  }

  ArrayList<String> resulting_values_info() {
    ArrayList<String> result = new ArrayList<String>();
    for (String x : resulting_value_descriptions) {
      result.add(x);
    }
    for (String x : resulting_value_infos) {
      result.add(x);
    }
    return result;
  }

  ArrayList<String> resulting_table() {
    if (data == null) {
      return new ArrayList<String>();
    } else {
      ArrayList<String> result = new ArrayList<String>();
      for (String x : table_parameter_descriptions) {
        result.add(x);
      }
      result.add(header_content());
      ArrayList<String> table_content = table_content();
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
    ArrayList<String> cell_contents = new ArrayList<String>();
    for (String column_name : table_columns) {
      if (table_column_callbacks.containsKey(column_name)) {
        Callback callback = table_column_callbacks.get(column_name);
        cell_contents.add( callback.run(info.get(column_name)).toString());
      } else {
        cell_contents.add(info.get(column_name).toString());
      }
    }
    return StringExtensions.join(cell_contents, "\t");
  }
  ArrayList<String> table_content() {
    ArrayList<String> result = new ArrayList<String>();
    for (ResultInfo info : data) {
      result.add(row_content(info));
    }
    return result;
  }
}
