About
-----
A library to efficiently parse, optimise, compile and evaluate string expressions. 

**Note:** The optimisation steps of this library do not ensure complete floating 
point equivalence between the original and optimised expressions, as
the order of evaluation is not maintained.

Compilation
-----------
Expressions are compiled into java bytecode to be loaded into the JVM.
This requires more testing but in simple cases has been able to improve 
the performance of simple functions by over 100x.