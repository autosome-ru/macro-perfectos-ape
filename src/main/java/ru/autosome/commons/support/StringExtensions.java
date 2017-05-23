package ru.autosome.commons.support;

import java.util.ArrayList;
import java.util.List;

public class StringExtensions {
  public static String join(List<?> array, String separator) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < array.size(); ++i) {
      if (i != 0) {
        result.append(separator);
      }
      result.append(array.get(i).toString());
    }
    return result.toString();
  }

  public static boolean startWith(String string, String stringStart) {
    return (string.length() >= stringStart.length()) && string.subSequence(0, stringStart.length()).equals(stringStart);
  }

  public static String glueSections(String... sections) {
    List<String> nonEmptySections = new ArrayList<>();
    for (String section : sections) {
      if (!section.isEmpty()) {
        nonEmptySections.add(section);
      }
    }
    return join(nonEmptySections, "\n");
  }
}
