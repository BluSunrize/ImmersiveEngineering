/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 03.07.2017
 */
public class IngredientMultiOre extends Ingredient
{
	private NonNullList<ItemStack>[] ores;
	private IntList itemIds = null;
	private ItemStack[] array = null;

	public IngredientMultiOre(String... ores)
	{
		super(0);
		this.ores = new NonNullList[ores.length];
		for(int i = 0; i < ores.length; i++)
			this.ores[i] = OreDictionary.getOres(ores[i]);
	}

	private int totalSize()
	{
		int i = 0;
		for(NonNullList<ItemStack> list : ores)
			i += list.size();
		return i;
	}

	@Override
	@Nonnull
	public ItemStack[] getMatchingStacks()
	{
		if(array==null||this.array.length!=totalSize())
		{
			NonNullList<ItemStack> lst = NonNullList.create();
			for(NonNullList<ItemStack> list : ores)
				for(ItemStack stack : list)
				{
					if(stack.getMetadata()==OreDictionary.WILDCARD_VALUE)
						stack.getItem().getSubItems(CreativeTabs.SEARCH, lst);
					else
						lst.add(stack);
				}
			this.array = lst.toArray(new ItemStack[lst.size()]);
		}
		return this.array;
	}

	@Override
	@Nonnull
	@SideOnly(Side.CLIENT)
	public IntList getValidItemStacksPacked()
	{
		if(this.itemIds==null||this.itemIds.size()!=totalSize())
		{
			this.itemIds = new IntArrayList(totalSize());

			for(NonNullList<ItemStack> list : ores)
				for(ItemStack stack : list)
				{
					if(stack.getMetadata()==OreDictionary.WILDCARD_VALUE)
					{
						NonNullList<ItemStack> lst = NonNullList.create();
						stack.getItem().getSubItems(CreativeTabs.SEARCH, lst);
						for(ItemStack item : lst)
							this.itemIds.add(RecipeItemHelper.pack(item));
					}
					else
					{
						this.itemIds.add(RecipeItemHelper.pack(stack));
					}
				}
			this.itemIds.sort(IntComparators.NATURAL_COMPARATOR);
		}

		return this.itemIds;
	}


	@Override
	public boolean apply(@Nullable ItemStack input)
	{
		if(input==null)
			return false;

		for(NonNullList<ItemStack> list : ores)
			for(ItemStack stack : list)
				if(OreDictionary.itemMatches(stack, input, false))
					return true;

		return false;
	}

	@Override
	protected void invalidate()
	{
		this.itemIds = null;
	}
}
