/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author BluSunrize - 01.05.2015
 * <p>
 * The recipe for the crusher
 */
public class CrusherRecipe extends MultiblockRecipe
{
	public static IRecipeType<CrusherRecipe> TYPE = IRecipeType.register(Lib.MODID+":crusher");
	public static RegistryObject<IERecipeSerializer<CrusherRecipe>> SERIALIZER;

	public final Ingredient input;
	public final ItemStack output;
	public final List<StackWithChance> secondaryOutputs = new ArrayList<>();

	public CrusherRecipe(ResourceLocation id, ItemStack output, Ingredient input, int energy)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		setTimeAndEnergy(50, energy);

		setInputList(Lists.newArrayList(this.input));
		this.outputList = ListUtils.fromItem(this.output);
	}

	@Override
	protected IERecipeSerializer<CrusherRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(output);
		for(StackWithChance output : secondaryOutputs)
			if(!output.getStack().isEmpty()&&Utils.RAND.nextFloat() < output.getChance())
				list.add(output.getStack());
		return list;
	}

	/**
	 * Adds secondary outputs to the recipe. Should the recipe have secondary outputs, these will be added /in addition/
	 */
	public CrusherRecipe addToSecondaryOutput(StackWithChance output)
	{
		Preconditions.checkNotNull(output);
		secondaryOutputs.add(output);
		return this;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, CrusherRecipe> recipeList = Collections.emptyMap();

	public static CrusherRecipe findRecipe(ItemStack input)
	{
		for(CrusherRecipe recipe : recipeList.values())
			if(recipe.input.test(input))
				return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 4;
	}

}