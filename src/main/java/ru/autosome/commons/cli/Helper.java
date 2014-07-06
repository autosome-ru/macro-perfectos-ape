package ru.autosome.commons.cli;

import ru.autosome.commons.support.ArrayExtensions;

import java.util.List;

public class Helper {
  public static void print_help_if_requested(List<String> argv, String doc) {
    if (argv.isEmpty() || ArrayExtensions.contain(argv, "-h") || ArrayExtensions.contain(argv, "--h")
         || ArrayExtensions.contain(argv, "-help") || ArrayExtensions.contain(argv, "--help")) {
      System.err.println(doc);
      System.exit(1);
    }
  }
}
