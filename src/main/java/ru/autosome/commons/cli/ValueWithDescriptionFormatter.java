package ru.autosome.commons.cli;

import java.util.List;

public interface ValueWithDescriptionFormatter<CertainResultInfo extends ResultInfo>  {
  String formatParameter(List<ValueWithDescription> configuration);
  String formatResult(List<ValueWithDescription> results);
  String formatTable(List<TabularParameterConfig<CertainResultInfo>> columns, List<CertainResultInfo> data);
}
