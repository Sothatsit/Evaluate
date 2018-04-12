About
-----
A library to efficiently parse, optimise, compile and evaluate arbitrary 
string expressions at runtime. 

**Note:** The optimisation steps of this library do not ensure complete 
floating point equivalence between the original and optimised expressions, 
as the order of evaluation is not maintained.

Compilation
-----------
Expressions are compiled into java bytecode and loaded into the JVM.
In simple cases this has been able to improve performance of simple 
functions by over 100x, although this requires more rigorous testing.