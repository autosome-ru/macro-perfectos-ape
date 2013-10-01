package ru.autosome.macroape;


import java.util.ArrayList;

public class Helper {
  static void print_help_if_requested(ArrayList<String> argv, String doc) {
    if (argv.isEmpty() || ArrayExtensions.contain(argv, "-h") || ArrayExtensions.contain(argv, "--h")
            || ArrayExtensions.contain(argv, "-help") || ArrayExtensions.contain(argv, "--help")) {
      System.err.println(doc);
      System.exit(1);
    }
  }
}
