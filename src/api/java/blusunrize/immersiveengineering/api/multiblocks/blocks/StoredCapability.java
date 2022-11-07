package blusunrize.immersiveengineering.api.multiblocks.blocks;

import net.minecraftforge.common.util.LazyOptional;

public class StoredCapability<T>
{
	private final T value;
	private LazyOptional<T> cap = LazyOptional.empty();

	public StoredCapability(T value)
	{
		this.value = value;
	}

	public LazyOptional<T> get(IMultiblockContext<?> ctx)
	{
		if(cap.isPresent())
			return cap;
		final var result = ctx.registerCapability(value);
		this.cap = result;
		return result;
	}

	public <U> LazyOptional<U> cast(IMultiblockContext<?> ctx)
	{
		return get(ctx).cast();
	}
}
