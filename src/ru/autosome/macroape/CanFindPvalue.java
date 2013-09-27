package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.HashMap;

public interface CanFindPvalue {
    public HashMap<String, Object> parameters();
    public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds);
    public PvalueInfo pvalue_by_threshold(double threshold);
}
