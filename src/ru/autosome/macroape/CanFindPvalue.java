package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.HashMap;

public interface CanFindPvalue {
  public HashMap<String, Object> parameters();
  public void set_parameters(HashMap<String, Object> parameters);
  public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds);
  public PvalueInfo pvalue_by_threshold(double threshold);
  public void set_discretization(Double discretization);
  public void set_background(BackgroundModel background);
  public void set_max_hash_size(Integer max_hash_size);
}
