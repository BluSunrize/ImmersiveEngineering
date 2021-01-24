/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove Bottling Machine recipes.
 * <p>
 * Bottling Machine recipes consist of an item ingredient, a fluid input and an item output.
 *
 * @docParam this <recipetype:immersiveengineering:bottling_machine>
 */
@ZenRegister
@Document("mods/immersiveengineering/BottlingMachine")
@ZenCodeType.Name("mods.immersiveengineering.BottlingMachine")
public class BottlingMachineRecipeManager implements IRecipeManager
{

	@Override
	public IRecipeType<BottlingMachineRecipe> getRecipeType()
	{
		return BottlingMachineRecipe.TYPE;
	}

	/**
	 * Adds a recipe to the Bottling Machine.
	 * The bottling Machine only goes via Fluid tag!
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param itemInput  The item input (the item to be filled)
	 * @param fluidTag   The fluid tag of the fluid
	 * @param output     The resulting "filled" item.
	 * @docParam recipePath "grow_a_pick"
	 * @docParam itemInput <item:minecraft:stick>
	 * @docParam fluidTag <tag:minecraft:water>
	 * @docParam amount 250
	 * @docParam output <item:minecraft:wooden_pickaxe>
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredient itemInput, MCTagWithAmount<Fluid> fluidTag, IItemStack output)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);

		final FluidTagInput fluidTagInput = CrTIngredientUtil.getFluidTagInput(fluidTag);

		final ItemStack itemOutput = output.getInternal();
		final Ingredient input = itemInput.asVanillaIngredient();

		final BottlingMachineRecipe recipe = new BottlingMachineRecipe(resourceLocation, itemOutput, input, fluidTagInput);

		CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
	}
}
