package com.terraforged.core.concurrent.pool;

import com.terraforged.core.concurrent.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ArrayPool<T> {

    private volatile long time = 0;
    private volatile long misses = 0;

    private final int capacity;
    private final IntFunction<T[]> constructor;
    private final List<ArrayPool.Item<T>> pool;

    public ArrayPool(int size, IntFunction<T[]> constructor) {
        this.capacity = size;
        this.constructor = constructor;
        this.pool = new ArrayList<>(size);
    }

    public Resource<T[]> get(int size) {
        synchronized (pool) {
            if (pool.size() > 0) {
                ArrayPool.Item<T> resource = pool.get(pool.size() - 1);
                if (resource.get().length >= size) {
                    return resource.retain();
                }
            }
            misses++;
        }
        return new ArrayPool.Item<>(constructor.apply(size), this);
    }

    public void stats() {
        long now = System.currentTimeMillis();
        if (now - time > 1000) {
            time = now;
            long misses = getMisses();
            if (misses > 0) {
                this.misses = capacity;
                System.out.println("Misses=" + misses);
            }
        }
    }

    public long getMisses() {
        // a 'miss' occurs when we have to create a new instance because the pool is being fully utilized
        // the pool is lazily filled so we can expect the miss count to be at least equal to the pool capacity
        return Math.max(0, misses - capacity);
    }

    public int size() {
        synchronized (pool) {
            return pool.size();
        }
    }

    private boolean restore(ArrayPool.Item<T> item) {
        synchronized (pool) {
            if (pool.size() < capacity) {
                pool.add(item);
                return true;
            }
        }
        return false;
    }

    public static class Item<T> implements Resource<T[]> {

        private final T[] value;
        private final ArrayPool<T> pool;

        private boolean released = false;

        private Item(T[] value, ArrayPool<T> pool) {
            this.value = value;
            this.pool = pool;
        }

        @Override
        public T[] get() {
            return value;
        }

        @Override
        public boolean isOpen() {
            return !released;
        }

        @Override
        public void close() {
            if (!released) {
                released = true;
                released = pool.restore(this);
            }
        }

        private ArrayPool.Item<T> retain() {
            released = false;
            return this;
        }
    }

    public static <T> ArrayPool<T> of(int size, IntFunction<T[]> constructor) {
        return new ArrayPool<>(size, constructor);
    }

    public static <T> ArrayPool<T> of(int size, Supplier<T> supplier, IntFunction<T[]> constructor) {
        return new ArrayPool<>(size, new ArrayConstructor<>(supplier, constructor));
    }

    private static class ArrayConstructor<T> implements IntFunction<T[]> {

        private final Supplier<T> element;
        private final IntFunction<T[]> array;

        private ArrayConstructor(Supplier<T> element, IntFunction<T[]> array) {
            this.element = element;
            this.array = array;
        }

        @Override
        public T[] apply(int size) {
            T[] t = array.apply(size);
            for (int i = 0; i < t.length; i++) {
                t[i] = element.get();
            }
            return t;
        }
    }
}