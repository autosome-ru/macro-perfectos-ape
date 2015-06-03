package ru.autosome.commons.importer_two.formatters;

public abstract class Formatter<Model> {
  abstract String formatModel(Model model);
}
