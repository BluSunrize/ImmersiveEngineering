/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fluids.FluidRegistry;
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
	public static final HashMap<PotionType, MixerRecipePotion> REGISTERED = new HashMap<>();
	public static final Set<String> BLACKLIST = new HashSet<>();
	private final Set<Pair<FluidStack, IngredientStack[]>> alternateInputs = new HashSet<>();

	public MixerRecipePotion(PotionType outputType, PotionType inputType, IngredientStack reagent)
	{
		super(getFluidStackForType(outputType, 1000), getFluidStackForType(inputType, 1000), new IngredientStack[]{reagent}, 6400);
	}

	public void addAlternateInput(PotionType inputType, IngredientStack reagent)
	{
		alternateInputs.add(Pair.of(getFluidStackForType(inputType, 1000), new IngredientStack[]{reagent}));
	}

	public Set<Pair<FluidStack, IngredientStack[]>> getAlternateInputs()
	{
		return alternateInputs;
	}

	public static void registerPotionRecipe(PotionType output, PotionType input, IngredientStack reagent)
	{
		if(REGISTERED.containsKey(output))
		{
			MixerRecipePotion recipe = REGISTERED.get(output);
			recipe.addAlternateInput(input, reagent);
		}
		else if(!BLACKLIST.contains(PotionType.REGISTRY.getNameForObject(output).toString()))
		{
			MixerRecipePotion recipe = new MixerRecipePotion(output, input, reagent);
			MixerRecipe.recipeList.add(recipe);
			REGISTERED.put(output, recipe);

			BottlingMachineRecipe.addRecipe(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), output),
					new ItemStack(Items.GLASS_BOTTLE), getFluidStackForType(output, 250));
		}
	}

	public static FluidStack getFluidStackForType(PotionType type, int amount)
	{
		if(type==PotionTypes.WATER||type==null)
			return new FluidStack(FluidRegistry.WATER, amount);
		FluidStack stack = new FluidStack(IEContent.fluidPotion, amount);
		stack.tag = new NBTTagCompound();
		stack.tag.setString("Potion", PotionType.REGISTRY.getNameForObject(type).toString());
		return stack;
	}

	@Override
	public FluidStack getFluidOutput(FluidStack input, NonNullList<ItemStack> components)
	{
//		if(components.size()!=1)
//			return input;
//		if(input!=null)
//			for(PotionHelper.MixPredicate<PotionType> mixPredicate : PotionHelper.POTION_TYPE_CONVERSIONS)
//				if(mixPredicate.input==this.inputPotionType&&mixPredicate.reagent.apply(components.get(0)))
//					return getFluidStackForType(mixPredicate.output, input.amount);
//		return input;
		return super.getFluidOutput(input, components);
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
