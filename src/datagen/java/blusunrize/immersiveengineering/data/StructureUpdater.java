/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class StructureUpdater implements IDataProvider
{
	private static final Logger LOGGER = LogManager.getLogger();

	private final String basePath;
	private final String modid;
	private final DataGenerator gen;
	private final SimpleReloadableResourceManager resources;

	public StructureUpdater(
			String basePath, String modid, ExistingFileHelper helper, DataGenerator gen
	)
	{
		this.basePath = basePath;
		this.modid = modid;
		this.gen = gen;
		try
		{
			Field serverData = ExistingFileHelper.class.getDeclaredField("serverData");
			serverData.setAccessible(true);
			resources = (SimpleReloadableResourceManager)serverData.get(helper);
		} catch(NoSuchFieldException|IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void act(@Nonnull DirectoryCache cache) throws IOException
	{
		for(ResourceLocation loc : resources.getAllResourceLocations(basePath, $ -> true))
			if(loc.getNamespace().equals(modid))
				process(loc, cache);
	}

	private void process(ResourceLocation loc, DirectoryCache cache) throws IOException
	{
		CompoundNBT inputNBT = CompressedStreamTools.readCompressed(
				resources.getResource(loc).getInputStream()
		);
		CompoundNBT converted = updateNBT(inputNBT);
		if(!converted.equals(inputNBT))
			writeNBTTo(loc, converted, cache);
	}

	private void writeNBTTo(ResourceLocation loc, CompoundNBT data, DirectoryCache cache) throws IOException
	{
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		CompressedStreamTools.writeCompressed(data, bytearrayoutputstream);
		byte[] bytes = bytearrayoutputstream.toByteArray();
		String hashString = HASH_FUNCTION.hashBytes(bytes).toString();
		Path outputPath = gen.getOutputFolder().resolve("data/"+loc.getNamespace()+"/"+loc.getPath());

		if(!Objects.equals(cache.getPreviousHash(outputPath), hashString)||!Files.exists(outputPath))
		{
			Files.createDirectories(outputPath.getParent());
			try(OutputStream outputstream = Files.newOutputStream(outputPath))
			{
				outputstream.write(bytes);
			}
		}
		cache.recordHash(outputPath, hashString);
	}

	private static CompoundNBT updateNBT(CompoundNBT nbt)
	{
		final CompoundNBT updatedNBT = NBTUtil.update(
				DataFixesManager.getDataFixer(), DefaultTypeReferences.STRUCTURE, nbt, nbt.getInt("DataVersion")
		);
		Template template = new Template();
		template.read(updatedNBT);
		return template.writeToNBT(new CompoundNBT());
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Update structure files in "+basePath;
	}
}
