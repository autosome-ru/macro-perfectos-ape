package ru.autosome.macroape;

import java.util.ArrayList;

public class OutputInformation {
  ArrayList<String> table_parameter_descriptions;
  ArrayList<String> parameter_descriptions;
  ArrayList<String> parameter_value_infos;
  ArrayList<String> resulting_value_descriptions;
  ArrayList<String> resulting_value_infos;

  ArrayList<String> table_headers;
  ArrayList<String> table_rows;
  ArrayList<String> table_rows_callbacks;

  final ArrayList<? extends ResultInfo> data;

  OutputInformation(ArrayList<? extends ResultInfo> data) {
    table_parameter_descriptions = new ArrayList<String>();

    parameter_descriptions = new ArrayList<String>();
    parameter_value_infos = new ArrayList<String>();

    resulting_value_descriptions = new ArrayList<String>();
    resulting_value_infos = new ArrayList<String>();

    table_headers = new ArrayList<String>();
    table_rows = new ArrayList<String>();
    table_rows_callbacks = new ArrayList<String>();

    this.data = data;
  }

  void add_parameter(String param_name, String description, Object value) {
    parameter_descriptions.add(parameter_description_string(param_name, description));
    parameter_value_infos.add("# " + param_name + " = " + value.toString());
  }

  public static boolean is_background_wordwise(double[] bckgr) {
    for (int i = 0; i < 4; ++i) {
      if (bckgr[i] != 1.0)  return false;
    }
    return true;
  }

  void background_parameter(String param_name, String description, double[] value) {
    if (! is_background_wordwise(value)) {
      String val_string = "";
      for (int i = 0; i < 3; ++i) {
        val_string += value[i] + ",";
      }
      val_string += value[3];
      add_parameter(param_name, description, val_string);
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
    for (ArrayList<String> x: tmp) {
      if(!x.isEmpty()){
        results.add(StringExtensions.join(x,"\n"));
      }
    }
    return StringExtensions.join(results, "\n#\n");
  }

  ArrayList<String> parameters_info() {
    ArrayList<String> result = new ArrayList<String>();
    for (String x: parameter_descriptions) {
      result.add(x);
    }
    for (String x: parameter_value_infos) {
      result.add(x);
    }
    return result;
  }

  ArrayList<String> resulting_values_info() {
    ArrayList<String> result = new ArrayList<String>();
    for (String x: resulting_value_descriptions) {
      result.add(x);
    }
    for (String x: resulting_value_infos) {
      result.add(x);
    }
    return result;
  }

  ArrayList<String> resulting_table() {
    if (data == null) {
      return new ArrayList<String>();
    } else {
      ArrayList<String> result = new ArrayList<String>();
      for (String x: table_parameter_descriptions) {
        result.add(x);
      }
      result.add(header_content());
      ArrayList<String> table_content = table_content();
      for (String x: table_content) {
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
    for (ResultInfo info: data) {
      ArrayList<String> tmp = new ArrayList<String>();
      for(String row: table_rows) {
        tmp.add(info.get(row).toString());
      }
      result.add(StringExtensions.join(tmp,"\t"));
    }
    return result;
  }
}
