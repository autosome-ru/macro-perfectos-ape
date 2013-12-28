package ru.autosome.perfectosape.formatters;

public class ResultInfo {
  public Object get(String key) {
    try {
      try {
        return this.getClass().getDeclaredField(key).get(this);
      } catch (NoSuchFieldException e) {
        return this.getClass().getMethod(key).invoke(this);
      }
    } catch (Exception e) {
      System.err.println("Tried to call " + this + "#" + key + "\n" + e);
      return null;
    }
  }
}
