package net.sothatsit.evaluate.compiler;

import net.sothatsit.evaluate.tree.function.Function;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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

    public CompiledExpression load(String name, List<Function> fields, byte[] bytes) {
        Class<?> loaded = defineClass(name, bytes, 0, bytes.length);
        Class<? extends CompiledExpression> clazz = loaded.asSubclass(CompiledExpression.class);

        try {
            Class<?>[] arguments = new Class<?>[fields.size()];
            Arrays.fill(arguments, Function.class);

            return clazz.getConstructor(arguments).newInstance(fields.toArray());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Compilation failed", e);
        }
    }
}
