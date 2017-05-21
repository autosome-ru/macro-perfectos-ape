package ru.autosome.commons.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class IOExtensions {
  public static void extract_doubles_from_input_stream(InputStream inputStream, List<Double> thresholds_list) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    while ((line = reader.readLine()) != null){
      for (String token: line.split("\\s+")) {
        thresholds_list.add(Double.valueOf(token));
      }
    }
  }
}
