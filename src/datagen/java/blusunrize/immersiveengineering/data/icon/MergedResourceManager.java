/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.icon;

import com.google.common.collect.Sets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.Unit;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record MergedResourceManager(ResourceManager first, ResourceManager second) implements ReloadableResourceManager
{
	@Nonnull
	@Override
	public Set<String> getNamespaces()
	{
		return Sets.union(first.getNamespaces(), second.getNamespaces());
	}

	@Override
	public boolean hasResource(@Nonnull ResourceLocation pPath)
	{
		return first.hasResource(pPath)||second.hasResource(pPath);
	}

	@Nonnull
	@Override
	public List<Resource> getResources(@Nonnull ResourceLocation pResourceLocation) throws IOException
	{
		List<Resource> result = new ArrayList<>();
		Exception xcp = null;
		try
		{
			result.addAll(first.getResources(pResourceLocation));
		} catch(IOException x)
		{
			xcp = x;
		}
		try
		{
			result.addAll(second.getResources(pResourceLocation));
		} catch(IOException x)
		{
			if(xcp!=null) throw x;
		}
		return result;
	}

	@Nonnull
	@Override
	public Collection<ResourceLocation> listResources(@Nonnull String pPath, @Nonnull Predicate<String> pFilter)
	{
		Set<ResourceLocation> result = new HashSet<>(first.listResources(pPath, pFilter));
		result.addAll(second.listResources(pPath, pFilter));
		return result;
	}

	@Nonnull
	@Override
	public Stream<PackResources> listPacks()
	{
		return Stream.concat(first.listPacks(), second.listPacks());
	}

	@Nonnull
	@Override
	public Resource getResource(@Nonnull ResourceLocation p_143935_) throws IOException
	{
		try
		{
			return first.getResource(p_143935_);
		} catch(IOException x)
		{
			return second.getResource(p_143935_);
		}
	}

	@Nonnull
	@Override
	public ReloadInstance createReload(@Nonnull Executor p_143930_, @Nonnull Executor p_143931_, @Nonnull CompletableFuture<Unit> p_143932_, @Nonnull List<PackResources> p_143933_)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerReloadListener(@Nonnull PreparableReloadListener pListener)
	{

	}

	@Override
	public void close()
	{

	}
}
