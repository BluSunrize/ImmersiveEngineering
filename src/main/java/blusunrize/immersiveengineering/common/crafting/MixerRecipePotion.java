/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static blusunrize.immersiveengineering.common.crafting.PotionHelper.getFluidStackForType;
import static blusunrize.immersiveengineering.common.crafting.PotionHelper.getFluidTagForType;

/**
 * @author BluSunrize - 22.02.2017
 */
public class MixerRecipePotion extends MixerRecipe
{
	public static final Set<String> BLACKLIST = new HashSet<>();
	private final Set<Pair<FluidTagInput, IngredientWithSize[]>> alternateInputs = new HashSet<>();

	public MixerRecipePotion(ResourceLocation id, Potion outputType, Potion inputType, IngredientWithSize reagent)
	{
		this(id, getFluidStackForType(outputType, 1000), getFluidTagForType(inputType, 1000),
				new IngredientWithSize[]{reagent});
	}

	public MixerRecipePotion(ResourceLocation id, FluidStack outputType, FluidTagInput inputType, IngredientWithSize[] reagent)
	{
		super(id, outputType, inputType, reagent, 6400);
	}

	public void addAlternateInput(Potion inputType, IngredientWithSize reagent)
	{
		addAlternateInput(getFluidTagForType(inputType, 1000), reagent);
	}

	public void addAlternateInput(FluidTagInput inputType, IngredientWithSize reagent)
	{
		alternateInputs.add(Pair.of(inputType, new IngredientWithSize[]{reagent}));
	}

	public Set<Pair<FluidTagInput, IngredientWithSize[]>> getAlternateInputs()
	{
		return alternateInputs;
	}

	public static List<MixerRecipe> initPotionRecipes()
	{
		Map<Potion, MixerRecipePotion> recipes = new HashMap<>();
		PotionHelper.applyToAllPotionRecipes((out, in, reagent) -> registerPotionRecipe(out, in, reagent, recipes));
		return new ArrayList<>(recipes.values());
	}

	public static void registerPotionRecipe(Potion output, Potion input, IngredientWithSize reagent, Map<Potion, MixerRecipePotion> all)
	{
		MixerRecipePotion existing = all.get(output);
		if(existing!=null)
			existing.addAlternateInput(input, reagent);
		else if(!BLACKLIST.contains(output.getRegistryName().toString()))
		{
			ResourceLocation name = output.getRegistryName();

			MixerRecipePotion recipe = new MixerRecipePotion(name, output, input, reagent);
			all.put(output, recipe);
		}
	}

	@Override
	public boolean matches(FluidStack fluid, NonNullList<ItemStack> components)
	{
		if(super.matches(fluid, components))
			return true;
		return this.alternateInputs.stream().anyMatch(alternate -> this.compareToInputs(fluid, components, alternate.getLeft(), alternate.getRight()));
	}

	@Override
	public int[] getUsedSlots(FluidStack fluid, NonNullList<ItemStack> components)
	{
		for(int i = 0; i < components.size(); i++)
			if(!components.get(i).isEmpty()&&BrewingRecipeRegistry.isValidIngredient(components.get(i)))
				return new int[]{i};
		return new int[0];
	}
}
