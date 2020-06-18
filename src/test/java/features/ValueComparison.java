package features;

public class ValueComparison<T> {

    private final T left;
    private final T right;

    public ValueComparison(T left, T right) {
        this.left = left;
        this.right = right;
    }

    public Boolean valuesAreEqual() {
        return left.equals(right);
    }

    public T getLeft() {
        return left;
    }

    public T getRight() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("left: %s\nright:%s", left.toString(), right.toString());
    }
}
