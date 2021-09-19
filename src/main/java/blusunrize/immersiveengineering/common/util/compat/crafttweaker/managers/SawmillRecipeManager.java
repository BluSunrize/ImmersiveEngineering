/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionRemoveMultipleOutputs;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.helper.CraftTweakerHelper;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import org.openzen.zencode.java.ZenCodeType;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Allows you to add or remove Sawmill recipes.
 * <p>
 * Sawmill Recipes consist of an input, an optional, intermediate "stripped" output and a "cut" output.
 * <p>
 * Each step (stripping and sawing) have possible secondary outputs.
 * These won't be returned through the conveyor belt, but through the item output to the front, right next to the sawblade.
 *
 * @docParam this <recipetype:immersiveengineering:sawmill>
 */
@ZenRegister
@Document("mods/immersiveengineering/Sawmill")
@ZenCodeType.Name("mods.immersiveengineering.Sawmill")
public class SawmillRecipeManager implements IRecipeManager
{

	@Override
	public RecipeType<SawmillRecipe> getRecipeType()
	{
		return SawmillRecipe.TYPE;
	}

	@Override
	public void removeRecipe(IItemStack output)
	{
		final AbstractActionRemoveMultipleOutputs<SawmillRecipe> action = new AbstractActionRemoveMultipleOutputs<SawmillRecipe>(this, output)
		{
			@Override
			public List<ItemStack> getAllOutputs(SawmillRecipe recipe)
			{
				final List<ItemStack> itemStacks = new ArrayList<>();
				itemStacks.add(recipe.output);
				itemStacks.add(recipe.stripped);
				itemStacks.addAll(recipe.secondaryOutputs);
				itemStacks.addAll(recipe.secondaryStripping);
				return itemStacks;
			}
		};
		CraftTweakerAPI.apply(action);
	}

	/**
	 * Adds a sawmill recipe.
	 * <p>
	 * Note that the recipe only works from start to final output.<br/>
	 * So if you remove the sawblade to get the intermediate item, you need a 2nd recipe starting from the intermediate item if you later want to process that item.
	 *
	 * @param recipePath                The recipe name, without the resource location
	 * @param input                     The item input
	 * @param energy                    The total energy required
	 * @param strippedOutput            The intermediate Stripped output. Will be returned if no sawblade is present
	 * @param strippedOutputSecondaries The secondary outputs that are created while stripping. Must be empty if no intermediate output was provided.
	 * @param output                    The output that is returned when a sawblade is present
	 * @param outputSecondaries         The secondary outputs that are created alongside the `output` item
	 * @docParam recipePath "shredding_seeds"
	 * @docParam input <tag:minecraft:saplings>
	 * @docParam energy 1200
	 * @docParam strippedOutput <item:minecraft:dead_bush>
	 * @docParam strippedOutputSecondaries [<item:minecraft:grass>]
	 * @docParam output <item:minecraft:stick> * 2
	 * @docParam outputSecondaries [<item:immersiveengineering:dust_wood>]
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredient input, int energy, IItemStack strippedOutput, IItemStack[] strippedOutputSecondaries, IItemStack output, IItemStack[] outputSecondaries)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final Ingredient ingredient = input.asVanillaIngredient();

		final ItemStack stripped = strippedOutput.getInternal();
		final ItemStack[] secondaryStripping = CraftTweakerHelper.getItemStacks(strippedOutputSecondaries);

		if(stripped.isEmpty()&&strippedOutputSecondaries.length!=0)
			throw new IllegalArgumentException("Cannot have secondary stripped outputs when the main stripped output is empty!");

		final ItemStack mainOutput = output.getInternal();
		final ItemStack[] secondaryOutputs = CraftTweakerHelper.getItemStacks(outputSecondaries);

		final SawmillRecipe recipe = new SawmillRecipe(resourceLocation, mainOutput, stripped, ingredient, energy);
		for(ItemStack stack : secondaryStripping)
			recipe.addToSecondaryStripping(stack);

		for(ItemStack stack : secondaryOutputs)
			recipe.addToSecondaryOutput(stack);

		CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
	}

	/**
	 * Adds a sawmill recipe.
	 * This method is a shorter version for recipes that do not require stripping.
	 * Note that recipes without an intermediate item will do nothing if the sawmill has no sawblade.
	 *
	 * @param recipePath        The recipe name, without the resource location
	 * @param input             The item input
	 * @param energy            The total energy required
	 * @param output            The item that is returned
	 * @param outputSecondaries The secondary outputs that are created alongside the `output` item
	 * @docParam recipePath "splitting_bones"
	 * @docParam input <item:minecraft:bone_block>
	 * @docParam energy 1000
	 * @docParam output <item:minecraft:bone> * 5
	 * @docParam outputSecondaries [<item:minecraft:bone_meal> * 2]
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredient input, int energy, IItemStack output, IItemStack[] outputSecondaries)
	{
		addRecipe(recipePath, input, energy, MCItemStack.EMPTY.get(), new IItemStack[0], output, outputSecondaries);
	}
}
