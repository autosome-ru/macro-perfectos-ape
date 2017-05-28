package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.model.BoundaryType;

import java.util.List;

public interface CanFindThreshold {

  FoundedThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType);

  List<FoundedThresholdInfo> thresholdsByPvalues(List<Double> pvalues, BoundaryType boundaryType);
}
