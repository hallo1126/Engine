/*
 *   
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.core.concurrent.pool;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.cache.SafeCloseable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ObjectPool<T> {

    private transient long misses = 0L;

    private final int capacity;
    private final List<Item<T>> pool;
    private final Supplier<? extends T> supplier;

    public ObjectPool(int size, Supplier<? extends T> supplier) {
        this.capacity = size;
        this.pool = new ArrayList<>(size);
        this.supplier = supplier;
    }

    public Resource<T> get() {
        synchronized (pool) {
            if (pool.size() > 0) {
                return pool.remove(pool.size() - 1).retain();
            }
            misses++;
        }
        return new Item<>(supplier.get(), this);
    }

    public long getMisses() {
        return Math.max(0, misses - capacity);
    }

    public int size() {
        synchronized (pool) {
            return pool.size();
        }
    }

    private boolean restore(Item<T> item) {
        synchronized (pool) {
            int size = pool.size();
            if (size < capacity) {
                pool.add(item);
                return true;
            }
        }
        return false;
    }

    public static class Item<T> implements Resource<T> {

        private final T value;
        private final ObjectPool<T> pool;

        private boolean released = false;

        private Item(T value, ObjectPool<T> pool) {
            this.value = value;
            this.pool = pool;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public boolean isOpen() {
            return !released;
        }

        @Override
        public void close() {
            if (value instanceof SafeCloseable) {
                ((SafeCloseable) value).close();
            }
            if (!released) {
                released = true;
                released = pool.restore(this);
            }
        }

        private Item<T> retain() {
            released = false;
            return this;
        }
    }
}
