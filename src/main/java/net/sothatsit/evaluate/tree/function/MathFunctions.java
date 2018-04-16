package net.sothatsit.evaluate.tree.function;

import net.sothatsit.evaluate.compiler.MethodCompiler;
import net.sothatsit.evaluate.tree.function.operator.*;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class MathFunctions {

    public static Function[] get() {
        return new Function[] {
                Add.fn, Subtract.fn, Multiply.fn, Divide.fn,

                sin, cos, tan,
                sinh, cosh, tanh,
                asin, acos, atan, atan2,
                csc, sec, cot,

                sqrt, Power.fn,
                ln, log2, log10,
                sign, abs, floor, ceil,
                round, roundto
        };
    }

    public static final CompilableOneArgFunction sin = new CompilableOneArgFunction("sin") {
        public double evaluate(double arg) {
            return Math.sin(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "sin", 1);
        }
    };

    public static final CompilableOneArgFunction cos = new CompilableOneArgFunction("cos") {
        public double evaluate(double arg) {
            return Math.cos(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "cos", 1);
        }
    };

    public static final CompilableOneArgFunction tan = new CompilableOneArgFunction("tan") {
        public double evaluate(double arg) {
            return Math.sin(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "tan", 1);
        }
    };

    public static final CompilableOneArgFunction sinh = new CompilableOneArgFunction("sinh") {
        public double evaluate(double arg) {
            return Math.sinh(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "sinh", 1);
        }
    };

    public static final CompilableOneArgFunction cosh = new CompilableOneArgFunction("cosh") {
        public double evaluate(double arg) {
            return Math.cosh(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "cosh", 1);
        }
    };

    public static final CompilableOneArgFunction tanh = new CompilableOneArgFunction("tanh") {
        public double evaluate(double arg) {
            return Math.tanh(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "tanh", 1);
        }
    };

    public static final CompilableOneArgFunction asin = new CompilableOneArgFunction("asin", "arcsin") {
        public double evaluate(double arg) {
            return Math.asin(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "asin", 1);
        }
    };

    public static final CompilableOneArgFunction acos = new CompilableOneArgFunction("acos", "arccos") {
        public double evaluate(double arg) {
            return Math.acos(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "acos", 1);
        }
    };

    public static final CompilableOneArgFunction atan = new CompilableOneArgFunction("atan", "arctan") {
        public double evaluate(double arg) {
            return Math.atan(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "atan", 1);
        }
    };

    public static final CompilableTwoArgFunction atan2 = new CompilableTwoArgFunction("atan2", "arctan2") {
        public double evaluate(double y, double x) {
            return Math.atan2(y, x);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "atan2", 2);
        }
    };

    public static final CompilableOneArgFunction csc = new CompilableOneArgFunction("csc", "cosec") {
        public double evaluate(double arg) {
            return 1.0 / Math.sin(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "sin", 1);
            mc.loadConstant(1.0d);
            mc.moveOneUnder();

            mc.divide();
        }
    };

    public static final CompilableOneArgFunction sec = new CompilableOneArgFunction("sec") {
        public double evaluate(double arg) {
            return 1.0 / Math.cos(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "cos", 1);
            mc.loadConstant(1.0d);
            mc.moveOneUnder();

            mc.divide();
        }
    };

    public static final CompilableOneArgFunction cot = new CompilableOneArgFunction("cot") {
        public double evaluate(double arg) {
            return 1.0 / Math.tan(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "tan", 1);
            mc.loadConstant(1.0d);
            mc.moveOneUnder();

            mc.divide();
        }
    };

    public static final CompilableOneArgFunction ln = new CompilableOneArgFunction("ln", "log") {
        public double evaluate(double arg) {
            return Math.log(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "log", 1);
        }
    };

    public static final CompilableOneArgFunction log2 = new CompilableOneArgFunction("log2") {
        private final double LOG2 = Math.log(2);

        public double evaluate(double arg) {
            return Math.log(arg) / LOG2;
        }

        public void compile(MethodCompiler mc) {
            mc.perform(ln);
            mc.loadConstant(LOG2);
            mc.divide();
        }
    };

    public static final CompilableOneArgFunction log10 = new CompilableOneArgFunction("log10") {
        private final double LOG10 = Math.log(10);

        public double evaluate(double arg) {
            return Math.log(arg) / LOG10;
        }

        public void compile(MethodCompiler mc) {
            mc.perform(ln);
            mc.loadConstant(LOG10);
            mc.divide();
        }
    };

    public static final CompilableOneArgFunction sqrt = new CompilableOneArgFunction("sqrt") {
        public double evaluate(double arg) {
            return Math.sqrt(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "sqrt", 1);
        }
    };

    public static final CompilableOneArgFunction sign = new CompilableOneArgFunction("sign", "signum") {
        public double evaluate(double arg) {
            return Math.signum(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "signum", 1);
        }
    };

    public static final CompilableOneArgFunction abs = new CompilableOneArgFunction("abs") {
        public double evaluate(double arg) {
            return Math.abs(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "abs", 1);
        }
    };

    public static final CompilableOneArgFunction floor = new CompilableOneArgFunction("floor") {
        public double evaluate(double arg) {
            return Math.floor(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "floor", 1);
        }
    };

    public static final CompilableOneArgFunction ceil = new CompilableOneArgFunction("ceil") {
        public double evaluate(double arg) {
            return Math.ceil(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "ceil", 1);
        }
    };

    public static final CompilableOneArgFunction round = new CompilableOneArgFunction("round") {
        public double evaluate(double arg) {
            return Math.round(arg);
        }

        public void compile(MethodCompiler mc) {
            mc.staticMethod(Math.class, "round", 1);
        }
    };

    public static final CompilableTwoArgFunction roundto = new CompilableTwoArgFunction("roundto") {
        public double evaluate(double arg, double decimalPlaces) {
            double a = Math.pow(10, decimalPlaces);

            return Math.round(arg * a) / a;
        }

        public void compile(MethodCompiler mc) {
            mc.loadConstant(10);
            mc.insn(DUP_X1);
            mc.pop();
            mc.perform(Power.fn);

            mc.duplicate();
            mc.storeTemp(0);

            mc.multiply();
            mc.perform(round);

            mc.loadTemp(0);
            mc.divide();
        }
    };
}
