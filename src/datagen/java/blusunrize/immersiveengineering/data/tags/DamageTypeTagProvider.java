/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.tags;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DamageTypeTagProvider extends TagsProvider<DamageType>
{
	public DamageTypeTagProvider(
			PackOutput output,
			CompletableFuture<Provider> provider,
			@Nullable ExistingFileHelper existingFileHelper
	)
	{
		super(output, Registries.DAMAGE_TYPE, provider, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(@NotNull Provider provider)
	{
		tag(DamageTypeTags.IS_FIRE).add(Lib.DMG_RevolverDragon);
		tag(DamageTypeTags.BYPASSES_ARMOR)
				.add(Lib.DMG_RazorShock)
				.add(Lib.DMG_WireShock)
				.add(Lib.DMG_Tesla)
				.add(Lib.DMG_Tesla_prim)
				.add(Lib.DMG_RevolverAP)
				.add(Lib.DMG_Railgun)
				.add(Lib.DMG_Sawblade);
	}
}
