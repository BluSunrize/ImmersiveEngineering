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
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author BluSunrize - 22.02.2017
 */
public class MixerRecipePotion extends MixerRecipe
{
	public static final HashMap<Potion, MixerRecipePotion> REGISTERED = new HashMap<>();
	public static final Set<String> BLACKLIST = new HashSet<>();
	private final Set<Pair<FluidStack, IngredientWithSize[]>> alternateInputs = new HashSet<>();

	public MixerRecipePotion(Potion outputType, Potion inputType, IngredientWithSize reagent)
	{
		super(getFluidStackForType(outputType, 1000), getFluidStackForType(inputType, 1000), new IngredientWithSize[]{reagent}, 6400);
	}

	public void addAlternateInput(Potion inputType, IngredientWithSize reagent)
	{
		alternateInputs.add(Pair.of(getFluidStackForType(inputType, 1000), new IngredientWithSize[]{reagent}));
	}

	public Set<Pair<FluidStack, IngredientWithSize[]>> getAlternateInputs()
	{
		return alternateInputs;
	}

	public static void registerPotionRecipe(Potion output, Potion input, IngredientWithSize reagent)
	{
		if(REGISTERED.containsKey(output))
		{
			MixerRecipePotion recipe = REGISTERED.get(output);
			recipe.addAlternateInput(input, reagent);
		}
		else if(!BLACKLIST.contains(output.getRegistryName().toString()))
		{
			MixerRecipePotion recipe = new MixerRecipePotion(output, input, reagent);
			MixerRecipe.recipeList.add(recipe);
			REGISTERED.put(output, recipe);

			BottlingMachineRecipe.addRecipe(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), output),
					Ingredient.fromItems(Items.GLASS_BOTTLE), getFluidStackForType(output, 250));
		}
	}

	public static FluidStack getFluidStackForType(Potion type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidStack(Fluids.WATER, amount);
		FluidStack stack = new FluidStack(IEContent.fluidPotion, amount);
		stack.getOrCreateTag().putString("Potion", type.getRegistryName().toString());
		return stack;
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
