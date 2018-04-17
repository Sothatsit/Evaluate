package net.sothatsit.evaluate.compiler;

import java.util.ArrayList;
import java.util.List;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class Locals {

    private final MethodCompiler mc;

    private final List<Local> locals;
    private final List<Local> variables;

    private final List<Local> referenceTemporaries;
    private final List<Local> doubleTemporaries;

    public Locals(MethodCompiler mc) {
        this.mc = mc;
        this.locals = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.referenceTemporaries = new ArrayList<>();
        this.doubleTemporaries = new ArrayList<>();

        newReferenceVariable(); // Add the "this" local
    }

    public int getVariableCount() {
        return variables.size();
    }

    public int getReferenceTemporaryCount() {
        return referenceTemporaries.size();
    }

    public int getDoubleTemporaryCount() {
        return doubleTemporaries.size();
    }

    /**
     * Find the index of {@param search} on this method's local stack.
     */
    private int findBytecodeIndex(Local search) {
        int bytecodeIndex = 0;

        for(Local local : locals) {
            if(local == search)
                return bytecodeIndex;

            bytecodeIndex += local.width;
        }

        throw new IllegalArgumentException("Unable to find local " + search);
    }

    private void loadLocal(Local local) {
        local.load(findBytecodeIndex(local));
    }

    private void storeLocal(Local local) {
        local.store(findBytecodeIndex(local));
    }


    public void loadVariable(int index) {
        loadLocal(variables.get(index));
    }

    public void storeVariable(int index) {
        storeLocal(variables.get(index));
    }

    private int newVariable(Local local) {
        variables.add(local);
        return variables.size() - 1;
    }

    public int newReferenceVariable() {
        return newVariable(newReferenceLocal());
    }

    public int newDoubleVariable() {
        return newVariable(newDoubleLocal());
    }

    public void loadReferenceTemporary(int index) {
        loadLocal(referenceTemporaries.get(index));
    }

    public void storeReferenceTemporary(int index) {
        if(index < referenceTemporaries.size()) {
            if(index != referenceTemporaries.size()) {
                throw new IllegalArgumentException(
                        "Temporaries must be created in order. " +
                        "Cannot store in reference temporary " + index +
                        " when there only exist " + referenceTemporaries.size() + " reference temporaries. " +
                        "Must instead store in reference temporary " + referenceTemporaries.size());
            }

            referenceTemporaries.add(newReferenceLocal());
        }

        storeLocal(referenceTemporaries.get(index));
    }

    public void loadDoubleTemporary(int index) {
        loadLocal(doubleTemporaries.get(index));
    }

    public void storeDoubleTemporary(int index) {
        if(index < doubleTemporaries.size()) {
            if(index != doubleTemporaries.size()) {
                throw new IllegalArgumentException(
                        "Temporaries must be created in order. " +
                                "Cannot store in double temporary " + index +
                                " when there only exist " + doubleTemporaries.size() + " double temporaries. " +
                                "Must instead store in double temporary " + doubleTemporaries.size());
            }

            doubleTemporaries.add(newDoubleLocal());
        }

        storeLocal(doubleTemporaries.get(index));
    }

    private Local newReferenceLocal() {
        Local local = new Local(1) {
            @Override
            public void store(int bytecodeIndex) {
                mc.varInsn(ASTORE, bytecodeIndex);
            }

            @Override
            public void load(int bytecodeIndex) {
                mc.varInsn(ALOAD, bytecodeIndex);
            }

            @Override
            public String toString() {
                return "{Reference Local}";
            }
        };

        locals.add(local);

        return local;
    }

    private Local newDoubleLocal() {
        Local local = new Local(2) {
            @Override
            public void store(int bytecodeIndex) {
                mc.varInsn(DSTORE, bytecodeIndex);
            }

            @Override
            public void load(int bytecodeIndex) {
                mc.varInsn(DLOAD, bytecodeIndex);
            }

            @Override
            public String toString() {
                return "{Double Local}";
            }
        };

        locals.add(local);

        return local;
    }

    /**
     * A local variable slot.
     */
    private abstract class Local {

        public final int width;

        public Local(int width) {
            this.width = width;
        }

        public abstract void store(int bytecodeIndex);

        public abstract void load(int bytecodeIndex);
    }
}
