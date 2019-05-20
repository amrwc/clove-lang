# Building from source

1. Make sure you have installed [JDK 12 or above](https://www.oracle.com/technetwork/java/javase/downloads/index.html).
1. [Get Eclipse](https://www.eclipse.org)…
1. …and [the JavaCC plugin](https://marketplace.eclipse.org/content/javacc-eclipse-plug).
1. Open the project:
   1. Go to File → Import… → General → Existing projects into workspace.
   1. In the `Select root directory` field point inside the parent `Clove` folder.
   1. Finish.
1. Compile the `Clove.jjt` file in the `src/parser` directory with JavaCC:
   1. Right-click the `Clove.jjt` file and press 'Compile with javacc'.
   1. If the above button is grey, open the file first and try again.
1. Make sure the project is built if it didn't happen automatically:
   Project → Build All (`cmd + B` on Mac)
