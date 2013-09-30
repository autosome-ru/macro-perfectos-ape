package ru.autosome.macroape;

import java.util.ArrayList;

public class OutputInformation {
  private ArrayList<String> table_parameter_descriptions;
  private ArrayList<String> parameter_descriptions;
  private ArrayList<String> parameter_value_infos;
  private ArrayList<String> resulting_value_descriptions;
  private ArrayList<String> resulting_value_infos;

  private ArrayList<String> table_headers;
  private ArrayList<String> table_rows;
  private ArrayList<String> table_rows_callbacks;

  public ArrayList<? extends ResultInfo> data;

  private void initialize() {
    table_parameter_descriptions = new ArrayList<String>();

    parameter_descriptions = new ArrayList<String>();
    parameter_value_infos = new ArrayList<String>();

    resulting_value_descriptions = new ArrayList<String>();
    resulting_value_infos = new ArrayList<String>();

    table_headers = new ArrayList<String>();
    table_rows = new ArrayList<String>();
    table_rows_callbacks = new ArrayList<String>();
  }

  OutputInformation() {
    initialize();
  }

  OutputInformation(ArrayList<? extends ResultInfo> data) {
    initialize();
    this.data = data;
  }

  void add_parameter(String param_name, String description, Object value) {
    parameter_descriptions.add(parameter_description_string(param_name, description));
    parameter_value_infos.add("# " + param_name + " = " + value.toString());
  }

  void background_parameter(String param_name, String description, BackgroundModel background) {
    if (!background.is_wordwise()) {
      add_parameter(param_name, description, background.toString());
    }
  }

  void add_table_parameter(String param_name, String description, String key_in_hash) {
    table_parameter_descriptions.add(parameter_description_string(param_name, description));
    add_table_parameter_without_description(param_name, key_in_hash);
  }

  void add_table_parameter_without_description(String param_name, String key_in_hash) {
    table_headers.add(param_name);
    table_rows.add(key_in_hash);
  }

  String parameter_description_string(String param_name, String description) {
    return "# " + param_name + ": " + description;
  }

  String result() {
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

  ArrayList<String> table_content() {
    ArrayList<String> result = new ArrayList<String>();
    for (ResultInfo info : data) {
      ArrayList<String> tmp = new ArrayList<String>();
      for (String row : table_rows) {
        tmp.add(info.get(row).toString());
      }
      result.add(StringExtensions.join(tmp, "\t"));
    }
    return result;
  }
}
