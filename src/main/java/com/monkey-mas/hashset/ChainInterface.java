public interface ChainInterface<T> {
	public abstract boolean add(T data);
	public abstract boolean remove(T data);
	public abstract boolean contains(T data);
}