/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import net.minecraft.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.packs.DelegatableResourcePack;
import net.minecraftforge.fml.packs.DelegatingResourcePack;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import net.minecraftforge.forgespi.language.IModInfo;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;

// Works around the horrible performance of DelegatingResourcePack in larger packs
// TODO open issue and maybe even PR at Forge
public class FastResourceAccess
{
	public static boolean hasResource(IResourceManager in, ResourceLocation loc)
	{
		if(in instanceof SimpleReloadableResourceManager)
		{
			SimpleReloadableResourceManager reloadable = (SimpleReloadableResourceManager)in;
			FallbackResourceManager forNamespace = reloadable.namespaceResourceManagers.get(loc.getNamespace());
			if(forNamespace!=null)
				return hasResourceFallback(forNamespace, loc);
			else
				return false;
		}
		else if(in instanceof FallbackResourceManager)
			return hasResourceFallback((FallbackResourceManager)in, loc);
		else
			return in.hasResource(loc);
	}

	public static IResource getResource(IResourceManager in, ResourceLocation loc) throws IOException
	{
		if(in instanceof SimpleReloadableResourceManager)
		{
			SimpleReloadableResourceManager reloadable = (SimpleReloadableResourceManager)in;
			FallbackResourceManager forNamespace = reloadable.namespaceResourceManagers.get(loc.getNamespace());
			if(forNamespace!=null)
				return getResourceFallback(forNamespace, loc);
			else
				throw new FileNotFoundException(loc.toString());
		}
		else if(in instanceof FallbackResourceManager)
			return getResourceFallback((FallbackResourceManager)in, loc);
		else
			return in.getResource(loc);
	}

	@Nonnull
	public static IResource getResourceFallback(FallbackResourceManager base, @Nonnull ResourceLocation resourceLocationIn) throws IOException
	{
		for(int i = base.resourcePacks.size()-1; i >= 0; --i)
		{
			IResourcePack currentPack = base.resourcePacks.get(i);
			InputStream stream = applyGuessing(
					currentPack,
					resourceLocationIn,
					(rp, rl) -> {
						try
						{
							return rp.getResourceStream(ResourcePackType.CLIENT_RESOURCES, rl);
						} catch(IOException e)
						{
							throw new RuntimeException(e);
						}
					},
					null
			);
			if(stream!=null)
				return new SimpleResource(
						currentPack.getName(),
						resourceLocationIn,
						stream,
						null
				);
		}

		throw new FileNotFoundException(resourceLocationIn.toString());
	}

	public static boolean hasResourceFallback(FallbackResourceManager base, @Nonnull ResourceLocation resourceLocationIn)
	{
		for(int i = base.resourcePacks.size()-1; i >= 0; --i)
		{
			IResourcePack currentPack = base.resourcePacks.get(i);
			if(applyGuessing(currentPack, resourceLocationIn, (rp, rl) -> true, false))
				return true;
		}
		return false;
	}

	private static final Field DelegatingResourcePack_delegates;

	static
	{
		// Allow for this to fail, to keep the code working after this is fixed in Forge
		Field delegates;
		try
		{
			delegates = DelegatingResourcePack.class.getDeclaredField("delegates");
			delegates.setAccessible(true);
			if(delegates.getType()!=List.class)
				delegates = null;
		} catch(Exception x)
		{
			x.printStackTrace();
			delegates = null;
		}
		DelegatingResourcePack_delegates = delegates;
	}

	@SuppressWarnings("unchecked")
	private static List<DelegatableResourcePack> getDelegates(DelegatingResourcePack pack)
	{
		try
		{
			return (List<DelegatableResourcePack>)DelegatingResourcePack_delegates.get(pack);
		} catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static <T> T applyGuessing(
			IResourcePack pack,
			ResourceLocation loc,
			BiFunction<IResourcePack, ResourceLocation, T> func,
			T defaultRet
	)
	{
		if(DelegatingResourcePack_delegates!=null&&pack instanceof DelegatingResourcePack)
		{
			List<DelegatableResourcePack> delegates = getDelegates((DelegatingResourcePack)pack);
			DelegatableResourcePack guessed = null;
			// If one of the sub-packs is the one from the mod owning this mod ID, check that first
			for(DelegatableResourcePack subPack : delegates)
				if(subPack instanceof ModFileResourcePack)
				{
					ModFileResourcePack mod = (ModFileResourcePack)subPack;
					for(IModInfo info : mod.getModFile().getModInfos())
						if(info.getModId().equals(loc.getNamespace()))
						{
							guessed = subPack;
							break;
						}
					if(guessed!=null)
					{
						if(subPack.resourceExists(ResourcePackType.CLIENT_RESOURCES, loc))
							return func.apply(subPack, loc);
						else
							// Only one sub-pack per mod ID
							break;
					}
				}
			// Guess failed => check all other subpacks
			for(DelegatableResourcePack subPack : delegates)
				if(subPack!=guessed&&subPack.resourceExists(ResourcePackType.CLIENT_RESOURCES, loc))
					return func.apply(subPack, loc);
			return defaultRet;
		}
		else if(pack.resourceExists(ResourcePackType.CLIENT_RESOURCES, loc))
			return func.apply(pack, loc);
		else
			return defaultRet;
	}
}
