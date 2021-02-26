package blusunrize.immersiveengineering.api.utils;

import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Do not use! This is only here to support the deprecated fields in {@link TagUtils}
 */
@Deprecated
public class LazyMirroringTagCollection<T> implements ITagCollection<T>
{
	private final Supplier<ITagCollection<T>> getter;

	public LazyMirroringTagCollection(Supplier<ITagCollection<T>> getter)
	{
		this.getter = getter;
	}

	@Nonnull
	@Override
	public Map<ResourceLocation, ITag<T>> getIDTagMap()
	{
		return getter.get().getIDTagMap();
	}

	@Nonnull
	@Override
	public ITag<T> getTagByID(@Nonnull ResourceLocation id)
	{
		return getter.get().getTagByID(id);
	}

	@Nullable
	@Override
	public ResourceLocation getDirectIdFromTag(@Nonnull ITag<T> tag)
	{
		return getter.get().getDirectIdFromTag(tag);
	}
}
