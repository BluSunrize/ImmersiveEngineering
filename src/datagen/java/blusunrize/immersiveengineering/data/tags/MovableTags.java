/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.tags;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.neoforge.common.Tags.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MovableTags extends IntrinsicHolderTagsProvider<Block>
{
	public MovableTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(
				output, Registries.BLOCK, provider,
				bet -> BuiltInRegistries.BLOCK.getResourceKey(bet).orElseThrow(),
				Lib.MODID, existingFileHelper
		);
	}

	private static final List<TagKey<Block>> IMMOVABLE_TAGS = ImmutableList.of(Blocks.RELOCATION_NOT_SUPPORTED);

	private static TagKey<Block> tag(ResourceLocation name)
	{
		return TagKey.create(Registries.BLOCK, name);
	}

	@Override
	protected void addTags(Provider p_256380_)
	{
		// Some tiles need the config to be available in the constructor, so just load the default values
		ConfigTracker.INSTANCE.loadDefaultServerConfigs();
		IEServerConfig.refresh();
		for(Holder<Block> type : IEBlocks.REGISTER.getEntries())
			if(type.value() instanceof EntityBlock entityBlock)
			{
				BlockEntity instance = entityBlock.newBlockEntity(BlockPos.ZERO, type.value().defaultBlockState());
				if(instance instanceof IImmersiveConnectable||instance instanceof IGeneralMultiblock)
					notMovable(type);
			}
	}

	private void notMovable(Holder<Block> type)
	{
		notMovable(type.value());
	}

	private void notMovable(Block type)
	{
		for(TagKey<Block> tag : IMMOVABLE_TAGS)
			tag(tag).add(type);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "IE tile tags";
	}
}
