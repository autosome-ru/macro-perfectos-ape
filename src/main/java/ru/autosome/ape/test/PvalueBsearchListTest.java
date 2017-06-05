package ru.autosome.ape.test;

import org.junit.Assert;
import org.junit.Test;
import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.ape.model.ThresholdPvaluePair;
import ru.autosome.commons.model.BoundaryType;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

public class PvalueBsearchListTest {
  @Test
  public void testPvalueByThreshold(){
    List<ThresholdPvaluePair> pairs = new ArrayList<>();
    for (double threshold = 0.0; threshold < 10.0; threshold += 1) {
      pairs.add(new ThresholdPvaluePair(threshold, (10-threshold)/10));
    }
    PvalueBsearchList bsearchList = new PvalueBsearchList(pairs);
    Assert.assertEquals(new ThresholdPvaluePair(5.0, 0.5), bsearchList.thresholdInfoByPvalue(0.55, BoundaryType.STRONG));
    Assert.assertEquals(new ThresholdPvaluePair(4.0, 0.6), bsearchList.thresholdInfoByPvalue(0.55, BoundaryType.WEAK));
    Assert.assertEquals(new ThresholdPvaluePair(4.0, 0.6), bsearchList.thresholdInfoByPvalue(0.6, BoundaryType.STRONG));
    Assert.assertEquals(new ThresholdPvaluePair(4.0, 0.6), bsearchList.thresholdInfoByPvalue(0.6, BoundaryType.WEAK));
    Assert.assertEquals(0.4, bsearchList.pvalue_by_threshold(6), 1e-7);
    Assert.assertEquals(sqrt(0.4 * 0.5), bsearchList.pvalue_by_threshold(5.3), 1e-7);
    Assert.assertEquals(sqrt(0.4 * 0.5), bsearchList.pvalue_by_threshold(5.5), 1e-7);
    Assert.assertEquals(sqrt(0.4 * 0.5), bsearchList.pvalue_by_threshold(5.8), 1e-7);
  }
}
