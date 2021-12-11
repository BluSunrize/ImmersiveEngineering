/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerUpper;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
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

public class StructureUpdater implements DataProvider
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
	public void run(@Nonnull HashCache cache) throws IOException
	{
		for(ResourceLocation loc : resources.listResources(basePath, $ -> true))
			if(loc.getNamespace().equals(modid))
				process(loc, cache);
	}

	private void process(ResourceLocation loc, HashCache cache) throws IOException
	{
		CompoundTag inputNBT = NbtIo.readCompressed(
				resources.getResource(loc).getInputStream()
		);
		CompoundTag converted = updateNBT(inputNBT);
		if(!converted.equals(inputNBT))
		{
			Class<? extends DataFixer> fixerClass = DataFixers.getDataFixer().getClass();
			if (!fixerClass.equals(DataFixerUpper.class))
				throw new RuntimeException("Structures are not up to date, but unknown data fixer is in use: "+fixerClass.getName());
			writeNBTTo(loc, converted, cache);
		}
	}

	private void writeNBTTo(ResourceLocation loc, CompoundTag data, HashCache cache) throws IOException
	{
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		NbtIo.writeCompressed(data, bytearrayoutputstream);
		byte[] bytes = bytearrayoutputstream.toByteArray();
		String hashString = SHA1.hashBytes(bytes).toString();
		Path outputPath = gen.getOutputFolder().resolve("data/"+loc.getNamespace()+"/"+loc.getPath());

		if(!Objects.equals(cache.getHash(outputPath), hashString)||!Files.exists(outputPath))
		{
			Files.createDirectories(outputPath.getParent());
			try(OutputStream outputstream = Files.newOutputStream(outputPath))
			{
				outputstream.write(bytes);
			}
		}
		cache.putNew(outputPath, hashString);
	}

	private static CompoundTag updateNBT(CompoundTag nbt)
	{
		final CompoundTag updatedNBT = NbtUtils.update(
				DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, nbt, nbt.getInt("DataVersion")
		);
		StructureTemplate template = new StructureTemplate();
		template.load(updatedNBT);
		return template.save(new CompoundTag());
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Update structure files in "+basePath;
	}
}
