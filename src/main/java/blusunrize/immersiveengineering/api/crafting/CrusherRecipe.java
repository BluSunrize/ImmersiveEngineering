/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * @author BluSunrize - 01.05.2015
 * <p>
 * The recipe for the crusher
 */
public class CrusherRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final Ingredient input;
	public final ItemStack output;
	public final List<SecondaryOutput> secondaryOutputs = new ArrayList<>();

	public CrusherRecipe(ItemStack output, Ingredient input, int energy)
	{
		this.output = output;
		this.input = input;
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(50*timeModifier);

		setInputList(Lists.newArrayList(this.input));
		this.outputList = ListUtils.fromItem(this.output);
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(output);
		for(SecondaryOutput output : secondaryOutputs)
			if(output.stack.isValid()&&Utils.RAND.nextFloat() < output.chance)
				list.add(output.stack.getExampleStack());
		return list;
	}

	/**
	 * Adds secondary outputs to the recipe. Should the recipe have secondary outputs, these will be added /in addition/
	 */
	public CrusherRecipe addToSecondaryOutput(SecondaryOutput... outputs)
	{
		for(SecondaryOutput o : outputs)
			Preconditions.checkNotNull(o);
		secondaryOutputs.addAll(Arrays.asList(outputs));
		return this;
	}

	public static ArrayList<CrusherRecipe> recipeList = new ArrayList<>();

	public static CrusherRecipe addRecipe(ItemStack output, Ingredient input, int energy)
	{
		CrusherRecipe r = new CrusherRecipe(output, input, energy);
		if(r.input!=null&&!r.output.isEmpty())
			recipeList.add(r);
		return r;
	}

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

	public static class SecondaryOutput
	{
		public final IngredientStack stack;
		public final float chance;

		public SecondaryOutput(Item item, float chance)
		{
			this(new IngredientStack(item), chance);
		}

		public SecondaryOutput(Tag<Item> tag, float chance)
		{
			this(new IngredientStack(tag), chance);
		}

		public SecondaryOutput(ResourceLocation tag, float chance)
		{
			this(new IngredientStack(tag), chance);
		}

		public SecondaryOutput(IngredientStack stack, float chance)
		{
			Preconditions.checkNotNull(stack);
			this.stack = stack;
			this.chance = chance;
		}
	}
}