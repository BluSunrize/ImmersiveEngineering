/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionGenericRemoveRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.ActionAddRecipeCustomOutput;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.tag.MCTag;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove Mixer recipes.
 * <p>
 * Mixer Recipes consist of a fluid input, multiple item inputs and a fluid output.
 *
 * @docParam this <recipetype:immersiveengineering:mixer>
 */
@ZenRegister
@Document("mods/immersiveengineering/Mixer")
@ZenCodeType.Name("mods.immersiveengineering.Mixer")
public class MixerRecipeManager implements IRecipeManager<MixerRecipe>
{

	@Override
	public RecipeType<MixerRecipe> getRecipeType()
	{
		return IERecipeTypes.MIXER.get();
	}

	@Override
	public void remove(IIngredient output)
	{
		throw new UnsupportedOperationException("Cannot remove a Mixer recipe by Item output because Mixer Recipes have no Item output!");
	}

	/**
	 * Removes all recipes that return this given fluid Stack.
	 * Only removes if the fluid and the fluid amount match.
	 * Does not remove potion recipes!
	 *
	 * @param fluidStack The output to remove
	 * @docParam fluidStack <fluid:immersiveengineering:concrete> * 500
	 */
	@ZenCodeType.Method
	public void removeRecipe(IFluidStack fluidStack)
	{
		CraftTweakerAPI.apply(new AbstractActionGenericRemoveRecipe<>(this, fluidStack)
		{
			@Override
			public boolean shouldRemove(MixerRecipe recipe)
			{
				return recipe.fluidOutput.isFluidStackIdentical(fluidStack.getInternal());
			}
		});
	}

	/**
	 * Removes all recipes that return this given fluid.
	 * Since it's only the fluid, it does not check amounts.
	 * Does not remove potion recipes!
	 *
	 * @param fluid The fluid output to remove
	 * @docParam fluid <fluid:immersiveengineering:concrete>.fluid
	 */
	@ZenCodeType.Method
	public void removeRecipe(Fluid fluid)
	{
		CraftTweakerAPI.apply(new AbstractActionGenericRemoveRecipe<>(this, fluid)
		{
			@Override
			public boolean shouldRemove(MixerRecipe recipe)
			{
				return fluid.isSame(recipe.fluidOutput.getFluid());
			}
		});
	}

	/**
	 * Adds a recipe to the Mixer.
	 * Make sure that the provided Tag is a valid fluid tag.
	 * <p>
	 * Mixer recipes will always convert 1mB of the input fluid to 1mB of the output fluid.
	 * The `amount` parameter specifies for how many mB the given ingredients last
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param fluidInput The fluid input as Tag
	 * @param inputItems The required input items
	 * @param energy     The total energy required
	 * @param output     The produced output fluidStack
	 * @param amount     The amount of fluid that can be converted per set of input items (in mB)
	 * @docParam recipePath "grow_creosote_oil"
	 * @docParam fluidInput <tag:minecraft:water>
	 * @docParam inputItems [<item:minecraft:oak_sapling>, <item:minecraft:bone_meal> * 4, <item:immersiveengineering:creosote_bucket>]
	 * @docParam energy 5000
	 * @docParam output <fluid:immersiveengineering:creosote>.fluid
	 * @docParam amount 8000
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, MCTag fluidInput, IIngredientWithAmount[] inputItems, int energy, Fluid output, int amount)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);

		final FluidTagInput fluidTagInput = CrTIngredientUtil.getFluidTagInput(fluidInput, amount);
		final IngredientWithSize[] ingredientsWithSize = CrTIngredientUtil.getIngredientsWithSize(inputItems);
		final FluidStack outputFluidStack = new FluidStack(output, amount);

		final MixerRecipe recipe = new MixerRecipe(resourceLocation, outputFluidStack, fluidTagInput, ingredientsWithSize, energy);
		CraftTweakerAPI.apply(new ActionAddRecipeCustomOutput<>(this, recipe, outputFluidStack));
	}
}
