/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove alloy smelter recipes.
 * <p>
 * Alloy smelter recipes consist of two input ingredients (size dependent) and one output ItemStack
 *
 * @docParam this <recipetype:immersiveengineering:alloy>
 */
@ZenRegister
@Document("mods/immersiveengineering/AlloySmelter")
@ZenCodeType.Name("mods.immersiveengineering.AlloySmelter")
public class AlloyRecipeManager implements IRecipeManager
{

	@Override
	public IRecipeType<AlloyRecipe> getRecipeType()
	{
		return AlloyRecipe.TYPE;
	}

	/**
	 * Adds a recipe to the alloy smelter
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param inputA     The first item input
	 * @param inputB     The second item input
	 * @param output     The recipe output
	 * @param time       The time this recipe needs, in ticks
	 * @docParam recipePath "spin_iron_to_gold"
	 * @docParam inputA <item:minecraft:iron_ingot> * 10
	 * @docParam inputB <tag:minecraft:wool>
	 * @docParam output <item:minecraft:gold_ingot> * 2
	 * @docParam time 200
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredient inputA, IIngredient inputB, int time, IItemStack output)
	{
		final ResourceLocation id = new ResourceLocation("crafttweaker", recipePath);
		final IngredientWithSize input0 = CrTIngredientUtil.getIngredientWithSize(inputA);
		final IngredientWithSize input1 = CrTIngredientUtil.getIngredientWithSize(inputB);
		final AlloyRecipe alloyRecipe = new AlloyRecipe(id, output.getInternal(), input0, input1, time);

		CraftTweakerAPI.apply(new ActionAddRecipe(this, alloyRecipe, null));
	}
}
