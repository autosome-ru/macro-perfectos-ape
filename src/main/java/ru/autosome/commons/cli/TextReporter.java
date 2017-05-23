package ru.autosome.commons.cli;

import ru.autosome.commons.support.StringExtensions;

import java.util.ArrayList;
import java.util.List;

public class TextReporter<ResultInfo> implements Reporter<ResultInfo> {
  @Override
  public String report(ResultInfo data, ReportLayout<ResultInfo> layout) {
    List<String> sections = new ArrayList<>();
    sections.add(formatParameter(layout.parameters));
    sections.add(formatResult(data, layout.resulting_values));

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

  protected String formatResult(ResultInfo data, List<ParameterDescription<ResultInfo>> parameters) {
    List<String> descriptions = new ArrayList<>();
    List<String> values = new ArrayList<>();
    for (ParameterDescription<ResultInfo> parameter: parameters) {
      if (parameter.description != null) {
        descriptions.add(parameter_description_string(parameter.name, parameter.description));
      }
      values.add(parameter.name + "\t" + parameter.callback.apply(data));
    }
    return StringExtensions.glueSections(
        StringExtensions.join(descriptions, "\n"),
        StringExtensions.join(values, "\n")
    );
  }
}
