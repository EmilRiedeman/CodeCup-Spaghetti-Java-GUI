package spaghetti.utils;

import java.io.Serializable;
import java.util.Objects;

public class Pair<T1, T2> implements Serializable {
    public T1 a;
    public T2 b;

    public Pair(T1 a, T2 b) {
        this.a = a;
        this.b = b;
    }

    public Pair<T1, T2> copy() {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!Objects.equals(a, pair.a)) return false;
        return Objects.equals(b, pair.b);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }
}
