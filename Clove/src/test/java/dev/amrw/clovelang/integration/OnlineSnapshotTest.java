package dev.amrw.clovelang.integration;

import dev.amrw.clovelang.tag.OnlineTest;
import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@OnlineTest
public class OnlineSnapshotTest implements SnapshotTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "builtins/http.clove",
  })
  void onlineTests(final String filePath) throws IOException {
    snapshotTest("integration/" + filePath);
  }
}
