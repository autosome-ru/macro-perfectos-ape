package ru.autosome.commons.cli;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ReportLayout<ResultInfo> {
  public List<ValueWithDescription> parameters;
  public List<ParameterDescription<ResultInfo>> resulting_values;

  public ReportLayout() {
    parameters = new ArrayList<>();
    resulting_values = new ArrayList<>();
  }

  public void add_parameter(String param_name, String description, Object value) {
    parameters.add(new ValueWithDescription(param_name, description, value));
  }

  public void background_parameter(String param_name, String description, GeneralizedBackgroundModel background) {
    if (!background.is_wordwise()) {
      add_parameter(param_name, description, background.toString());
    }
  }

  public void add_resulting_value(String param_name, String description, Function<ResultInfo, Object> callback) {
    resulting_values.add(new ParameterDescription<>(param_name, description, callback));
  }
}
