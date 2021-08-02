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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.mixin.accessors.TileTypeAccess;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileTags extends ForgeRegistryTagsProvider<BlockEntityType<?>>
{

	public TileTags(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(generatorIn, ForgeRegistries.BLOCK_ENTITIES, Lib.MODID, existingFileHelper);
	}

	private static final List<Named<BlockEntityType<?>>> IMMOVABLE_TAGS = ImmutableList.of(
			tag(new ResourceLocation("forge", "relocation_not_supported")),
			tag(new ResourceLocation("forge", "immovable"))
	);

	private static Named<BlockEntityType<?>> tag(ResourceLocation name)
	{
		return ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCK_ENTITIES, name);
	}

	@Override
	protected void addTags()
	{
		// Some tiles needs to config to be available in the constructor, so just load the default values
		IEServerConfig.CONFIG_SPEC.refreshCached();
		IEServerConfig.refresh();
		for(RegistryObject<BlockEntityType<?>> type : IETileTypes.REGISTER.getEntries())
		{
			BlockEntity instance = type.get().create(BlockPos.ZERO, ((TileTypeAccess)type.get()).getValidBlocks().iterator().next().defaultBlockState());
			if(instance instanceof IImmersiveConnectable||instance instanceof IGeneralMultiblock)
				notMovable(type);
		}
	}

	private void notMovable(RegistryObject<BlockEntityType<?>> type)
	{
		notMovable(type.get());
	}

	private void notMovable(BlockEntityType<?> type)
	{
		for(Named<BlockEntityType<?>> tag : IMMOVABLE_TAGS)
			tag(tag).add(type);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "IE tile tags";
	}
}
