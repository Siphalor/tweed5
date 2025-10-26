package de.siphalor.tweed5.weaver.pojo.impl.weaving;

public class PojoWeavingException extends RuntimeException {
  public PojoWeavingException(String message, Throwable cause) {
    super(message, cause);
  }

  public PojoWeavingException(String message) {
    super(message);
  }
}
