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
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 01.05.2015
 * <p>
 * The recipe for the crusher
 */
public class CrusherRecipe extends MultiblockRecipe
{
	public static IRecipeType<CrusherRecipe> TYPE = IRecipeType.register(Lib.MODID+":crusher");
	public static RegistryObject<IERecipeSerializer<CrusherRecipe>> SERIALIZER;

	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final Ingredient input;
	public final ItemStack output;
	public final List<StackWithChance> secondaryOutputs = new ArrayList<>();

	public CrusherRecipe(ResourceLocation id, ItemStack output, Ingredient input, int energy)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(50*timeModifier);

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

	public static List<CrusherRecipe> recipeList = new ArrayList<>();

	public static CrusherRecipe findRecipe(ItemStack input)
	{
		for(CrusherRecipe recipe : recipeList)
			if(recipe.input.test(input))
				return recipe;
		return null;
	}

	public static List<CrusherRecipe> removeRecipesForOutput(ItemStack stack)
	{
		List<CrusherRecipe> list = new ArrayList<>();
		Iterator<CrusherRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			CrusherRecipe ir = it.next();
			if(ItemStack.areItemsEqual(ir.output, stack))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	public static List<CrusherRecipe> removeRecipesForInput(ItemStack stack)
	{
		List<CrusherRecipe> list = new ArrayList<>();
		Iterator<CrusherRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			CrusherRecipe ir = it.next();
			if(ir.input.test(stack))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 4;
	}

}