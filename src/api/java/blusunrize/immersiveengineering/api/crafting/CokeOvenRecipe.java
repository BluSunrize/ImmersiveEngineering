/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collections;
import java.util.Map;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the coke oven
 */
public class CokeOvenRecipe extends IESerializableRecipe
{
	public static IRecipeType<CokeOvenRecipe> TYPE = IRecipeType.register(Lib.MODID+":coke_oven");
	public static RegistryObject<IERecipeSerializer<CokeOvenRecipe>> SERIALIZER;

	public final IngredientWithSize input;
	public final ItemStack output;
	public final int time;
	public final int creosoteOutput;

	public CokeOvenRecipe(ResourceLocation id, ItemStack output, IngredientWithSize input, int time, int creosoteOutput)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		this.time = time;
		this.creosoteOutput = creosoteOutput;
	}

	@Override
	protected IERecipeSerializer getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.output;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, CokeOvenRecipe> recipeList = Collections.emptyMap();

	public static CokeOvenRecipe findRecipe(ItemStack input)
	{
		for(CokeOvenRecipe recipe : recipeList.values())
			if(ItemUtils.stackMatchesObject(input, recipe.input))
				return recipe;
		return null;
	}

}
