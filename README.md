# Quaz Compiler
The compiler for the Quaz JVM language.
Designed to be easy to use, and yet still as powerful as native java.

---

# Implemented Features

- Types
  - [x] Casting
  - [x] Classes
  - [x] int
  - [x] double
  - [x] float
  - [x] boolean
  - [x] char
  - [ ] byte
  - [ ] long
  - [ ] short
  - [ ] generics
- Functions
  - [x] Define
  - [x] Call
  - [x] Arguments + types (main automatically has String[])
  - [x] Return + type
  - [ ] Non static
  - [ ] Private
- Variables
  - [x] Implicit type
  - [x] Implicit value
  - [ ] Non local
  - [ ] static
  - [ ] private
- Control Statements
  - [x] If / else if / else
  - [x] Basic for (e.g. `for(var i = 0; i < 5; i = i + 1)`)
  - [x] while
  
Also implemented are binary operators `+, -, /, *, %, &, |, ^, &&, ||, ==, !=, ===, !==, <, <=, >, >=`

These binary operators offer impicit casting between primative types. I.e. `1.5D / 2`

Unary operations `-, +, !, ~` are planned, as when as binary operation `**`.
Operator Overloading will also be implemented, and is currently supported for the == and != operator, which calls the object's `equals` method. Use `===` and `!==` operators for the java style of equal operators.

# Builtin Functions
Found within [Quaz STL](https://github.com/QcO-dev/quaz-stl)
- println  
  Alias for `System.out#println(args)` Will have other ways to call in further development.
- input  
  Executes `System.out#print(args)` and then `Scanner#nextLine()` and returns this value.
