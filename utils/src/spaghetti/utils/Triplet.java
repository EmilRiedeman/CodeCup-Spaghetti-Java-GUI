package spaghetti.utils;

import java.util.Objects;

public class Triplet<T1, T2, T3> {
    public T1 a;
    public T2 b;
    public T3 c;

    public Triplet(T1 a, T2 b, T3 c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Triplet<T1, T2, T3> copy() {
        return new Triplet<>(a, b, c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;

        if (!Objects.equals(a, triplet.a)) return false;
        if (!Objects.equals(b, triplet.b)) return false;
        return Objects.equals(c, triplet.c);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        result = 31 * result + (c != null ? c.hashCode() : 0);
        return result;
    }
}
