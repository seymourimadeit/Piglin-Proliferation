package tallestred.piglinproliferation.util;

import net.minecraft.core.Holder;

import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class LazyLoadedArray<T> {
    private Holder<T>[] unloadedObjects;
    private T[] loadedObjects;
    private int length = 0;

    public LazyLoadedArray() {
    }

    public LazyLoadedArray(int initialLength) {
        this.unloadedObjects = new Holder[initialLength];
        this.length = initialLength;
    }

    public int length() {
        return this.length;
    }

    public void add(Holder<T> toAdd) {
        boolean added = false;
        for (int i = 0; i < unloadedObjects.length; i++) {
            if (unloadedObjects[i] == null) {
                unloadedObjects[i] = toAdd;
                added = true;
            }
        }
        if (!added) {
            Holder<T>[] expanded = new Holder[unloadedObjects.length + 1];
            System.arraycopy(unloadedObjects, 0, expanded, 0, unloadedObjects.length);
            expanded[expanded.length - 1] = toAdd;
            this.unloadedObjects = expanded;
            this.length++;
        }
    }

    public T[] values() {
        if (loadedObjects == null) {
            loadedObjects = (T[]) new Object[unloadedObjects.length];
            for (int i = 0; i < loadedObjects.length; i++)
                loadedObjects[i] = unloadedObjects[i].value();
            unloadedObjects = null;
        }
        return loadedObjects;
    }

    public T findFirstMatch(Predicate<T> predicate) {
        for (T element : values())
            if (predicate.test(element))
                return element;
        return null;
    }

    public boolean allMatch(Predicate<T> predicate) {
        for (T element : values())
            if (predicate.test(element))
                return true;
        return false;
    }
}
