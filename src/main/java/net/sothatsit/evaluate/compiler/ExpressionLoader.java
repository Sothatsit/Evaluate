package net.sothatsit.evaluate.compiler;

import net.sothatsit.evaluate.tree.function.Function;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public void write(byte[] bytes) throws IOException {
        File output = new File("/Users/159503/Desktop/compiled.class");

        FileOutputStream fos = new FileOutputStream(output);

        fos.write(bytes);
        fos.flush();
        fos.close();
    }

    public CompiledExpression load(String name,
                                   List<Function> fields,
                                   int inputCount,
                                   int outputCount,
                                   byte[] bytes) {

        try {
            write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Class<?> loaded = defineClass(name, bytes, 0, bytes.length);
        Class<? extends CompiledExpression> clazz = loaded.asSubclass(CompiledExpression.class);

        try {
            Class<?>[] argumentTypes = new Class<?>[2 + fields.size()];

            argumentTypes[0] = int.class;
            argumentTypes[1] = int.class;
            for(int index = 0; index < fields.size(); ++index) {
                argumentTypes[2 + index] = ExpressionCompiler.getFunctionReferenceClass(fields.get(index));
            }

            Object[] arguments = new Object[2 + fields.size()];
            arguments[0] = inputCount;
            arguments[1] = outputCount;
            System.arraycopy(fields.toArray(), 0, arguments, 2, fields.size());

            return clazz.getConstructor(argumentTypes).newInstance(arguments);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Compilation failed", e);
        }
    }
}
