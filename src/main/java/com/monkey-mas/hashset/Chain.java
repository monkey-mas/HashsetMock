import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

class Chain<T> implements ChainInterface<T> {
	private HashSet<T> chain;

	public Chain() {
		chain = new HashSet<T>();
	}

	public boolean add(T data) {
		return chain.add(data);
	}

	public boolean remove(T data) {
		return chain.remove(data);
	}

	public boolean contains(T data) {
		return chain.contains(data);
	}

	public int size() {
		return chain.size();
	}

	public Iterator<T> iterator() {
		return chain.iterator();
	}
}