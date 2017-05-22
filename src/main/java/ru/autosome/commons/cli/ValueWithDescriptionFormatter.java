package ru.autosome.commons.cli;

import java.util.List;

public interface ValueWithDescriptionFormatter<ResultInfo>  {
  String formatParameter(List<ValueWithDescription> configuration);
  String formatResult(List<ValueWithDescription> results);
  String formatTable(List<TabularParameterConfig<ResultInfo>> columns, List<ResultInfo> data);
}
