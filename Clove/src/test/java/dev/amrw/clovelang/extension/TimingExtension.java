package dev.amrw.clovelang.extension;

import java.util.logging.Logger;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * https://github.com/junit-team/junit5/blob/main/documentation/src/test/java/example/timing/TimingExtension.java
 */
public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

  private static final Logger logger = Logger.getLogger(TimingExtension.class.getName());
  private static final String START_TIME = "start time";

  @Override
  public void beforeTestExecution(final ExtensionContext context) {
    getStore(context).put(START_TIME, System.currentTimeMillis());
  }

  @Override
  public void afterTestExecution(final ExtensionContext context) {
    final var testMethod = context.getRequiredTestMethod();
    final var startTime = getStore(context).remove(START_TIME, long.class);
    final var duration = System.currentTimeMillis() - startTime;

    logger.info(() ->
        String.format("Method [%s] took %s ms.", testMethod.getName(), duration));
  }

  private Store getStore(final ExtensionContext context) {
    final var namespace = Namespace.create(getClass(), context.getRequiredTestMethod());
    return context.getStore(namespace);
  }
}
