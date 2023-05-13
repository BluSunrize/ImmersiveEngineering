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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class EntityTypeTags extends EntityTypeTagsProvider
{

	public EntityTypeTags(PackOutput output, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(output, lookupProvider, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider p_255894_)
	{
		tag(IETags.shaderbagBlacklist).add(EntityType.WITHER).add(EntityType.IRON_GOLEM);
		tag(net.minecraft.tags.EntityTypeTags.RAIDERS).add(IEEntityTypes.FUSILIER.get(), IEEntityTypes.COMMANDO.get(), IEEntityTypes.BULWARK.get());
	}


	@Nonnull
	@Override
	public String getName()
	{
		return "IE entity tags";
	}
}
