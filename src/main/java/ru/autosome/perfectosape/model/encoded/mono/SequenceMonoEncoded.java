package ru.autosome.perfectosape.model.encoded.mono;

import ru.autosome.commons.model.Alphabet;
import ru.autosome.perfectosape.model.encoded.EncodedSequenceType;

public class SequenceMonoEncoded implements EncodedSequenceType {
  final public byte[] directSequence;
  final public byte[] revcompSequence;

  public SequenceMonoEncoded(byte[] directSequence, byte[] revcompSequence) {
    if (directSequence.length != revcompSequence.length) {
      throw new IllegalArgumentException("direct and revcomp sequences should be of equal length");
    }
    this.directSequence = directSequence;
    this.revcompSequence = revcompSequence;
  }

  @Override
  public int length() {
    return directSequence.length;
  }

  @Override
  public String toString() {
    return Alphabet.monoACGTN.decodeString(directSequence);
  }
}
