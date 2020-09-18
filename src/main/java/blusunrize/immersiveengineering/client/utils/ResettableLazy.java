package blusunrize.immersiveengineering.client.utils;

import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ResettableLazy<T> implements Supplier<T>
{
	private final NonNullSupplier<T> getter;
	private final NonNullConsumer<T> destructor;
	@Nullable
	private T cached;

	public ResettableLazy(NonNullSupplier<T> getter)
	{
		this(getter, v -> {
		});
	}

	public ResettableLazy(NonNullSupplier<T> getter, NonNullConsumer<T> destructor)
	{
		this.getter = getter;
		this.destructor = destructor;
	}

	@Nonnull
	public T get()
	{
		if(cached==null)
			cached = getter.get();
		return cached;
	}

	public void reset()
	{
		if(cached!=null)
		{
			destructor.accept(cached);
			cached = null;
		}
	}
}
