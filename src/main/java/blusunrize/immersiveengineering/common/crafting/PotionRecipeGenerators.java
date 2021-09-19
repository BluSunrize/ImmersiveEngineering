/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidAttributes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.common.crafting.PotionHelper.getFluidTagForType;
import static blusunrize.immersiveengineering.common.util.fluids.PotionFluid.getFluidStackForType;

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
		Map<Potion, BottlingMachineRecipe> recipes = new HashMap<>();
		Function<Potion, BottlingMachineRecipe> toRecipe = potion -> new BottlingMachineRecipe(
				potion.getRegistryName(),
				PotionUtils.setPotion(new ItemStack(Items.POTION), potion),
				Ingredient.of(Items.GLASS_BOTTLE),
				getFluidTagForType(potion, 250)
		);
		PotionHelper.applyToAllPotionRecipes((out, in, reagent) -> {
					if(!recipes.containsKey(out))
						recipes.put(out, toRecipe.apply(out));
				}
		);
		recipes.put(Potions.WATER, toRecipe.apply(Potions.WATER));
		IELogger.logger.info(
				"Recipes for potions: "+recipes.keySet().stream()
						.map(Potion::getRegistryName)
						.filter(Objects::nonNull)
						.map(ResourceLocation::toString)
						.collect(Collectors.joining(", "))
		);
		return new ArrayList<>(recipes.values());
	}

	public static void registerPotionRecipe(Potion output, Potion input, IngredientWithSize reagent, Map<Potion, List<MixerRecipe>> all)
	{
		if(!BLACKLIST.contains(output.getRegistryName().toString()))
		{
			List<MixerRecipe> existing = all.computeIfAbsent(output, p -> new ArrayList<>());
			ResourceLocation baseName = output.getRegistryName();
			ResourceLocation name = new ResourceLocation(baseName.getNamespace(), baseName.getPath()+"_"+existing.size());

			MixerRecipe recipe = new MixerRecipe(name, getFluidStackForType(output, FluidAttributes.BUCKET_VOLUME),
					getFluidTagForType(input, FluidAttributes.BUCKET_VOLUME), new IngredientWithSize[]{reagent}, 6400);
			existing.add(recipe);
		}
	}
}
