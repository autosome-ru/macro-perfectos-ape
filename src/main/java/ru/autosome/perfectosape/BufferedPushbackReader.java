package ru.autosome.perfectosape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;

public class BufferedPushbackReader extends BufferedReader {
  private BufferedReader reader;
  private Deque<Character> buf;
  BufferedPushbackReader(InputStream in) {
    super(new InputStreamReader(in));
    buf = new ArrayDeque<Character>();
    reader = new BufferedReader(new InputStreamReader(in));

  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    int numRead = 0;

    int pos = off;
    while (pos < off + len && !buf.isEmpty()) {
      cbuf[pos] = buf.pop();
      ++pos;
      ++numRead;
    }
    if (pos < off + len) {
      int res = reader.read(cbuf, pos, len - (pos - off));
      if (res > 0)
        numRead += res;
    }
    return (numRead > 0) ? numRead : -1;
  }

  @Override
  public int read() throws IOException {
    if (!buf.isEmpty()) {
      return buf.pop();
    } else {
      return reader.read();
    }
  }

  @Override
  public int read(char[] cbuf) throws IOException {
    int pos = 0;
    while(!buf.isEmpty()) {
      cbuf[pos] = buf.pop();
      ++pos;
    }
    boolean eof = false;
    while (!eof) {
      int c = reader.read();
      if (c == -1) {
        eof = true;
      } else {
        cbuf[pos] = (char)c;
        ++pos;
      }

    }
    return (pos > 0) ? pos : -1;
  }
  @Override
  public String readLine() throws IOException {
    StringBuilder builder = new StringBuilder();

    boolean lineEnded = false;
    while (!lineEnded) {
      int c = read();
      if (c == -1){
        lineEnded = true;
      } else if (c == '\r' || c =='\n') {
        lineEnded = true;
        unread(c);
      } else {
        builder.append((char)c);
      }
    }
    eatEndOfLine();
    return builder.toString();
  }

  // return true if some symbols eaten
  public boolean eatEndOfLine() throws IOException {
    int c = read();
    if (c == -1) {
      return false;
    } else if(c == '\n') {
      return true;
    } else if (c == '\r') {
      int next_c = read();
      if (next_c != -1 && next_c != '\n') {
        unread(next_c);
      }
      return true;
    } else {
      unread(c);
      return false;
    }
  }

  @Override
  public void close() throws IOException {
    buf.clear();
    reader.close();
  }

  public void unread(char[] cbuf, int off, int len) throws IOException {
    for (int pos = off+len-1; pos >= off; --pos) {
      buf.push(cbuf[pos]);
    }
  }
  public void unread(char[] cbuf) throws IOException {
    unread(cbuf, 0 , cbuf.length);
  }
  public void unread(int c) throws IOException {
    buf.push((char)c);
  }
  public void unreadLine(String s) throws IOException {
    unread((s+"\n").toCharArray());
  }
}
