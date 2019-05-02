# Dumblang

Dumb ([Sili](https://github.com/DaveVoorhis/LDI/tree/master/Sili)<sup>2</sup>) is a Java-based, interpreted, general-purpose programming language. Syntactically and feature-wise it resembles JavaScript.

The grammar is defined in [JavaCC](https://javacc.org) with [JJTree](https://javacc.org/jjtree).

#### Building from source

1. Make sure you have installed [JDK 12 or above](https://www.oracle.com/technetwork/java/javase/downloads/index.html).
2. [Get Eclipse](https://www.eclipse.org)...
3. ...and [the JavaCC plugin](https://marketplace.eclipse.org/content/javacc-eclipse-plug).
4. Compile the `Dumb.jjt` file in the `parser` directory with JavaCC.


#### Running .dumb files

```
cd Dumb
java -classpath ./bin Dumb < [file_name].dumb
```

You can also use the `run` script placed in the `examples` directory

```
./run [file_name].dumb
```
