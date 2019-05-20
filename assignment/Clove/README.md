# Clove-lang

Clove ([Sili](https://github.com/DaveVoorhis/LDI/tree/master/Sili)<sup>2</sup>) is a Java-based, interpreted, general-purpose programming language. Syntactically and feature-wise it resembles JavaScript.

The grammar is defined in [JavaCC](https://javacc.org) with [JJTree](https://javacc.org/jjtree).

#### Running .clove files

After setting up and building, do:

```
cd Clove
java -classpath ./bin Clove < [file_name].clove [args]
```

You can also use the `run` script placed in the `examples` directory

```
./run [file_name].clove [args]
```
