/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.dynregistry;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.Lib.DamageTypes;
import blusunrize.immersiveengineering.api.Lib.TurretDamageType;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

		tag(DamageTypeTags.IS_FIRE)
				.add(DamageTypes.REVOLVER_DRAGONSBREATH.playerType())
				.add(DamageTypes.REVOLVER_DRAGONSBREATH.turretType());
		tag(DamageTypeTags.BYPASSES_ARMOR)
				.add(DamageTypes.RAZOR_SHOCK)
				.add(DamageTypes.WIRE_SHOCK)
				.add(DamageTypes.TESLA)
				.add(DamageTypes.TESLA_PRIMARY)
				.add(DamageTypes.REVOLVER_ARMORPIERCING.playerType())
				.add(DamageTypes.REVOLVER_ARMORPIERCING.turretType())
				.add(DamageTypes.RAILGUN.playerType())
				.add(DamageTypes.SAWBLADE.playerType());
		// ALL the projectiles!
		List<TurretDamageType> projectiles = List.of(
				DamageTypes.REVOLVER_CASULL, DamageTypes.REVOLVER_ARMORPIERCING, DamageTypes.REVOLVER_BUCKSHOT,
				DamageTypes.REVOLVER_DRAGONSBREATH, DamageTypes.REVOLVER_HOMING, DamageTypes.REVOLVER_WOLFPACK,
				DamageTypes.REVOLVER_SILVER, DamageTypes.REVOLVER_POTION, DamageTypes.RAILGUN, DamageTypes.SAWBLADE
		);
		projectiles.forEach(turretDamageType -> tag(DamageTypeTags.IS_PROJECTILE)
				.add(turretDamageType.playerType())
				.add(turretDamageType.turretType()));
	}
}
