# Clove-lang

Clove is an interpreted, dynamically typed, general-purpose programming language implemented in Java, based on [Sili](https://github.com/DaveVoorhis/LDI/tree/master/Sili). The grammar defined in [JavaCC](https://javacc.org) with [JJTree](https://javacc.org/jjtree). It is primarily a procedural language, but has functional programming features, such as lambda expressions, and the programs can be composed in ways that utilise the declarative paradigm. The language allows for defining immutable primitive values, but compound structures, such as lists, can still be changed, but not reassigned; otherwise, all values are mutable.

Common statements and expressions, such as loops, if-statements, variable and function declarations/calls/invocations are conventional and similar to other languages, namely JavaScript. Variable names cannot start with a number and must be preceded by either _let_/_const_ keywords or their aliases.

More detailed specification of the language can be found [here](https://github.com/amrwc/Clove-lang/tree/master/Clove-lang-spec/Clove-lang.pdf).

## Feature ideas

- Expressions
  - [ ] ternary operator,
  - [ ] null support
    - `if (result == null)…`,
  - [ ] shorthand modulo reassignment
    - `%=`,
  - [ ] root function (n<sup>th</sup> root)
    - `sqrt()`,
    - `root(9, 2)` (root of 9, degree 2)
      - [identities and properties](https://en.wikipedia.org/wiki/Nth_root#Identities_and_properties).
- Statements
  - [ ] switch,
  - [ ] do-while,
  - [ ] optional arguments in for-loop
    - `for(;;)`,
  - [ ] break,
  - [ ] continue,
  - [ ] chain of declarations/definitions
    - `const v1, v2, v3 = 10, v4 = 'word'`,
  - [ ] global variable definition from any scope
    - `global result = 10`,
    - it would be available anywhere until the end of the program, even in the outside scope,
    - why: in a deep scope when a condition is met, define a global variable that will be accessible from anywhere, so it doesn't have to be declared beforehand,
  - [ ] Java code literals
    - Java code wrapped in backticks (or other symbols) that is run in place,
    - `` `System.out.println(“bob”);` ``,
    - Compiler API,
    - how would it interact with the already defined code?
- Functions
  - [ ] default parameter values
    - `function(param1 = 'default string', param2 = 10) {}`.
- Prototype functions
  - [x] size/length,
  - [x] keys
        — returns a ValueList of object’s keys,
  - [x] shift,
  - [x] push, pop
        — imitate stack with lists/arrays.
- Types
  - [ ] ValueTuple,
  - [ ] break ValueRational into ValueFloat and ValueDouble.

## Known bugs

- [ ] Variables defined during recursion
  - Variables defined inside a FunctionInvocation are being found deeper in the recursion, therefore, they throw ‘variable already defined’ error.
  - The workaround is to modify variables defined outside of the outermost FunctionDefinition – before the recursion starts.
- [ ] Higher-order functions
  - When an anonymous function is passed to any FunctionDefinition, the argument (callback function) works correctly only on the first occurrence. Subsequent calls to the first argument of the function will result in calling the argument first used, regardless of the new declaration.

  ```JavaScript
  function repeat(n, action) {
    for (let i = 0; i < n; i++)
      action(i)
  }

  const list = []
  repeat(8, (n) => { // This anonymous function...
    list->append(n)
  })
  log(list) // [0, 1, 2, 3, 4, 5, 6, 7]

  const str = "text"
  repeat(3, (x) => { // ...is still remembered here.
    log(x, str) // Never done.
  })
  log(list) // [0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2]
  ```

- [ ] Return statements in a nested block
  - A function's return statement cannot be inside of a different node. I.e. it must be in the function's body, not in an if-statement, not in a loop, etc.
  - Why: return statement is not defined as a statement, but is optionally expected inside of function definition.
  - To solve it, return statement would have to be defined as a sole statement and accounted for inside of ASTFunctionDefinition node. I.e. the whole AST inside of function definition would have to be searched for a return statement and evaluate it if it exists.
  ```JavaScript
  function max(x, y) {
    if (x > y) return x; // Error
    else if (x < y) return y;
    else return 0;
  }
  ```
