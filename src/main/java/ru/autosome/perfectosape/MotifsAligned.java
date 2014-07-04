package ru.autosome.perfectosape;

public class MotifsAligned<T extends Alignable<T>> {
  public final T firstMotifAligned;
  public final T secondMotifAligned;
  private final Position relative_position;

  private final int first_length;
  private final int second_length;

  public MotifsAligned(T first_pwm_unaligned, T second_pwm_unaligned, Position relative_position) {
    first_length = first_pwm_unaligned.length();
    second_length = second_pwm_unaligned.length();
    this.relative_position = relative_position;

    T first_tmp = first_pwm_unaligned;
    T second_tmp = second_pwm_unaligned;
    if (isReverseComplement()) {
      second_tmp = second_tmp.reverseComplement();
    }

    if (shift() > 0) {
      second_tmp = second_tmp.leftAugment(shift());
    } else {
      first_tmp = first_tmp.leftAugment(-shift());
    }

    firstMotifAligned = first_tmp.rightAugment(length() - first_tmp.length());
    secondMotifAligned = second_tmp.rightAugment(length() - second_tmp.length());
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

  boolean isDirect() {
    return relative_position.directStrand;
  }

  boolean isReverseComplement() {
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


  public String first_pwm_alignment() {
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

  public String second_pwm_alignment() {
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
    return first_pwm_alignment() + "\n" + second_pwm_alignment();
  }
}
