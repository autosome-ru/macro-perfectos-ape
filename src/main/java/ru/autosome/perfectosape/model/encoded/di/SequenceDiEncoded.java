package ru.autosome.perfectosape.model.encoded.di;

import ru.autosome.commons.model.Alphabet;
import ru.autosome.perfectosape.model.Sequence;
import ru.autosome.perfectosape.model.encoded.EncodedSequenceType;

public class SequenceDiEncoded implements EncodedSequenceType {
  final public byte[] directSequence;
  final public byte[] revcompSequence;

  public SequenceDiEncoded(byte[] directSequence, byte[] revcompSequence) {
    if (directSequence.length != revcompSequence.length) {
      throw new IllegalArgumentException("direct and revcomp sequences should be of equal length");
    }
    this.directSequence = directSequence;
    this.revcompSequence = revcompSequence;
  }

  @Override
  public int length() {
    return directSequence.length + 1;
  }

  @Override
  public String toString() {
    return Alphabet.diACGTN.decodeString(directSequence);
  }

  public static SequenceDiEncoded encode(Sequence sequence) {
    byte[] directSeq = Alphabet.diACGTN.convertString(sequence.sequenceString());
    byte[] revcompSeq = Alphabet.diACGTN.convertString(sequence.reverseComplementString());
    return new SequenceDiEncoded(directSeq, revcompSeq);
  }
}
