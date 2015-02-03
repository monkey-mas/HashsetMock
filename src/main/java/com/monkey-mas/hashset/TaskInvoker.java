import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class TaskInvoker<T> {
	private HashSetWithChain<T> setA;
	private HashSetWithChain<? extends T> setB;
	private Collection<? extends T> collection;
	private int numberOfThreads;

	/* Used for basic operations */
	public TaskInvoker(HashSetWithChain<T> a,
					   Collection<? extends T> collection) {
		setA = a;
		this.collection = collection;
		numberOfThreads = a.getThreadSizeOfSetOp();
	}

	/* Used for basic operations */
	public TaskInvoker(HashSetWithChain<T> a,
					   HashSetWithChain<? extends T> b) {
		setA = a;
		setB = b;
		numberOfThreads = a.getThreadSizeOfSetOp();
	}

	/*
	 * Logic of multithreaded set operation
	 */
	protected final boolean execute(SetOperationTypes setOperation) {
		/*
		 * As we expect our Future objects to return only Boolean,
		 * we can say that this is type-safe
		 */
		@SuppressWarnings("unchecked") Future<Boolean> future[] =
			new Future[numberOfThreads];

		/* Create thread pool */
		ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

		/* Invoke threads as many threads as being set up */
		invokeThread(service, future, setOperation);

		/* Check results from thread execution */
		collectResults(future);

		/* Destroy the thread pool */
		service.shutdown();

		return true;
	}

	/*
	 * This is the part you create an object that is used when calling Callable()
	 */
	private SetOperation<T> createOperation(int from, int to,
											SetOperationTypes setOperation) {
		Chain<T>[] 			 tableA = setA.getTable();
		Chain<? extends T>[] tableB = (setB == null) ? null : setB.getTable();

		SetOperation<T> operation = null;
		/* Set proper operation */
		if (setOperation == SetOperationTypes.ADD_ALL) {
			operation = new AddAll<T>(setA, setB, collection, from, to);
		}
		else if (setOperation == SetOperationTypes.REMOVE_ALL) {
			operation = new RemoveAll<T>(setA, setB, collection, from, to);
		}
		else if (setOperation == SetOperationTypes.RETAIN_ALL) {
			operation = new RetainAll<T>(setA, setB, collection, from, to);
		}
		else if (setOperation == SetOperationTypes.CONTAINS_ALL) {
			operation = new ContainsAll<T>(setA, setB, collection, from, to);
		} else {
			//
		}
		return operation;
	}

	/*
	 * Invoke threads to add/remove/find collection of elements from setA
	 */
	private void invokeThread(ExecutorService service,
							  Future<Boolean>[] future,
							  SetOperationTypes setOperation) {
		int nextIndex = 0;
		int taskSizeLeft = setA.getTableSize();

		for (int count=0; count < numberOfThreads; count++) {
			int sizePerThread = taskSizeLeft / (numberOfThreads-count);
			/* Range of table index to do operation */
			int from = nextIndex;
			int to   = nextIndex+sizePerThread;
			
			/* Create proper object of operation */
			SetOperation<T> operation = createOperation(from, to, setOperation);

			/* Thread submmition */
			try {
				future[count] = service.submit(operation);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			/* Set the next _from_ index */
			nextIndex += sizePerThread;
			taskSizeLeft -= sizePerThread;
		}
	}

	private void collectResults(Future<Boolean>[] future) {
		for (Future<Boolean> result : future) {
			try {
				boolean succeess = (boolean) result.get();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
}