package dev.amrw.clovelang.integration;

import static org.assertj.core.api.Assertions.assertThat;

import dev.amrw.clovelang.tag.IntegrationTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@IntegrationTest
class FastSnapshotTest implements SnapshotTest {

  @DisplayName("Fast tests")
  @ParameterizedTest(name = "[{index}] {0}")
  @ValueSource(strings = {
      "builtins/random.clove",
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

  @Test
  @DisplayName("File test (builtins/file.clove)")
  void fileTest() throws IOException {
    snapshotTest("integration/builtins/file.clove");

    final var builtinsPath = "src/test/resources/integration/builtins/";
    final var createdFilePath = Path.of(builtinsPath + "file-test-output.txt");
    final var createdNestedFilePath = Path.of(
        builtinsPath + "file-test/file-test-output-nested.txt");
    final var snapshotPath = Path.of(createdFilePath + ".snapshot");
    final var nestedSnapshotPath = Path.of(builtinsPath + "file-test-output-nested.txt.snapshot");

    assertSameContents(createdFilePath, snapshotPath);
    assertSameContents(createdNestedFilePath, nestedSnapshotPath);

    if (!Files.deleteIfExists(createdFilePath)
        || !Files.deleteIfExists(createdNestedFilePath)
        || !Files.deleteIfExists(createdNestedFilePath.getParent())
    ) {
      throw new IOException(
          "Failed to delete the test output file. Has the file been created correctly?");
    }
  }

  @DisplayName("Known bugs")
  @ParameterizedTest(name = "[{index}] {0}")
  @ValueSource(strings = {
      "comments-illegal-characters.clove",
      "empty-script.clove",
      "strings-illegal-characters.clove",
  })
  void knownBugs(final String filePath) throws IOException {
    snapshotTest("integration/known-bugs/" + filePath);
  }

  private void assertSameContents(final Path path, final Path other) throws IOException {
    assertThat(Files.readString(path)).isEqualTo(Files.readString(other));
  }
}
