# Clove-lang

## Prerequisites

- JDK 12 or above

  ```console
  sdk install java 17.0.1.12.1-amzn
  ```

### Optional

- [JavaCC](https://github.com/javacc/javacc) – for compiling abstract syntax
  tree manually.

  ```console
  brew install javacc
  ```

## Project setup and compiling

### Gradle

```console
cd Clove
./gradlew javacc
./gradlew build
```

Test it:

```console
./gradlew run < tests/test01.clove
```

Clean up:

```console
./gradlew clean
```

### Manual

```console
cd Clove/src/main/java/dev/amrw/clovelang/parser
jjtree -JJTREE_OUTPUT_DIRECTORY=ast Clove.jjt

cd ast
javacc Clove.jj

cd ../../../../..  # Clove/src/main/java
javac dev/amrw/clovelang/CloveMain.java
```

Test it:

```console
cd Clove
java --class-path src/main/java dev.amrw.clovelang.CloveMain < tests/test01.clove
```

Clean up:

```console
find src/main/java -name "*.class" -type f -delete
```

### Eclipse IDE

```console
brew install --cask eclipse-java
```

1. Open the project:
   1. Go to File → Import… → General → Existing projects into workspace.
   1. In the `Select root directory` field point to the parent `Clove` folder.
   1. Finish.
1. Get
   [JavaCC plugin](https://marketplace.eclipse.org/content/javacc-eclipse-plug).
1. Compile the `Clove.jjt` file in the `src/parser` directory with JavaCC:
   1. Right-click the `Clove.jjt` file and press 'Compile with javacc'.
   1. If the above button is grey, open the file first and try again.
1. Make sure the project is built, if it didn't happen automatically:
   1. Project → Build All (`cmd + B` on Mac, `ctrl + B` on Windows).

## Running `.clove` files (OUTDATED, see above)

```console
cd Clove
java -classpath ./bin Clove < [file_name] [args]
```

You can also use the `run` scripts placed in the `examples` directory. Please
note that the second example (`vigenere-cipher`) requires 3 arguments; read the
brief at the top of the file.

### Mac

```console
./run [file_name] [args]
```

### Windows

```console
run [file_name] [args]
```

If you get `'java' is not recognized as an internal or external command` error,
you need to add Java to your path. Follow the steps from
[this short tutorial](https://stackoverflow.com/a/28451116).
