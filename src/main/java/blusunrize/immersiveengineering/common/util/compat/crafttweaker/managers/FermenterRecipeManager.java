/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionGenericRemoveRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.ActionAddRecipeCustomOutput;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStackMutable;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove Fermenter recipes.
 * <p>
 * Fermenter Recipes consist of an input, a fluid output and an item output either fluid or item output can be empty.
 *
 * @docParam this <recipetype:immersiveengineering:fermenter>
 */
@ZenRegister
@Document("mods/immersiveengineering/Fermenter")
@ZenCodeType.Name("mods.immersiveengineering.Fermenter")
public class FermenterRecipeManager implements IRecipeManager
{

	@Override
	public IRecipeType<FermenterRecipe> getRecipeType()
	{
		return FermenterRecipe.TYPE;
	}

	/**
	 * Adds a Fermenter recipe.
	 * You need to provide an item output, a fluid output, or both
	 *
	 * @param recipePath  The recipe name, without the resource location
	 * @param input       The recipe's input
	 * @param energy      The total energy required for this recipe
	 * @param itemOutput  The item output (can be empty)
	 * @param fluidOutput The fluid output (can be empty)
	 * @docParam recipePath "fermenter_upgrade_sword"
	 * @docParam input <item:minecraft:wooden_sword>
	 * @docParam energy 1000
	 * @docParam itemOutput <item:minecraft:stone_sword>
	 * @docParam fluidOutput <fluid:minecraft:water> * 100
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredientWithAmount input, int energy, IItemStack itemOutput, IFluidStack fluidOutput)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final IngredientWithSize ingredient = CrTIngredientUtil.getIngredientWithSize(input);
		final FluidStack fluidStack = fluidOutput.getInternal();
		final ItemStack outputItem = itemOutput.getInternal();

		final FermenterRecipe recipe = new FermenterRecipe(resourceLocation, fluidStack, outputItem, ingredient, energy);
		final String outputDescription = String.format("%s and %s", itemOutput.getCommandString(), fluidOutput.getCommandString());
		CraftTweakerAPI.apply(new ActionAddRecipeCustomOutput(this, recipe, outputDescription));
	}

	/**
	 * Adds a Fermenter recipe.
	 * The overload for only the fluid Output
	 *
	 * @param recipePath  The recipe name, without the resource location
	 * @param input       The recipe's input
	 * @param energy      The total energy required for this recipe
	 * @param fluidOutput The fluid output (can be empty)
	 * @docParam recipePath "fermenter_extract_water"
	 * @docParam input <item:minecraft:wooden_hoe>
	 * @docParam energy 1000
	 * @docParam fluidOutput <fluid:minecraft:water> * 100
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredientWithAmount input, int energy, IFluidStack fluidOutput)
	{
		addRecipe(recipePath, input, energy, MCItemStack.EMPTY.get(), fluidOutput);
	}

	/**
	 * Adds a Fermenter recipe.
	 * The overload for only the item output
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param input      The recipe's input
	 * @param energy     The total energy required for this recipe
	 * @param itemOutput The item output (can be empty)
	 * @docParam recipePath "fermenter_upgrade_hoe"
	 * @docParam input <item:minecraft:wooden_shovel>
	 * @docParam energy 1000
	 * @docParam itemOutput <item:minecraft:stone_shovel>
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredientWithAmount input, int energy, IItemStack itemOutput)
	{
		addRecipe(recipePath, input, energy, itemOutput, new MCFluidStackMutable(FluidStack.EMPTY));
	}

	/**
	 * Removes all recipes that return the given output fluid.
	 * Since it uses a fluid and not a fluidStack it does not compare stack sizes
	 *
	 * @param outputFluid The fluid to remove
	 * @docParam outputFluid <fluid:immersiveengineering:ethanol>.fluid
	 */
	@ZenCodeType.Method
	public void removeRecipe(Fluid outputFluid)
	{
		CraftTweakerAPI.apply(new AbstractActionGenericRemoveRecipe<FermenterRecipe>(this, outputFluid)
		{
			@Override
			public boolean shouldRemove(FermenterRecipe recipe)
			{
				return outputFluid.isEquivalentTo(recipe.fluidOutput.getFluid());
			}
		});
	}

	/**
	 * Removes all recipes that return the given fluidStack.
	 * Takes stack sizes into account!
	 *
	 * @param output The fluid to remove
	 * @docParam output <fluid:immersiveengineering:ethanol> * 80
	 */
	@ZenCodeType.Method
	public void removeRecipe(IFluidStack output)
	{

		CraftTweakerAPI.apply(new AbstractActionGenericRemoveRecipe<FermenterRecipe>(this, output)
		{
			@Override
			public boolean shouldRemove(FermenterRecipe recipe)
			{
				return output.getInternal().isFluidStackIdentical(recipe.fluidOutput);
			}
		});
	}
}
