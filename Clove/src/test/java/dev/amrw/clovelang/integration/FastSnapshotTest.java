package dev.amrw.clovelang.integration;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FastSnapshotTest implements SnapshotTest {

  @DisplayName("Fast tests")
  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({
      "functions/function-definition.clove",
      "functions/nested-function-definition.clove",
      "functions/function-expression.clove",
      "loops/for.clove",
      "loops/while.clove",
      "miscellaneous/block-statement.clove",
      "miscellaneous/if-statement.clove",
      "miscellaneous/recursion.clove",
      "miscellaneous/simple-expressions.clove",
      "statements/declarations.clove",
  })
  void fastTests(final String filePath) throws IOException {
    snapshotTest("integration/" + filePath);
  }
}
