package ru.autosome.commons.cli;

import ru.autosome.commons.support.StringExtensions;

import java.util.ArrayList;
import java.util.List;

public class TextListReporter<ResultInfo> implements ListReporter<ResultInfo> {

  @Override
  public String report(List<ResultInfo> data, ReportListLayout<ResultInfo> layout) {
    List<String> sections = new ArrayList<>();
    sections.add(formatParameter(layout.parameters));
    sections.add(formatTable(layout.columns, data));

    List<String> results = new ArrayList<>();
    for (String section : sections) {
      if (!section.isEmpty()) {
        results.add(section);
      }
    }
    return StringExtensions.join(results, "\n#\n");
  }

  protected String parameter_description_string(String param_name, String description) {
    return "# " + param_name + ": " + description;
  }

  protected String formatParameter(List<ValueWithDescription> parameters) {
    List<String> descriptions = new ArrayList<>();
    List<String> values = new ArrayList<>();
    for (ValueWithDescription parameter: parameters) {
      if (parameter.description != null) {
        descriptions.add(parameter_description_string(parameter.name, parameter.description));
      }
      values.add("# " + parameter.name + " = " + parameter.value);
    }
    return StringExtensions.glueSections(
        StringExtensions.join(descriptions, "\n"),
        StringExtensions.join(values, "\n")
    );
  }

  protected String formatRow(List<ParameterDescription<ResultInfo>> columns, ResultInfo rowData) {
    List<String> rowCells = new ArrayList<>();
    for (ParameterDescription<ResultInfo> parameter : columns) {
      rowCells.add(parameter.callback.apply(rowData).toString());
    }
    return StringExtensions.join(rowCells, "\t");
  }

  protected String formatTable(List<ParameterDescription<ResultInfo>> columns, List<ResultInfo> data) {
    if (data == null) {
      return "";
    } else {
      List<String> descriptionRows = new ArrayList<>();
      List<String> table_headers = new ArrayList<>();
      for (ParameterDescription<ResultInfo> parameter : columns) {
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

      return StringExtensions.glueSections(
          StringExtensions.join(descriptionRows, "\n"),
          header,
          StringExtensions.join(tableRows, "\n")
      );
    }
  }
}
