package ru.autosome.commons.cli;

import ru.autosome.commons.support.StringExtensions;

import java.util.ArrayList;
import java.util.List;

class TextFormatter<ResultInfo> implements ValueWithDescriptionFormatter<ResultInfo> {

  protected String parameter_description_string(String param_name, String description) {
    return "# " + param_name + ": " + description;
  }

  protected String glueSections(String... sections) {
    List<String> nonEmptySections = new ArrayList<String>();
    for (String section : sections) {
      if (!section.isEmpty()) {
        nonEmptySections.add(section);
      }
    }
    return StringExtensions.join(nonEmptySections, "\n");
  }

  @Override
  public String formatParameter(List<ValueWithDescription> parameters) {
    List<String> descriptions = new ArrayList<String>();
    List<String> values = new ArrayList<String>();
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

  @Override
  public String formatResult(List<ValueWithDescription> parameters) {
    List<String> descriptions = new ArrayList<String>();
    List<String> values = new ArrayList<String>();
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

  public String formatRow(List<TabularParameterConfig<ResultInfo>> columns, ResultInfo rowData) {
    List<String> rowCells = new ArrayList<String>();
    for (TabularParameterConfig<ResultInfo> parameter : columns) {
      rowCells.add(parameter.callback.run(rowData).toString());
    }
    return StringExtensions.join(rowCells, "\t");
  }

  @Override
  public String formatTable(List<TabularParameterConfig<ResultInfo>> columns, List<ResultInfo> data) {
    if (data == null) {
      return "";
    } else {
      List<String> descriptionRows = new ArrayList<String>();
      List<String> table_headers = new ArrayList<String>();
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
      List<String> tableRows = new ArrayList<String>();
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
