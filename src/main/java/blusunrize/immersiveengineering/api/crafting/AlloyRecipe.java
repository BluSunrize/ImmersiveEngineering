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

import java.util.Map;

/**
 * @author BluSunrize - 19.05.2017
 * <br>
 * The recipe for the alloy smelter
 */
public class AlloyRecipe extends IESerializableRecipe
{
	public static IRecipeType<AlloyRecipe> TYPE = IRecipeType.register(Lib.MODID+":alloy");
	public static RegistryObject<IERecipeSerializer<AlloyRecipe>> SERIALIZER;

	public final IngredientWithSize input0;
	public final IngredientWithSize input1;
	public final ItemStack output;
	public final int time;

	public AlloyRecipe(ResourceLocation id, ItemStack output, IngredientWithSize input0, IngredientWithSize input1, int time)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input0 = input0;
		this.input1 = input1;
		this.time = time;
	}

	@Override
	protected IERecipeSerializer<AlloyRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.output;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, AlloyRecipe> recipeList;

	public static AlloyRecipe findRecipe(ItemStack input0, ItemStack input1)
	{
		for(AlloyRecipe recipe : recipeList.values())
			if((recipe.input0.test(input0)&&recipe.input1.test(input1))||(recipe.input0.test(input1)&&recipe.input1.test(input0)))
				return recipe;
		return null;
	}
}
