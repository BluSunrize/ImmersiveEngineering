/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import net.minecraftforge.fml.packs.ResourcePackLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class StaticTemplateManager
{
	public static Optional<InputStream> getModResource(ResourcePackType type, ResourceLocation name)
	{
		return ModList.get().getMods().stream()
				.map(ModInfo::getModId)
				.map(ResourcePackLoader::getResourcePackFor)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(mfrp -> mfrp.resourceExists(type, name))
				.map(mfrp -> getInputStreamOrThrow(type, name, mfrp))
				.findAny();
	}

	private static InputStream getInputStreamOrThrow(ResourcePackType type, ResourceLocation name, ModFileResourcePack source)
	{
		try
		{
			return source.getResourceStream(type, name);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static Template loadStaticTemplate(ResourceLocation loc) throws IOException
	{
		String[] paths = {
				"structures/"+loc.getPath()+".nbt",
				loc.getPath()+".nbt",
		};
		for(String path : paths)
		{
			Optional<InputStream> optStream = getModResource(ResourcePackType.SERVER_DATA,
					new ResourceLocation(loc.getNamespace(), path));
			if(optStream.isPresent())
				return loadTemplate(optStream.get());
		}
		throw new RuntimeException("Mod resource not found: "+loc);
	}

	public static Template loadTemplate(InputStream inputStreamIn) throws IOException
	{
		CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(inputStreamIn);
		Template template = new Template();
		template.read(compoundnbt);
		return template;
	}
}