package net.sothatsit.evaluate.compiler;

import net.sothatsit.evaluate.tree.Expression;
import net.sothatsit.evaluate.tree.function.Function;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ExpressionLoader extends ClassLoader {

    private static final String PACKAGE = "net.sothatsit.evaluate.compiled";
    private static final String CLASS_NAME = "CompiledExpression";

    private long counter = 0;

    public String getNextName() {
        do {
            counter += 1;
        } while(findLoadedClass(PACKAGE + "." + CLASS_NAME + "$" + counter) != null);

        return PACKAGE + "." + CLASS_NAME + "$" + counter;
    }

    public CompiledExpression load(String name,
                                   Expression expression,
                                   int inputCount,
                                   List<Function> fields,
                                   byte[] bytes) {

        Class<?> loaded = defineClass(name, bytes, 0, bytes.length);
        Class<? extends CompiledExpression> clazz = loaded.asSubclass(CompiledExpression.class);

        try {
            Class<?>[] argumentTypes = new Class<?>[2 + fields.size()];

            argumentTypes[0] = Expression.class;
            argumentTypes[1] = int.class;
            for(int index = 0; index < fields.size(); ++index) {
                argumentTypes[2 + index] = ExpressionCompiler.getFunctionReferenceClass(fields.get(index));
            }

            Object[] arguments = new Object[2 + fields.size()];
            arguments[0] = expression;
            arguments[1] = inputCount;
            System.arraycopy(fields.toArray(), 0, arguments, 2, fields.size());

            return clazz.getConstructor(argumentTypes).newInstance(arguments);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Compilation failed", e);
        }
    }
}
