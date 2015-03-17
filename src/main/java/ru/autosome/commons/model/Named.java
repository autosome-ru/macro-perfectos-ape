package ru.autosome.commons.model;

public class Named<Type> implements ru.autosome.commons.motifModel.Named {
  private final Type object;
  private String name;

  public Named(Type object) {
    this.object = object;
  }

  public Named(Type object, String name) {
    this.object = object;
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public Type getObject() {
    return object;
  }
}
