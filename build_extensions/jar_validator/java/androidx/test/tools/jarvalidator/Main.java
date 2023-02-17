package androidx.test.tools.jarvalidator;

public class Main {
  private Main() {}

  public static void main(String[] args) {
    if (!JarValidatorKt.validateJar(args)) {
      System.exit(1);
    }
  }
}
