package dev.amrw.clovelang.integration;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FastSnapshotTest implements SnapshotTest {

  @DisplayName("Fast tests")
  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({
      "expressions/assignment-operator.clove",
      "expressions/binary-operator.clove",
      "expressions/logical-operator.clove",
      "expressions/unary-operator.clove",
      "functions/arrow-function.clove",
      "functions/function-definition.clove",
      "functions/nested-function-definition.clove",
      "functions/function-expression.clove",
      "literals/anonymous-object.clove",
      "literals/list.clove",
      "loops/for.clove",
      "loops/while.clove",
      "miscellaneous/block-statement.clove",
      "miscellaneous/constant-reassignment.clove",
      "miscellaneous/if-statement.clove",
      "miscellaneous/recursion.clove",
      "scope/block-const.clove",
      "scope/block-function.clove",
      "scope/block-let-declaration.clove",
      "scope/block-let-definition.clove",
      "scope/block-var-declaration.clove",
      "scope/block-var-definition.clove",
      "scope/function-definition.clove",
      "scope/function-expression.clove",
      "statements/declarations.clove",
  })
  void fastTests(final String filePath) throws IOException {
    snapshotTest("integration/" + filePath);
  }
}
