package net.sothatsit.evaluate;

import java.util.HashMap;
import java.util.Map;

public class PrefixTree<E> {

    private final Map<Character, E> values = new HashMap<>();
    private final Map<Character, PrefixTree<E>> subTrees = new HashMap<>();

    public E get(char ch) {
        return values.get(ch);
    }

    public PrefixTree<E> getSubTree(char ch) {
        return subTrees.get(ch);
    }

    public void add(String string, E value) {
        add(string, 0, value);
    }

    private void add(String string, int index, E value) {
        if(index >= string.length())
            throw new IllegalArgumentException("This tree already contains the prefix \"" + string + "\"");

        char ch = string.charAt(index);
        E existing = values.get(ch);

        if(existing != null) {
            throw new IllegalArgumentException("A value for the prefix \"" + string.substring(0, index + 1) + "\"" +
                                               " already exists in this tree");
        }

        PrefixTree<E> subTree = subTrees.get(ch);

        if(subTree != null) {
            subTree.add(string, index + 1, value);
            return;
        }

        if(index == string.length() - 1) {
            values.put(ch, value);
        } else {
            subTree = new PrefixTree<>();
            subTrees.put(ch, subTree);

            subTree.add(string, index + 1, value);
        }
    }
}
