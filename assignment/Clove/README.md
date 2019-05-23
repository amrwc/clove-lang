# Clove-lang

Clove ([Sili](https://github.com/DaveVoorhis/LDI/tree/master/Sili)<sup>2</sup>) is a Java-based, interpreted, general-purpose programming language. Syntactically and feature-wise it resembles JavaScript.

The grammar is defined in [JavaCC](https://javacc.org) with [JJTree](https://javacc.org/jjtree).

## Running .clove files

After setting up and building, do:

```
cd Clove
java -classpath ./bin Clove < [file_name].clove [args]
```

You can also use the `run` scripts placed in the `examples` directory. Please note that the second example ('vigenere-cipher') requires 3 arguments; read the brief at the top fo the file.

#### Mac

```
./run [file_name].clove [args]
```

#### Windows

```
run [file_name].clove [args]
```

On Windows 10 if you get `'java' is not recognized as an internal or external command` error, you need to add Java to your path. Follow the steps from [this short tutorial](https://stackoverflow.com/a/28451116).
