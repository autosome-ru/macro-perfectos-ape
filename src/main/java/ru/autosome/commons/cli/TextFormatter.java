package ru.autosome.commons.cli;

import ru.autosome.commons.support.StringExtensions;

import java.util.ArrayList;
import java.util.List;

class TextFormatter<ResultInfo> {

  protected String parameter_description_string(String param_name, String description) {
    return "# " + param_name + ": " + description;
  }

  protected String glueSections(String... sections) {
    List<String> nonEmptySections = new ArrayList<>();
    for (String section : sections) {
      if (!section.isEmpty()) {
        nonEmptySections.add(section);
      }
    }
    return StringExtensions.join(nonEmptySections, "\n");
  }

  public String formatParameter(List<ValueWithDescription> parameters) {
    List<String> descriptions = new ArrayList<>();
    List<String> values = new ArrayList<>();
    for (ValueWithDescription parameter: parameters) {
      if (parameter.description != null) {
        descriptions.add(parameter_description_string(parameter.name, parameter.description));
      }
      values.add("# " + parameter.name + " = " + parameter.value);
    }
    return glueSections(
        StringExtensions.join(descriptions, "\n"),
        StringExtensions.join(values, "\n")
        );
  }

  public String formatResult(List<ValueWithDescription> parameters) {
    List<String> descriptions = new ArrayList<>();
    List<String> values = new ArrayList<>();
    for (ValueWithDescription parameter: parameters) {
      if (parameter.description != null) {
        descriptions.add(parameter_description_string(parameter.name, parameter.description));
      }
      values.add(parameter.name + "\t" + parameter.value);
    }
    return glueSections(
        StringExtensions.join(descriptions, "\n"),
        StringExtensions.join(values, "\n")
    );
  }

  protected String formatRow(List<TabularParameterConfig<ResultInfo>> columns, ResultInfo rowData) {
    List<String> rowCells = new ArrayList<>();
    for (TabularParameterConfig<ResultInfo> parameter : columns) {
      rowCells.add(parameter.callback.apply(rowData).toString());
    }
    return StringExtensions.join(rowCells, "\t");
  }

  public String formatTable(List<TabularParameterConfig<ResultInfo>> columns, List<ResultInfo> data) {
    if (data == null) {
      return "";
    } else {
      List<String> descriptionRows = new ArrayList<>();
      List<String> table_headers = new ArrayList<>();
      for (TabularParameterConfig<ResultInfo> parameter : columns) {
        if (parameter.description != null) {
          descriptionRows.add(parameter_description_string(parameter.name, parameter.description));
        }
        table_headers.add(parameter.name);
      }
      String header = "";
      if (!table_headers.isEmpty()) {
        header = "# " + StringExtensions.join(table_headers, "\t");
      }
      List<String> tableRows = new ArrayList<>();
      for (ResultInfo row : data) {
        tableRows.add(formatRow(columns, row));
      }

      return glueSections(
          StringExtensions.join(descriptionRows, "\n"),
          header,
          StringExtensions.join(tableRows, "\n")
      );
    }
  }
}
