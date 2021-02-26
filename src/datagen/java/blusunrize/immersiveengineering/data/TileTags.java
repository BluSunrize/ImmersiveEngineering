/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileTags extends ForgeRegistryTagsProvider<TileEntityType<?>>
{

	public TileTags(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(generatorIn, ForgeRegistries.TILE_ENTITIES, Lib.MODID, existingFileHelper);
	}

	private static final List<INamedTag<TileEntityType<?>>> IMMOVABLE_TAGS = ImmutableList.of(
			tag(new ResourceLocation("forge", "relocation_not_supported")),
			tag(new ResourceLocation("forge", "immovable"))
	);

	private static INamedTag<TileEntityType<?>> tag(ResourceLocation name)
	{
		return ForgeTagHandler.makeWrapperTag(ForgeRegistries.TILE_ENTITIES, name);
	}

	@Override
	protected void registerTags()
	{
		// Some tiles needs to config to be available in the constructor, so just load the default values
		IEServerConfig.refresh();
		for(RegistryObject<TileEntityType<?>> type : IETileTypes.REGISTER.getEntries())
		{
			TileEntity instance = type.get().create();
			if(instance instanceof IImmersiveConnectable||instance instanceof IGeneralMultiblock)
				notMovable(type);
		}
	}

	private void notMovable(RegistryObject<TileEntityType<?>> type)
	{
		for(INamedTag<TileEntityType<?>> tag : IMMOVABLE_TAGS)
			getOrCreateBuilder(tag).add(type.get());
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "IE tile tags";
	}
}
