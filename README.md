# Clove-lang

Clove is an interpreted, dynamically typed, general-purpose programming language implemented in Java, based on [Sili](https://github.com/DaveVoorhis/LDI/tree/master/Sili). The grammar defined in [JavaCC](https://javacc.org) with [JJTree](https://javacc.org/jjtree). It is primarily a procedural language, but has functional programming features, such as lambda expressions, and the programs can be composed in ways that utilise the declarative paradigm. The language allows for defining immutable primitive values, but compound structures, such as lists, can still be changed, but not reassigned; otherwise, all values are mutable.

Common statements and expressions, such as loops, if-statements, variable and function declarations/calls/invocations are conventional and similar to other languages, namely JavaScript. Variable names cannot start with a number and must be preceded by either _let_/_const_ keywords or their aliases.

More detailed specification of the language can be found [here](https://github.com/amrwc/Clove-lang/tree/master/Clove-lang-spec/Clove-lang.pdf).
