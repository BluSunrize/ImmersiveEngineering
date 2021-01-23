/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionGenericRemoveRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.ActionAddRecipeCustomOutput;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove Refinery recipes.
 * <p>
 * Refinery Recipes consist of two fluid inputs and a fluid output.
 *
 * @docParam this <recipetype:immersiveengineering:refinery>
 */
@ZenRegister
@Document("mods/immersiveengineering/Refinery")
@ZenCodeType.Name("mods.immersiveengineering.Refinery")
public class RefineryRecipeManager implements IRecipeManager
{

	@Override
	public IRecipeType<RefineryRecipe> getRecipeType()
	{
		return RefineryRecipe.TYPE;
	}

	@Override
	public void removeRecipe(IItemStack output)
	{
		throw new UnsupportedOperationException("Cannot remove a refinery recipe by item output, since it only has a fluid output");
	}

	/**
	 * Removes all recipes that return this given fluid Stack.
	 * Only removes if the fluid and the fluid amount match.
	 *
	 * @param fluidStack The output to remove
	 * @docParam fluidStack <fluid:immersiveengineering:biodiesel> * 16
	 */
	@ZenCodeType.Method
	public void removeRecipe(IFluidStack fluidStack)
	{
		final AbstractActionGenericRemoveRecipe<RefineryRecipe> action = new AbstractActionGenericRemoveRecipe<RefineryRecipe>(this, fluidStack)
		{
			@Override
			public boolean shouldRemove(RefineryRecipe recipe)
			{
				return recipe.output.isFluidStackIdentical(fluidStack.getInternal());
			}
		};

		CraftTweakerAPI.apply(action);
	}

	/**
	 * Removes all recipes that return this given fluid.
	 * Since it's only the fluid, it does not check amounts.
	 *
	 * @param fluid The fluid output to remove
	 * @docParam fluid <fluid:immersiveengineering:biodiesel>.fluid
	 */
	@ZenCodeType.Method
	public void removeRecipe(Fluid fluid)
	{
		final AbstractActionGenericRemoveRecipe<RefineryRecipe> action = new AbstractActionGenericRemoveRecipe<RefineryRecipe>(this, fluid)
		{
			@Override
			public boolean shouldRemove(RefineryRecipe recipe)
			{
				return fluid.isEquivalentTo(recipe.output.getFluid());
			}
		};

		CraftTweakerAPI.apply(action);
	}

	/**
	 * Adds a recipe to the Refinery.
	 * Make sure that the provided Tags are valid fluid tags.
	 *
	 * @param recipePath  The recipe name, without the resource location
	 * @param fluidInput1 The first fluid input, as Tag
	 * @param fluidInput2 The second fluid input, as Tag
	 * @param energy      The total energy required
	 * @param output      The output fluid
	 * @docParam recipePath "refine_herbicide"
	 * @docParam fluidInput1 <tag:minecraft:water>
	 * @docParam amount1 10
	 * @docParam fluidInput2 <tag:forge:ethanol>
	 * @docParam amount2 1
	 * @docParam energy 1000
	 * @docParam output <fluid:immersiveengineering:herbicide> * 10
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, MCTagWithAmount<Fluid> fluidInput1, MCTagWithAmount<Fluid> fluidInput2, int energy, IFluidStack output)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final FluidStack outputStack = output.getInternal();

		final FluidTagInput tagInput1 = CrTIngredientUtil.getFluidTagInput(fluidInput1.getTag(), fluidInput1.getAmount());
		final FluidTagInput tagInput2 = CrTIngredientUtil.getFluidTagInput(fluidInput2.getTag(), fluidInput2.getAmount());

		final RefineryRecipe recipe = new RefineryRecipe(resourceLocation, outputStack, tagInput1, tagInput2, energy);
		CraftTweakerAPI.apply(new ActionAddRecipeCustomOutput(this, recipe, output));
	}
}
