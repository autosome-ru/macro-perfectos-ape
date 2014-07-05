package ru.autosome.perfectosape;

import ru.autosome.perfectosape.motifModels.Alignable;

public class PWMAligned<ModelType extends Alignable<ModelType>> {
  public final ModelType firstModelAligned;
  public final ModelType secondModelAligned;
  public final Position relative_position;

  private int first_length, second_length;

  public PWMAligned(ModelType first_model_unaligned, ModelType second_model_unaligned, Position relative_position) {
    first_length = first_model_unaligned.length();
    second_length = second_model_unaligned.length();
    this.relative_position = relative_position;

    ModelType first_tmp = first_model_unaligned;
    ModelType second_tmp = second_model_unaligned;
    if (isReverseComplement()) {
      second_tmp = second_tmp.reverseComplement();
    }

    if (shift() > 0) {
      second_tmp = second_tmp.leftAugment(shift());
    } else {
      first_tmp = first_tmp.leftAugment(-shift());
    }

    firstModelAligned = first_tmp.rightAugment(length() - first_tmp.length());
    secondModelAligned = second_tmp.rightAugment(length() - second_tmp.length());
  }

  public int shift() {
    return relative_position.position;
  }
  public String orientation() {
    return relative_position.strand();
  }
  public int length() {
    if (shift() > 0) {
      return Math.max(first_length, second_length + shift());
    } else {
      return Math.max(first_length - shift(), second_length);
    }
  }

  public boolean isDirect() {
    return relative_position.directStrand;
  }

  public boolean isReverseComplement() {
    return !relative_position.directStrand;
  }

  private boolean isFirstOverlapsPosition(int position) {
    if (! (position >= 0 && position < length())) {
      return false;
    }

    if (shift() > 0) {
      return position < first_length;
    } else {
      return (position >= -shift()) && (position < -shift() + first_length);
    }
  }

  private boolean isSecondOverlapsPosition(int position) {
    if (! (position >= 0 && position < length())) {
      return false;
    }

    if (shift() > 0) {
      return (position >= shift()) && (position < shift() + second_length);
    } else {
      return position < second_length;
    }
  }

  public int overlapSize() {
    int sum = 0;
    for (int pos = 0; pos < length(); ++pos) {
      if (isFirstOverlapsPosition(pos) && isSecondOverlapsPosition(pos)) {
         sum += 1;
      }
    }
    return sum;
  }


  public String first_model_alignment() {
    StringBuilder builder = new StringBuilder();
    for (int pos = 0; pos < length(); ++pos) {
      if (isFirstOverlapsPosition(pos)) {
        builder.append('>');
      } else {
        builder.append('.');
      }
    }
    return builder.toString();
  }

  public String second_model_alignment() {
    StringBuilder builder = new StringBuilder();
    for (int pos = 0; pos < length(); ++pos) {
      if (isSecondOverlapsPosition(pos)) {
        if (isDirect()) {
          builder.append('>');
        } else {
          builder.append('<');
        }
      } else {
        builder.append('.');
      }
    }
    return builder.toString();
  }

  @Override
  public String toString() {
    return first_model_alignment() + "\n" + second_model_alignment();
  }
}
