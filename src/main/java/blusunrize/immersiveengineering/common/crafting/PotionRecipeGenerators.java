/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import blusunrize.immersiveengineering.common.fluids.PotionFluid.PotionBottleType;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static blusunrize.immersiveengineering.common.crafting.PotionHelper.getFluidIngredientForType;
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
		Map<Holder<Potion>, BottlingMachineRecipe[]> bottleRecipes = new HashMap<>();
		Function<Holder<Potion>, BottlingMachineRecipe[]> toBottleRecipe = potion -> new BottlingMachineRecipe[]{
				new BottlingMachineRecipe(
						new TagOutputList(new TagOutput(PotionContents.createItemStack(Items.POTION, potion))),
						IngredientWithSize.of(new ItemStack(Items.GLASS_BOTTLE)),
						getFluidIngredientForType(potion, 250, PotionFluid.PotionBottleType.REGULAR)
				),
				new BottlingMachineRecipe(
						new TagOutputList(new TagOutput(PotionContents.createItemStack(Items.SPLASH_POTION, potion))),
						IngredientWithSize.of(new ItemStack(Items.GLASS_BOTTLE)),
						getFluidIngredientForType(potion, 250, PotionFluid.PotionBottleType.SPLASH)
				),
				new BottlingMachineRecipe(
						new TagOutputList(new TagOutput(PotionContents.createItemStack(Items.LINGERING_POTION, potion))),
						IngredientWithSize.of(new ItemStack(Items.GLASS_BOTTLE)),
						getFluidIngredientForType(potion, 250, PotionFluid.PotionBottleType.LINGERING)
				)
		};

		Map<Holder<Potion>, BottlingMachineRecipe> bulletRecipes = new HashMap<>();
		Function<Holder<Potion>, BottlingMachineRecipe> toBulletRecipe = potion -> {
			var potionBulletItem = BulletHandler.getBulletItem(IEBullets.POTION);
			return new BottlingMachineRecipe(
					new TagOutputList(new TagOutput(PotionContents.createItemStack(potionBulletItem, potion))),
					new IngredientWithSize(Ingredient.of(potionBulletItem)),
					getFluidIngredientForType(potion, 250, PotionFluid.PotionBottleType.REGULAR)
			);
		};

		PotionHelper.applyToAllPotionRecipes((out, in, reagent) -> {
			if(!bottleRecipes.containsKey(out))
				bottleRecipes.put(out, toBottleRecipe.apply(out));
			if(!bulletRecipes.containsKey(out))
				bulletRecipes.put(out, toBulletRecipe.apply(out));
		});
		bottleRecipes.put(Potions.WATER, toBottleRecipe.apply(Potions.WATER));
		IELogger.logger.info(
				"Recipes for potions: "+bottleRecipes.keySet().stream()
						.map(h -> h.unwrapKey().orElseThrow().location().toString())
						.collect(Collectors.joining(", "))
		);
		;

		List<BottlingMachineRecipe> ret = new ArrayList<>(
				bottleRecipes.values().stream()
						.flatMap((Function<BottlingMachineRecipe[], Stream<BottlingMachineRecipe>>)Arrays::stream)
						.toList()
		);
		ret.addAll(bulletRecipes.values());
		return ret;
	}

	public static void registerPotionRecipe(
			Holder<Potion> output, Holder<Potion> input, IngredientWithSize reagent, Map<Potion, List<MixerRecipe>> all
	)
	{
		ResourceLocation outputID = output.unwrapKey().orElseThrow().location();
		if(!BLACKLIST.contains(outputID.toString()))
		{
			List<MixerRecipe> existing = all.computeIfAbsent(output.value(), p -> new ArrayList<>());
			// base recipe
			for(PotionBottleType bottle : PotionFluid.PotionBottleType.values())
				existing.add(new MixerRecipe(
						getFluidStackForType(Optional.of(output), FluidType.BUCKET_VOLUME, bottle),
						getFluidIngredientForType(input, FluidType.BUCKET_VOLUME, bottle), List.of(reagent), 6400)
				);
			// bottle changes
			existing.add(new MixerRecipe(
					getFluidStackForType(Optional.of(output), FluidType.BUCKET_VOLUME, PotionFluid.PotionBottleType.SPLASH),
					getFluidIngredientForType(output, FluidType.BUCKET_VOLUME, PotionFluid.PotionBottleType.REGULAR),
					List.of(new IngredientWithSize(Tags.Items.GUNPOWDERS)), 6400
			));
			existing.add(new MixerRecipe(
					getFluidStackForType(Optional.of(output), FluidType.BUCKET_VOLUME, PotionFluid.PotionBottleType.LINGERING),
					getFluidIngredientForType(output, FluidType.BUCKET_VOLUME, PotionFluid.PotionBottleType.SPLASH),
					List.of(IngredientWithSize.of(Items.DRAGON_BREATH.getDefaultInstance())), 6400
			));
		}
	}
}
