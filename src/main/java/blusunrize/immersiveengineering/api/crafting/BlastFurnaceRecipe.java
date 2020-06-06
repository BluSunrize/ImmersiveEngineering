/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the blast furnace
 */
public class BlastFurnaceRecipe extends IESerializableRecipe
{
	public static IRecipeType<BlastFurnaceRecipe> TYPE = IRecipeType.register(Lib.MODID+":blast_furnace");
	public static RegistryObject<IERecipeSerializer<BlastFurnaceRecipe>> SERIALIZER;

	public final IngredientWithSize input;
	public final ItemStack output;
	@Nonnull
	public final ItemStack slag;
	public final int time;

	public BlastFurnaceRecipe(ResourceLocation id, ItemStack output, IngredientWithSize input, int time, @Nonnull ItemStack slag)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		this.time = time;
		this.slag = slag;
	}

	@Override
	protected IERecipeSerializer<BlastFurnaceRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return output;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, BlastFurnaceRecipe> recipeList;

	public static BlastFurnaceRecipe findRecipe(ItemStack input)
	{
		for(BlastFurnaceRecipe recipe : recipeList.values())
			if(recipe.input.test(input))
				return recipe;
		return null;
	}
}