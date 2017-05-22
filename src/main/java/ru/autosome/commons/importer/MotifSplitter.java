package ru.autosome.commons.importer;

import java.util.ArrayList;
import java.util.List;

public class MotifSplitter {
  public final String splitter_pattern; // splitter between motifs
  public final String first_line_pattern; // start of line can become a line of cut, line itself is included in a motif
  MotifSplitter(String splitter_pattern, String first_line_pattern) {
    this.splitter_pattern = splitter_pattern;
    this.first_line_pattern = first_line_pattern;
  }
  public List<List<String>> split(List<String> strings) {
    List<List<String>> result = new ArrayList<>();
    List<String> chunk;
    chunk = new ArrayList<>();
    for (String string : strings) {
      if (string.matches(splitter_pattern)) {
        if (!chunk.isEmpty()) {
          result.add(chunk);
        }
        chunk = new ArrayList<>();
      } else if (string.matches(first_line_pattern)) {
        if (!chunk.isEmpty()) {
          result.add(chunk);
        }
        chunk = new ArrayList<>();
        chunk.add(string);
      } else {
        chunk.add(string);
      }
    }
    if (!chunk.isEmpty()) {
      result.add(chunk);
    }
    return result;
  }
}
