package ru.autosome.commons.cli;

public class ValueWithDescription {
  public final String name;
  public final String description;
  public final Object value;
  public ValueWithDescription(String name, String description, Object value) {
    this.name = name;
    this.description = description;
    this.value = value;
  }
}
