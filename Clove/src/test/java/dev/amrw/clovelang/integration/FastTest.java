package dev.amrw.clovelang.integration;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FastTest implements SnapshotTest {

  @DisplayName("Fast tests")
  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({
      "functions/function-definition.clove",
      "functions/nested-function-definition.clove",
      "functions/function-expression.clove",
      "loops/for-loop.clove",
      "if-else-statement.clove",
      "block-statement.clove",
      "unary-expressions.clove",
  })
  void fastTests(final String filePath) throws IOException {
    snapshotTest("integration/" + filePath);
  }
}
