import java.util.Collection;

class HashSetWithChain<T> {
	private static final int DEFAULT_TABLE_SIZE = 32;
	private static final int DEFAULT_THREAD_SIZE = 1;
	private static final int DEFAULT_UPPER_LIMIT_OF_THREAD_NUMS = 16;
	private int size;
	private Chain<T>[] table;

	public HashSetWithChain() {
		/*
		 * As Chain objects only contain T,
		 * we can say that this is type-safe
		 */
		@SuppressWarnings("unchecked") Chain<T>[] table = new Chain[DEFAULT_TABLE_SIZE];
		for (int i=0; i<DEFAULT_TABLE_SIZE; i++) {
			table[i] = new Chain<T>();
		}
		this.table = table;
	}

	/*
	 * @return The number of elements in set
	 */
	public int size() {
		int totalSize=0;
		for (Chain<T> chain : table) {
			totalSize += chain.size();
		}
		return totalSize;
	}

	/*
	 * Recompute the current size of set
	 */
	private void updateSize() {
		size = size();
	}

	/*
	 * @return Table that contains all the chains
	 */
	protected Chain<T>[] getTable() {
		return table;
	}

	/*
	 * @return The size of table
	 */
	protected int getTableSize() {
		return table.length;
	}

	public boolean isEmpty() {
		if (size == 0)
			return true;
		return false;
	}

	/* 
	 * Much too simple hash function used to compute the index of table
	 * to store the input data.
	 * FIXME(mas): Use the better function
	 *
	 * @param data Input data to compute a hash value
	 * @return Hash value computed with the input data
	 */
	public int hashCode(T data) {
		String strFormat = data.toString();
		int length = strFormat.length();
		int hashValue = 0;

		for (int i=0, weight=0; i<length; i++, weight++) {
			if (weight > 7) {
				weight = 0;
			}
			hashValue += (int)strFormat.charAt(i) << (4 * weight);
		}

		int result = hashValue % table.length;
		if (result < 0) {
			return (result * -1);
		}
		return result;
	}

	/*
	 * @param data Input data
	 * @return Chain that is used to add/remove/contains... the input data
	 */
	protected Chain<T> getChain(T data) {
		int hashValue    = hashCode(data);
		Chain<T>[] table = getTable();
		return table[hashValue];
	}

	/*
	 * Add the input data to set
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean add(T data) {
		Chain<T> chain   = getChain(data);
		boolean  success = chain.add(data);
		return success;
	}

	/*
	 * Add the input collection data to set
	 * 
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean addAll(Collection<? extends T> collection) {
		TaskInvoker<T> task = new TaskInvoker<T>(this, collection);
		boolean success = task.execute(SetOperationTypes.ADD_ALL);
		updateSize();
		return success;
	}

	/*
	 * Add the input set data to set
	 * 
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean addAll(HashSetWithChain<? extends T> set) {
		TaskInvoker<T> task = new TaskInvoker<T>(this, set);
		boolean success = task.execute(SetOperationTypes.ADD_ALL);
		updateSize();
		return success;
	}

	/*
	 * Remove the input data from set.
	 * Even if nothing to remove from set,
	 * return true if the operation succeeded.
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean remove(T data) {
		Chain<T> chain   = getChain(data);
		boolean  success = chain.remove(data);
		return success;
	}

	/*
	 * Remove the input collection data from set.
	 * Even if nothing to remove from set,
	 * return true if the operation succeeded.
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean removeAll(Collection<? extends T> collection) {
		HashSetWithChain<T> set = this;
		TaskInvoker<T> task = new TaskInvoker<T>(set, collection);
		boolean success = task.execute(SetOperationTypes.REMOVE_ALL);
		set.updateSize();
		return success;
	}

	/*
	 * Remove the input set data from set.
	 * Even if nothing to remove from set,
	 * return true if the operation succeeded.
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean removeAll(HashSetWithChain<? extends T> setB) {
		HashSetWithChain<T> set = this;
		TaskInvoker<T> task = new TaskInvoker<T>(set, setB);
		boolean success = task.execute(SetOperationTypes.REMOVE_ALL);
		set.updateSize();
		return success;
	}

	/*
	 * Check if the input data exists in set.
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean contains(T data) {
		Chain<T> chain = getChain(data);
		boolean  exist = chain.contains(data);
		return exist;
	}

	/*
	 * Check if all the input collection data exist in set.
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean containsAll(Collection<? extends T> collection) {
		HashSetWithChain<T> set = this;
		TaskInvoker<T> task = new TaskInvoker<T>(set, collection);
		boolean success = task.execute(SetOperationTypes.CONTAINS_ALL);
		return success;
	}

	/*
	 * Check if all the input setB elements exist in set.
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean containsAll(HashSetWithChain<? extends T> setB) {
		HashSetWithChain<T> set = this;
		TaskInvoker<T> task = new TaskInvoker<T>(set, setB);
		boolean success = task.execute(SetOperationTypes.CONTAINS_ALL);
		return success;
	}

	/*
	 * Remove all the elements in set that don't exist in collection.
	 * In other words, this is intersection operation with the input data.
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean retainAll(Collection<? extends T> collection) {
		HashSetWithChain<T> set = this;
		TaskInvoker<T> task = new TaskInvoker<T>(set, collection);
		boolean success = task.execute(SetOperationTypes.RETAIN_ALL);
		set.updateSize();
		return success;
	}

	/*
	 * Remove all the elements in set that don't exist in seB.
	 * In other words, this is intersection operation with the input setB.
	 *
	 * @param data Input data
	 * @return Result of operation
	 */
	public boolean retainAll(HashSetWithChain<? extends T> setB) {
		HashSetWithChain<T> set = this;
		TaskInvoker<T> task = new TaskInvoker<T>(set, setB);
		boolean success = task.execute(SetOperationTypes.RETAIN_ALL);
		updateSize();
		return success;
	}

	public int getThreadSizeOfSetOp() {
		int numberOfThreads = DEFAULT_THREAD_SIZE;
		int maxThreadNum = DEFAULT_TABLE_SIZE; /* Table size is the max thread size as well */
		if (numberOfThreads < 0) {
			/* Must be POSITIVE */
			numberOfThreads = 1;
		}
		else if (numberOfThreads > maxThreadNum) {
			/* If number of threads are too large, set the upper limit */
			numberOfThreads = maxThreadNum;
		}
		return numberOfThreads;
	}

	public static <T> HashSetWithChain<T> union(HashSetWithChain<? extends T> a,
												HashSetWithChain<? extends T> b) {
		HashSetWithChain<T> union = new HashSetWithChain<T>();

		/* Add all of set a to union */
		TaskInvoker<T> taskA = new TaskInvoker<T>(union, a);
		boolean successA = taskA.execute(SetOperationTypes.ADD_ALL);

		/* Add all of set b to union */
		TaskInvoker<T> taskB = new TaskInvoker<T>(union, b);
		boolean successB = taskB.execute(SetOperationTypes.ADD_ALL);

		union.updateSize();
		return union;
	}

	public static <T> HashSetWithChain<T> intersection(HashSetWithChain<? extends T> a,
													   HashSetWithChain<? extends T> b) {
		/*
		 * FIXME(mas):
		 * This optimization should be done in the implementation?
		 * If so then, retailAll.java class has its responsbility.
		 */
		if (a.size() > b.size()) { // Faster algorithm
			return HashSetWithChain.<T>intersection(b, a);
		}

		HashSetWithChain<T> intersection = new HashSetWithChain<T>();

		/* Add all of set a to intersection */
		TaskInvoker<T> taskA = new TaskInvoker<T>(intersection, a);
		boolean successA = taskA.execute(SetOperationTypes.ADD_ALL);

		/* Remove elements in b which are not in intersection */
		TaskInvoker<T> taskB = new TaskInvoker<T>(intersection, b);
		boolean successB = taskB.execute(SetOperationTypes.RETAIN_ALL);

		intersection.updateSize();
		return intersection;
	}

	public static <T> HashSetWithChain<T> difference(HashSetWithChain<? extends T> a,
													 HashSetWithChain<? extends T> b) {
		HashSetWithChain<T> diff = new HashSetWithChain<T>();

		/* Add all of set a to diff */
		TaskInvoker<T> taskA = new TaskInvoker<T>(diff, a);
		boolean successA = taskA.execute(SetOperationTypes.ADD_ALL);

		/* Remove all of set b from diff */
		TaskInvoker<T> taskB = new TaskInvoker<T>(diff, b);
		boolean successB = taskB.execute(SetOperationTypes.REMOVE_ALL);

		diff.updateSize();
		return diff;
	}

	public static <T> HashSetWithChain<T> symmetricDifference(HashSetWithChain<? extends T> a,
															  HashSetWithChain<? extends T> b) {
		/* Union the difference of a and b & b and a */
		HashSetWithChain<T> left  = HashSetWithChain.<T>difference(a, b);
		HashSetWithChain<T> right = HashSetWithChain.<T>difference(a, b);
		HashSetWithChain<T> sdiff = HashSetWithChain.<T>union(left, right);
		return sdiff;
	}
}