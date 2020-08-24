/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.registries.IRegistryDelegate;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author BluSunrize - 22.02.2017
 */
public class MixerRecipePotion extends MixerRecipe
{
	public static final HashMap<Potion, MixerRecipePotion> REGISTERED = new HashMap<>();
	public static final Set<String> BLACKLIST = new HashSet<>();
	private final Set<Pair<FluidTagInput, IngredientWithSize[]>> alternateInputs = new HashSet<>();

	public MixerRecipePotion(ResourceLocation id, Potion outputType, Potion inputType, IngredientWithSize reagent)
	{
		super(id, getFluidStackForType(outputType, 1000), getFluidTagForType(inputType, 1000), new IngredientWithSize[]{reagent}, 6400);
	}

	public void addAlternateInput(Potion inputType, IngredientWithSize reagent)
	{
		alternateInputs.add(Pair.of(getFluidTagForType(inputType, 1000), new IngredientWithSize[]{reagent}));
	}

	public Set<Pair<FluidTagInput, IngredientWithSize[]>> getAlternateInputs()
	{
		return alternateInputs;
	}

	public static void initPotionRecipes()
	{
		REGISTERED.clear();
		// Vanilla
		try
		{
			String mixPredicateName = "net.minecraft.potion.PotionBrewing$MixPredicate";
			Class<PotionBrewing> mixPredicateClass = (Class<PotionBrewing>)Class.forName(mixPredicateName);
			Field f_input = ObfuscationReflectionHelper.findField(mixPredicateClass, "field_185198_a");
			Field f_reagent = ObfuscationReflectionHelper.findField(mixPredicateClass, "field_185199_b");
			Field f_output = ObfuscationReflectionHelper.findField(mixPredicateClass, "field_185200_c");
			f_input.setAccessible(true);
			f_reagent.setAccessible(true);
			f_output.setAccessible(true);
			List l = PotionBrewing.POTION_TYPE_CONVERSIONS;
			for(Object mixPredicate : PotionBrewing.POTION_TYPE_CONVERSIONS)
			{
				Ingredient reagent = (Ingredient)f_reagent.get(mixPredicate);
				IRegistryDelegate<Potion> input = (IRegistryDelegate<Potion>)f_input.get(mixPredicate);
				IRegistryDelegate<Potion> output = (IRegistryDelegate<Potion>)f_output.get(mixPredicate);
				registerPotionRecipe(output.get(), input.get(), new IngredientWithSize(reagent));
			}

		} catch(Exception e)
		{
			IELogger.error("Error when trying to figure out vanilla potion recipes", e);
		}

		// Modded
		for(IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes())
			if(recipe instanceof BrewingRecipe)
			{
				IngredientWithSize ingredient = new IngredientWithSize(((BrewingRecipe)recipe).getIngredient());
				Ingredient input = ((BrewingRecipe)recipe).getInput();
				ItemStack output = ((BrewingRecipe)recipe).getOutput();
				if(output.getItem()==Items.POTION)
					registerPotionRecipe(PotionUtils.getPotionFromItem(output),
							PotionUtils.getPotionFromItem(input.getMatchingStacks()[0]), ingredient);
			}
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
			MixerRecipePotion recipe = new MixerRecipePotion(output.getRegistryName(), output, input, reagent);
			MixerRecipe.recipeList.put(recipe.getId(), recipe);
			REGISTERED.put(output, recipe);

			BottlingMachineRecipe bottling = new BottlingMachineRecipe(output.getRegistryName(),
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), output),
					Ingredient.fromItems(Items.GLASS_BOTTLE), getFluidTagForType(output, 250));
			BottlingMachineRecipe.recipeList.put(bottling.getId(), bottling);
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

	public static FluidTagInput getFluidTagForType(Potion type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidTagInput(FluidTags.WATER.getName(), amount);
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("Potion", type.getRegistryName().toString());
		//TODO this is a workaround, we should probably be syncing the potion recipes along with everything else
		if(EffectiveSide.get().isServer())
			return new FluidTagInput(IETags.fluidPotion.getName(), amount, nbt);
		else
			return new FluidTagInput(
					Either.right(ImmutableList.of(IEContent.fluidPotion.getRegistryName())),
					amount,
					nbt
			);
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
