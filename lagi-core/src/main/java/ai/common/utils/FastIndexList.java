package ai.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FastIndexList<E> extends ArrayList<E> {
    private final Map<E, Integer> indexMap;

    public FastIndexList() {
        super();
        indexMap = new HashMap<>();
    }

    @Override
    public boolean add(E element) {
        indexMap.put(element, size());
        return super.add(element);
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        rebuildIndexMap();
    }

    @Override
    public E set(int index, E element) {
        E oldElement = super.set(index, element);
        rebuildIndexMap();
        return oldElement;
    }

    @Override
    public E remove(int index) {
        E removedElement = super.remove(index);
        rebuildIndexMap();
        return removedElement;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = super.remove(o);
        if (result) {
            rebuildIndexMap();
        }
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        indexMap.clear();
    }

    @Override
    public int indexOf(Object o) {
        return indexMap.getOrDefault(o, -1);
    }

    private void rebuildIndexMap() {
        indexMap.clear();
        for (int i = 0; i < size(); i++) {
            indexMap.put(get(i), i);
        }
    }
}
