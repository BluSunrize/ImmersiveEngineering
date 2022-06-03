/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionGenericRemoveRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.ActionAddRecipeCustomOutput;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.fluid.MCFluidStackMutable;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove Squeezer recipes.
 * <p>
 * Squeezer Recipes consist of an input, a fluid output and an item output.
 *
 * @docParam this <recipetype:immersiveengineering:squeezer>
 */
@ZenRegister
@Document("mods/immersiveengineering/Squeezer")
@ZenCodeType.Name("mods.immersiveengineering.Squeezer")
public class SqueezerRecipeManager implements IRecipeManager<SqueezerRecipe>
{

	@Override
	public RecipeType<SqueezerRecipe> getRecipeType()
	{
		return IERecipeTypes.SQUEEZER.get();
	}

	/**
	 * Removes all recipes that return this given fluid Stack.
	 * Only removes if the fluid and the fluid amount match.
	 *
	 * @param fluidStack The output to remove
	 * @docParam fluidStack <fluid:immersiveengineering:plantoil> * 60
	 */
	@ZenCodeType.Method
	public void remove(IFluidStack fluidStack)
	{
		CraftTweakerAPI.apply(new AbstractActionGenericRemoveRecipe<>(this, fluidStack)
		{
			@Override
			public boolean shouldRemove(SqueezerRecipe recipe)
			{
				return recipe.fluidOutput.isFluidStackIdentical(fluidStack.getInternal());
			}
		});
	}

	/**
	 * Removes all recipes that return this given fluid.
	 * Since it's only the fluid, it does not check amounts.
	 *
	 * @param fluid The fluid output to remove
	 * @docParam fluid <fluid:immersiveengineering:plantoil>.fluid
	 */
	@ZenCodeType.Method
	public void remove(Fluid fluid)
	{
		CraftTweakerAPI.apply(new AbstractActionGenericRemoveRecipe<>(this, fluid)
		{
			@Override
			public boolean shouldRemove(SqueezerRecipe recipe)
			{
				return fluid.isSame(recipe.fluidOutput.getFluid());
			}
		});
	}

	/**
	 * Adds a recipe to the Squeezer.
	 * The item output is optional.
	 *
	 * @param recipePath  The recipe name, without the resource location
	 * @param input       The input item
	 * @param energy      The total energy required for this recipe
	 * @param fluidOutput The fluid output
	 * @param itemOutput  The item output
	 * @docParam recipePath "pressure_creates_diamonds"
	 * @docParam input <item:minecraft:coal_block> * 8
	 * @docParam energy 6000
	 * @docParam fluidOutput <fluid:immersiveengineering:creosote> * 2500
	 * @docParam itemOutput <item:minecraft:diamond>
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredientWithAmount input, int energy, IFluidStack fluidOutput, @ZenCodeType.Optional("<item:minecraft:air>") IItemStack itemOutput)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final IngredientWithSize inputWithSize = CrTIngredientUtil.getIngredientWithSize(input);
		final FluidStack fluidOut = fluidOutput.getInternal();
		final ItemStack itemOut = itemOutput.getInternal();

		final SqueezerRecipe recipe = new SqueezerRecipe(
				resourceLocation, fluidOut, IESerializableRecipe.of(itemOut), inputWithSize, energy
		);

		final String outputDescription = String.format("%s and %s", fluidOutput.getCommandString(), itemOutput.getCommandString());
		CraftTweakerAPI.apply(new ActionAddRecipeCustomOutput<>(this, recipe, outputDescription));
	}

	/**
	 * Adds a recipe to the Squeezer.
	 * Short form if you don't want a fluid output.
	 * Does the same as if you provided `<fluid:minecraft:empty> * 0` to the other addRecipe Method.
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param input      The input item
	 * @param energy     The total energy required for this recipe
	 * @param itemOutput The item output
	 * @docParam recipePath "slag_off"
	 * @docParam input <item:immersiveengineering:slag> * 9
	 * @docParam energy 5000
	 * @docParam itemOutput <item:minecraft:dirt>
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredientWithAmount input, int energy, IItemStack itemOutput)
	{
		addRecipe(recipePath, input, energy, new MCFluidStackMutable(FluidStack.EMPTY), itemOutput);
	}
}
