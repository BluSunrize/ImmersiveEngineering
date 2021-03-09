package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import java.util.function.Consumer;

public class EventWaiterResult
{
	private final Consumer<Runnable> startWithCallback;
	private final String name;

	public EventWaiterResult(Consumer<Runnable> startWithCallback, String name)
	{
		this.startWithCallback = startWithCallback;
		this.name = name;
	}

	public final void start(Runnable callback)
	{
		startWithCallback.accept(callback);
	}

	public String getName()
	{
		return name;
	}
}
