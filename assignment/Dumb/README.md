# Dumb-lang

Dumb ([Sili](https://github.com/DaveVoorhis/LDI/tree/master/Sili)<sup>2</sup>) is a Java-based, interpreted, general-purpose programming language. Syntactically and feature-wise it resembles JavaScript.

The grammar is defined in [JavaCC](https://javacc.org) with [JJTree](https://javacc.org/jjtree).

#### Building from source

1. Make sure you have installed [JDK 12 or above](https://www.oracle.com/technetwork/java/javase/downloads/index.html).
1. [Get Eclipse](https://www.eclipse.org)…
1. …and [the JavaCC plugin](https://marketplace.eclipse.org/content/javacc-eclipse-plug).
1. Open the project:
   1. Go to File → Import… → General → Existing projects into workspace.
   1. In the `Select root directory` field point inside the parent `Dumb` folder.
   1. Finish.
1. Compile the `Dumb.jjt` file in the `src/parser` directory with JavaCC:
   1. Right-click the `Dumb.jjt` file and press 'Compile with javacc'.
   1. If the above button is grey, open the file first and try again.
1. Make sure the project is built if it didn't happen automatically:
   Project → Build All (`cmd + B` on Mac)

#### Running .dumb files

```
cd Dumb
java -classpath ./bin Dumb < [file_name].dumb [args]
```

You can also use the `run` script placed in the `examples` directory

```
./run [file_name].dumb [args]
```
