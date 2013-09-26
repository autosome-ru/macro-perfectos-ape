package ru.autosome.macroape;

import java.util.ArrayList;

public class StringExtensions {
  public static String join(ArrayList<? extends Object> array, String separator) {
    if (array.isEmpty()) {
      return "";
    } else {
      String result = "";
      for (int i = 0; i+1 < array.size(); ++i) {
        result += array.get(i).toString() + separator;
      }
      result += array.get(array.size() - 1);
      return result;
    }
  }
}
