package dev.amrw.clovelang.namegenerator;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayNameGenerator;

public class SlowTestDisplayNameGenerator implements DisplayNameGenerator {

  public SlowTestDisplayNameGenerator() {
  }

  @Override
  public String generateDisplayNameForClass(final Class<?> testClass) {
    return "Slow tests";
  }

  @Override
  public String generateDisplayNameForNestedClass(final Class<?> nestedClass) {
    return nestedClass.getSimpleName();
  }

  @Override
  public String generateDisplayNameForMethod(final Class<?> testClass, final Method testMethod) {
    return generateDisplayNameForClass(testClass) + " > " + testMethod.getName();
  }
}
