package ru.autosome.macroape;

import java.util.ArrayList;

class StringExtensions {
  public static String join(ArrayList<? extends Object> array, String separator) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < array.size(); ++i) {
      if (i != 0) {
        result.append(separator);
      }
      result.append(array.get(i).toString());
    }
    return result.toString();
  }
}
