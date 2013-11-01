package ru.autosome.perfectosape;

import java.util.ArrayList;

class StringExtensions {
  public static String join(ArrayList<?> array, String separator) {
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
