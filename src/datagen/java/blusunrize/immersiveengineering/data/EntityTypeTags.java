/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityTypeTags extends ForgeRegistryTagsProvider<EntityType<?>>
{

	public EntityTypeTags(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(generatorIn, ForgeRegistries.ENTITY_TYPES, Lib.MODID, existingFileHelper);
	}

	private static TagKey<BlockEntityType<?>> tag(ResourceLocation name)
	{
		return TagKey.create(Registry.BLOCK_ENTITY_TYPE_REGISTRY, name);
	}

	@Override
	protected void addTags()
	{
		tag(IETags.shaderbagBlacklist).add(EntityType.WITHER);
	}


	@Nonnull
	@Override
	public String getName()
	{
		return "IE entity tags";
	}
}
