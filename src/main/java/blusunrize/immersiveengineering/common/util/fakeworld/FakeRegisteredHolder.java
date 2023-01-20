/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fakeworld;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record FakeRegisteredHolder<T>(
		T value, ResourceKey<T> key
) implements Holder<T>
{
	@Override
	public boolean isBound()
	{
		return true;
	}

	@Override
	public boolean is(ResourceLocation p_205713_)
	{
		return this.key.location().equals(p_205713_);
	}

	@Override
	public boolean is(ResourceKey<T> p_205712_)
	{
		return this.key==p_205712_;
	}

	@Override
	public boolean is(Predicate<ResourceKey<T>> p_205711_)
	{
		return p_205711_.test(this.key);
	}

	@Override
	public boolean is(TagKey<T> p_205705_)
	{
		return false;
	}

	@Override
	public Stream<TagKey<T>> tags()
	{
		return Stream.empty();
	}

	@Override
	public Either<ResourceKey<T>, T> unwrap()
	{
		return Either.right(value);
	}

	@Override
	public Optional<ResourceKey<T>> unwrapKey()
	{
		return Optional.of(key);
	}

	@Override
	public Kind kind()
	{
		return Kind.DIRECT;
	}

	@Override
	public boolean canSerializeIn(HolderOwner<T> p_255833_)
	{
		return false;
	}
}
