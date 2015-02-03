import java.util.concurrent.Callable;

public abstract class SetOperation<T> implements Callable<Boolean> {
	public abstract Boolean call();
}