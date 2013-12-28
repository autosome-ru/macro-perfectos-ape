package ru.autosome.perfectosape;

import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MotifEvaluatorCollection implements Iterable<MotifEvaluatorCollection.MotifEvaluator> {
  public int size() {
    return collection.size();
  }

  public static class MotifEvaluator {
    public final PWM pwm;
    public final CanFindPvalue pvalueCalculator;
    public MotifEvaluator(PWM pwm, CanFindPvalue pvalueCalculator) {
      this.pwm = pwm;
      this.pvalueCalculator = pvalueCalculator;
    }
  }

  private List<MotifEvaluator> collection;

  public MotifEvaluatorCollection() {
    collection = new ArrayList<MotifEvaluator>();
  }

  public MotifEvaluatorCollection(List<MotifEvaluator> collection) {
    this.collection = collection;
  }

  public void add(PWM pwm, CanFindPvalue pvalueCalculator) {
    collection.add(new MotifEvaluator(pwm, pvalueCalculator));
  }

  @Override
  public Iterator<MotifEvaluator> iterator() {
    return collection.iterator();
  }

}
