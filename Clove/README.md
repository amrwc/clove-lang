# Instructions

### Building from source

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
   Project → Build All (`cmd + B` on Mac, `ctrl + B` on Windows).

### Running .clove files

After setting up and building, do:

```PowerShell
cd Clove
java -classpath ./bin Clove < [file_name] [args]
```

You can also use the `run` scripts placed in the `examples` directory. Please note that the second example ('vigenere-cipher') requires 3 arguments; read the brief at the top of the file.

#### Mac

Terminal:

```PowerShell
./run [file_name] [args]
```

If you get `-bash: ./run: Permission denied` error, run:

```PowerShell
chmod 555 run
```

#### Windows

cmd/PowerShell:

```PowerShell
run [file_name] [args]
```

If you get `'java' is not recognized as an internal or external command` error, you need to add Java to your path. Follow the steps from [this short tutorial](https://stackoverflow.com/a/28451116).
