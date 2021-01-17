/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionGenericRemoveRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.ActionAddRecipeCustomOutput;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove Fertilizers from the Garden Cloche
 * <p>
 * A Fertilizer consists of an ingredient and a fertilizer value
 *
 * @docParam this <recipetype:immersiveengineering:fertilizer>
 */
@ZenRegister
@Document("mods/immersiveengineering/Fertilizer")
@ZenCodeType.Name("mods.immersiveengineering.Fertilizer")
public class ClocheFertilizerManager implements IRecipeManager
{

	@Override
	public IRecipeType<ClocheFertilizer> getRecipeType()
	{
		return ClocheFertilizer.TYPE;
	}

	/**
	 * Adds the fertilizer as possible fertilizer
	 *
	 * @param recipePath      The recipe name, without the resource location
	 * @param fertilizer      The fertilizer to be added
	 * @param fertilizerValue The value this fertilizer gives in the garden cloche
	 * @docParam recipePath "sulfur_grow"
	 * @docParam fertilizer <tag:forge:dusts/sulfur>
	 * @docParam fertilizerValue 6.0F
	 */
	@ZenCodeType.Method
	public void addFertilizer(String recipePath, IIngredient fertilizer, float fertilizerValue)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final ClocheFertilizer recipe = new ClocheFertilizer(resourceLocation, fertilizer.asVanillaIngredient(), fertilizerValue);
		CraftTweakerAPI.apply(new ActionAddRecipeCustomOutput(this, recipe, fertilizer));
	}

	/**
	 * Removes a given fertilizer.
	 * Will remove all fertilizers for which this IItemStack matches
	 * <p>
	 * In other words, if a fertilizer uses a Tag ingredient, you can remove it by providing any item with that tag.
	 *
	 * @param fertilizer The fertilizer to be removed
	 * @docParam fertilizer <item:minecraft:bone_meal>
	 */
	@ZenCodeType.Method
	public void removeFertilizer(IItemStack fertilizer)
	{
		CraftTweakerAPI.apply(new AbstractActionGenericRemoveRecipe<ClocheFertilizer>(this, fertilizer)
		{
			@Override
			public boolean shouldRemove(ClocheFertilizer recipe)
			{
				return recipe.input.test(fertilizer.getInternal());
			}
		});
	}

	@Override
	public void removeRecipe(IItemStack fertilizer)
	{
		removeFertilizer(fertilizer);
	}
}
