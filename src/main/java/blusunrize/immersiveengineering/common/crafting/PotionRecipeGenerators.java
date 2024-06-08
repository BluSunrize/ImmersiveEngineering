/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.common.crafting.PotionHelper.getFluidTagForType;
import static blusunrize.immersiveengineering.common.fluids.PotionFluid.getFluidStackForType;

/**
 * @author BluSunrize - 22.02.2017
 */
public class PotionRecipeGenerators
{
	public static final Set<String> BLACKLIST = new HashSet<>();

	public static List<MixerRecipe> initPotionRecipes()
	{
		Map<Potion, List<MixerRecipe>> recipes = new HashMap<>();
		PotionHelper.applyToAllPotionRecipes((out, in, reagent) -> registerPotionRecipe(out, in, reagent, recipes));
		return recipes.values().stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public static List<BottlingMachineRecipe> getPotionBottlingRecipes()
	{
		Map<Holder<Potion>, BottlingMachineRecipe> recipes = new HashMap<>();
		Function<Holder<Potion>, BottlingMachineRecipe> toRecipe = potion -> {
			ItemStack potionStack = new ItemStack(Items.POTION);
			potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
			return new BottlingMachineRecipe(
					new TagOutputList(new TagOutput(potionStack)),
					IngredientWithSize.of(new ItemStack(Items.GLASS_BOTTLE)),
					getFluidTagForType(potion, 250)
			);
		};
		PotionHelper.applyToAllPotionRecipes((out, in, reagent) -> {
					if(!recipes.containsKey(out))
						recipes.put(out, toRecipe.apply(out));
				}
		);
		recipes.put(Potions.WATER, toRecipe.apply(Potions.WATER));
		IELogger.logger.info(
				"Recipes for potions: "+recipes.keySet().stream()
						.map(h -> h.unwrapKey().orElseThrow().location().toString())
						.collect(Collectors.joining(", "))
		);
		return new ArrayList<>(recipes.values());
	}

	public static void registerPotionRecipe(
			Holder<Potion> output, Holder<Potion> input, IngredientWithSize reagent, Map<Potion, List<MixerRecipe>> all
	)
	{
		ResourceLocation outputID = output.unwrapKey().orElseThrow().location();
		if(!BLACKLIST.contains(outputID.toString()))
		{
			List<MixerRecipe> existing = all.computeIfAbsent(output.value(), p -> new ArrayList<>());

			MixerRecipe recipe = new MixerRecipe(getFluidStackForType(Optional.of(output), FluidType.BUCKET_VOLUME),
					getFluidTagForType(input, FluidType.BUCKET_VOLUME), List.of(reagent), 6400);
			existing.add(recipe);
		}
	}
}
