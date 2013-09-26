package ru.autosome.macroape;

public class ResultInfo {
    public Object get(String key) {
        try {
            return this.getClass().getDeclaredField(key).get(this);
            //return this.getClass().getMethod(key, null).invoke(this);
        } catch (Exception e) {
            System.err.println("Tried to call " + this + "#" + key + "\n" + e);
            return null;
        }
    }
}
