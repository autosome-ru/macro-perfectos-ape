package ru.autosome.perfectosape;

import ru.autosome.perfectosape.calculations.CanFindPvalue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PWMCollection implements Iterable<PWMCollection.PWMAugmented> {
  public static class PWMAugmented {
    public final PWM pwm;
    public final CanFindPvalue pvalueCalculator;
    public PWMAugmented(PWM pwm, CanFindPvalue pvalueCalculator) {
      this.pwm = pwm;
      this.pvalueCalculator = pvalueCalculator;
    }
  }

  private List<PWMAugmented> collection;

  public PWMCollection() {
    collection = new ArrayList<PWMAugmented>();
  }

  public PWMCollection(List<PWMAugmented> collection) {
    this.collection = collection;
  }

  public void add(PWM pwm, CanFindPvalue pvalueCalculator) {
    collection.add(new PWMAugmented(pwm, pvalueCalculator));
  }

  @Override
  public Iterator<PWMAugmented> iterator() {
    return collection.iterator();
  }

}
