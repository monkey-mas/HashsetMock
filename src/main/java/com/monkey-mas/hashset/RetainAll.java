import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.Iterator;

class RetainAll<T> extends SetOperation<T> {
	private HashSetWithChain<T> setA;
	private HashSetWithChain<? extends T> setB; /* Not used when adding collection */
	private Chain<T>[] tableA;
	private Chain<? extends T>[] tableB;		/* Not used when adding collection */
	private Collection<? extends T> collection; /* Not used when adding set */
	private int from;
	private int to;

	public RetainAll(HashSetWithChain<T> setA,
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
	 * Retain elements of setA and setB.
	 */
	@Override
	public Boolean call() {
		try {
			if (setB == null) { // Indicates that we'll add collections, NOT SET.
				retainAllElementsOfCollection();
			}
			else { // Will add elements in setB
				retainAllElementsOfSet();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void retainAllElementsOfCollection() {
		Iterator<? extends T> iter = collection.iterator();
		Chain<T> chainFiltered = new Chain<T>();

		/*
		 * Filter elements in collection to retain from elements in chainA
		 * (ranging from _from_ to _to_)
		 */
		while (iter.hasNext()) {
			/*
			* As we intend to create Chain<T> object using 
			* Chain<? extends T>, we can say that this is type-safe
			*/ 	 	 
			@SuppressWarnings("unchecked") T elem = 
				(T)iter.next();
			int hashValue = setA.hashCode(elem);
			if (from <= hashValue && hashValue < to) {
				chainFiltered.add(elem);
		 	}
		}

		/* Remove elements in chainA if they don't exist in chianFiltered */
		for (int i=from; i<to; i++) {
			Chain<T> chainA = tableA[i];			
			Iterator<T> filtered_collection = chainA.iterator();
			while (filtered_collection.hasNext()) {
				T elem = filtered_collection.next();
				if (!chainFiltered.contains(elem)) {
					chainA.remove(elem);
				}
			}
		}
	}

	private void retainAllElementsOfSet() {
		for (int i=from; i<to; i++) {
			Chain<T>		   retain = new Chain<T>(); /* */
			Chain<T>		   temp   = new Chain<T>();
			Chain<T> 		   chainA = tableA[i];
			Chain<? extends T> chainB = tableB[i];

			/* Add all elements in chainA to temp by iterating chainA */		
			Iterator<T> iterA = chainA.iterator();
			while (iterA.hasNext()) {
				T elem = iterA.next();
				temp.add(elem);
			}

			/* Add all elements in chainB to temp by iterating chainB */
			Iterator<? extends T> iterB = chainB.iterator();
			while (iterB.hasNext()) {
				/*
			 	 * As we intend to create Chain<T> object using 
			 	 * Chain<? extends T>, we can say that this is type-safe 
			 	 */
				@SuppressWarnings("unchecked") T elem = 
					(T)iterB.next();
				if(temp.contains(elem)) {
					retain.add(elem);
				}
			}

			/* Set the updated chain to tableA */
			tableA[i] = retain;
		}
	}
}