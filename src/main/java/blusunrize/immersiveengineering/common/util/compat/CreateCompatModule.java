/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.crafting.PotionHelper;
import blusunrize.immersiveengineering.common.fluids.PotionFluid.PotionBottleType;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules.StandardIECompatModule;
import com.mojang.serialization.JavaOps;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentPredicate.Builder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;

import java.util.EnumMap;

public class CreateCompatModule extends StandardIECompatModule
{
	@Override
	public void init()
	{
		Fluid potionFluid = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("create", "potion"));
		//noinspection unchecked
		DataComponentType<Enum<?>> bottleComponentType = (DataComponentType<Enum<?>>)BuiltInRegistries.DATA_COMPONENT_TYPE.get(
				ResourceLocation.fromNamespaceAndPath("create", "potion_fluid_bottle_type")
		);
		if(bottleComponentType==null)
			return;

		// build map of our types to Create types
		final EnumMap<PotionBottleType, Enum<?>> typeConversion = new EnumMap<>(PotionBottleType.class);
		for(PotionBottleType myBottle : PotionBottleType.values())
			bottleComponentType.codec()
					.terminal()
					.decode(JavaOps.INSTANCE, myBottle.getSerializedName())
					.result()
					.ifPresent(t -> typeConversion.put(myBottle, t));


		PotionHelper.CREATE_POTION_BUILDER = (potionHolder, potionBottleType) -> {
			Builder pred = DataComponentPredicate.builder()
					.expect(DataComponents.POTION_CONTENTS, new PotionContents(potionHolder));
			if(potionBottleType!=null)
				pred.expect(bottleComponentType, typeConversion.get(potionBottleType));
			return DataComponentFluidIngredient.of(false, pred.build(), potionFluid);
		};
	}
}