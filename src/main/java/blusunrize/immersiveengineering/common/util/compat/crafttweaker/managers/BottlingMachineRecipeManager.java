/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.tag.MCTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.util.Lazy;
import org.openzen.zencode.java.ZenCodeType;

import java.util.List;

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
public class BottlingMachineRecipeManager implements IRecipeManager<BottlingMachineRecipe>
{

	@Override
	public RecipeType<BottlingMachineRecipe> getRecipeType()
	{
		return IERecipeTypes.BOTTLING_MACHINE.get();
	}

	/**
	 * Adds a recipe to the Bottling Machine.
	 * The bottling Machine only goes via Fluid tag!
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param itemInput  The item input (the item to be filled)
	 * @param fluidTag   The fluid tag of the fluid
	 * @param outputs     The resulting "filled" items.
	 * @docParam recipePath "grow_a_pick"
	 * @docParam itemInput <item:minecraft:stick>
	 * @docParam fluidTag <tag:minecraft:water>
	 * @docParam amount 250
	 * @docParam output <item:minecraft:wooden_pickaxe>
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredient itemInput, Many<MCTag> fluidTag, IItemStack[] outputs)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);

		final FluidTagInput fluidTagInput = CrTIngredientUtil.getFluidTagInput(fluidTag);

		final List<Lazy<ItemStack>> outputList = CrTIngredientUtil.getNonNullList(outputs);
		final Ingredient input = itemInput.asVanillaIngredient();

		final BottlingMachineRecipe recipe = new BottlingMachineRecipe(
				resourceLocation, outputList, input, fluidTagInput
		);

		CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe, null));
	}
}
