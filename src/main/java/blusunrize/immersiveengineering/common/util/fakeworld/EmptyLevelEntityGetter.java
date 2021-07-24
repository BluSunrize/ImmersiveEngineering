package blusunrize.immersiveengineering.common.util.fakeworld;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public class EmptyLevelEntityGetter<T extends EntityAccess> implements LevelEntityGetter<T>
{
	@Nullable
	@Override
	public T get(int p_156931_)
	{
		return null;
	}

	@Nullable
	@Override
	public T get(@Nonnull UUID p_156939_)
	{
		return null;
	}

	@Nonnull
	@Override
	public Iterable<T> getAll()
	{
		return ImmutableList.of();
	}

	@Override
	public <U extends T> void get(@Nonnull EntityTypeTest<T, U> p_156935_, @Nonnull Consumer<U> p_156936_)
	{

	}

	@Override
	public void get(@Nonnull AABB p_156937_, @Nonnull Consumer<T> p_156938_)
	{

	}

	@Override
	public <U extends T> void get(@Nonnull EntityTypeTest<T, U> p_156932_, @Nonnull AABB p_156933_, @Nonnull Consumer<U> p_156934_)
	{

	}
}
