import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.Iterator;

class ContainsAll<T> extends SetOperation<T> {
	private HashSetWithChain<T> setA;
	private HashSetWithChain<? extends T> setB; /* Not used when adding collection */
	private Chain<T>[] tableA;
	private Chain<? extends T>[] tableB;		/* Not used when adding collection */
	private Collection<? extends T> collection; /* Not used when adding set */
	private int from;
	private int to;

	public ContainsAll(HashSetWithChain<T> setA,
				 	   HashSetWithChain<? extends T> setB,
					   Collection<? extends T> collection,
					   int from, int to) {
		this.setA = setA;
		this.setB = setB;
		this.tableA = setA.getTable();
		this.tableB = (setB == null) ? null : setB.getTable(); 
		this.collection = collection;
		this.from = from;
		this.to   = to;
	}

	/*
	 * Check if all the elements in setB exist in setA.
	 */
	@Override
	public Boolean call() {
		try {
			if (setB == null) { // Indicates that we'll add collections, NOT SET.
				Boolean result = containsAllElementsOfCollection();
				return result;
			}
			else { // Will add elements in setB
				Boolean result = containsAllElementsOfSet();
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private Boolean containsAllElementsOfCollection() {
		Iterator<? extends T> iter = collection.iterator();

		while (iter.hasNext()) {
			/*
			* As we intend to create Chain<T> object using 
			* Chain<? extends T>, we can say that this is type-safe
			*/ 	 	 
			@SuppressWarnings("unchecked") T elem = 
				(T)iter.next();
			int hashValue = setA.hashCode(elem);
			if (from <= hashValue && hashValue < to) {
				Chain<T> chainA = tableA[hashValue];
				if (!chainA.contains(elem)) {
					return false;
				}
		 	}
		}
		return true;
	}

	private Boolean containsAllElementsOfSet() {
		for (int i=from; i<to; i++) {
			Chain<T> 		   chainA = tableA[i];
			Chain<? extends T> chainB = tableB[i];

			/* Add all elements in chainB by iterating chainB */
			Iterator iter = chainB.iterator();
			while (iter.hasNext()) {
				/*
			 	 * As we intend to create Chain<T> object using 
			 	 * Chain<? extends T>, we can say that this is type-safe 
			 	 */
				@SuppressWarnings("unchecked") T elem = 
					(T)iter.next();
				if (!chainA.contains(elem)) {
					return false;
				}
			}
		}
		return true;
	}
}