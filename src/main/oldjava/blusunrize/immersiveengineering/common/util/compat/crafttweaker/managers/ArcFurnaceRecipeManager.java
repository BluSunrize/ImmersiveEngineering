/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionRemoveMultipleOutputs;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.util.Lazy;
import org.openzen.zencode.java.ZenCodeType;

import java.util.ArrayList;
import java.util.List;

import static blusunrize.immersiveengineering.api.crafting.IESerializableRecipe.of;

/**
 * Allows you to add or remove arc furnace smelter recipes.
 * <p>
 * Arc Furnace recipes consist of one base ingredident, a list of additives, and a list of outputs.
 * Optionally, they can also have an item as slag output.
 *
 * @docParam this <recipetype:immersiveengineering:arc_furnace>
 */
@ZenRegister
@Document("mods/immersiveengineering/ArcFurnace")
@ZenCodeType.Name("mods.immersiveengineering.ArcFurnace")
public class ArcFurnaceRecipeManager implements IRecipeManager<ArcFurnaceRecipe>
{

	@Override
	public RecipeType<ArcFurnaceRecipe> getRecipeType()
	{
		return IERecipeTypes.ARC_FURNACE.get();
	}

	/**
	 * Adds a recipe to the Arc Furnace
	 *
	 * @param recipePath     The recipe name, without the resource location
	 * @param mainIngredient The main ingredient
	 * @param additives      The additives
	 * @param time           The time the recipe takes, in ticks
	 * @param energy         The total energy the recipe requires
	 * @param outputs        The recipe result(s)
	 * @param slag           The item that should appear as slag
	 * @docParam recipePath "coal_to_bedrock"
	 * @docParam mainIngredient <item:minecraft:coal_block> * 2
	 * @docParam additives [<item:minecraft:diamond> * 1, <tag:minecraft:wool>]
	 * @docParam time 2000
	 * @docParam energy 100000
	 * @docParam outputs [<item:minecraft:bedrock>]
	 * @docParam slag <item:minecraft:gold_nugget>
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredientWithAmount mainIngredient, IIngredientWithAmount[] additives, int time, int energy, IItemStack[] outputs, @ZenCodeType.Optional("<item:minecraft:air>") IItemStack slag)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final List<Lazy<ItemStack>> outputList = CrTIngredientUtil.getNonNullList(outputs);
		final IngredientWithSize main = CrTIngredientUtil.getIngredientWithSize(mainIngredient);
		final IngredientWithSize[] additivesWithSize = CrTIngredientUtil.getIngredientsWithSize(additives);

		final ArcFurnaceRecipe recipe = IEServerConfig.MACHINES.arcFurnaceConfig.apply(
				new ArcFurnaceRecipe(resourceLocation, outputList, of(slag.getInternal()), List.of(), time, energy, main, additivesWithSize)
		);

		CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe, null));
	}

	@Override
	public void remove(IIngredient output)
	{
		remove(output, false);
	}

	/**
	 * Removes a recipe based on its outputs.
	 * Removes the recipe as long as one of the recipe's outputs matches the ingredient given.
	 *
	 * @param output    The recipe result
	 * @param checkSlag If the slag output should be included in the check or not
	 * @docParam output <item:minecraft:iron_ore>
	 * @docParam checkSlag true
	 */
	@ZenCodeType.Method
	public void remove(IIngredient output, boolean checkSlag)
	{
		CraftTweakerAPI.apply(new AbstractActionRemoveMultipleOutputs<>(this, output)
		{
			@Override
			public List<ItemStack> getAllOutputs(ArcFurnaceRecipe recipe)
			{
				final List<ItemStack> itemStacks = new ArrayList<>(recipe.output.get());
				if(checkSlag)
					itemStacks.add(recipe.slag.get());
				return itemStacks;
			}

			@Override
			public String describe()
			{
				return super.describe()+", "+(checkSlag?"including": "excluding")+" slag outputs";
			}
		});
	}
}
