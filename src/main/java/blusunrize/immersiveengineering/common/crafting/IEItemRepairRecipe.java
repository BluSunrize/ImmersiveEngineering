/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RepairItemRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class IEItemRepairRecipe extends RepairItemRecipe
{
	private Ingredient tool;

	public IEItemRepairRecipe(ResourceLocation name, Ingredient tool)
	{
		super(name);
		this.tool = tool;
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn)
	{
		return getRelevantStacks(inv)!=null;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
//		int[] relevant = getRelevantStacks(inv);
//		if(relevant==null)
//			return ItemStack.EMPTY;
//
//		ItemStack first = inv.getStackInSlot(relevant[0]);
//		ItemStack second = inv.getStackInSlot(relevant[1]);
//		IItemDamageableIE firstDamage = (IItemDamageableIE)first.getItem();
//		IItemDamageableIE secondDamage = (IItemDamageableIE)second.getItem();
//
//		int j = firstDamage.getMaxDamageIE(first)-firstDamage.getItemDamageIE(first);
//		int k = firstDamage.getMaxDamageIE(first)-secondDamage.getItemDamageIE(second);
//		int l = j+k+firstDamage.getMaxDamageIE(first)*5/100;
//		int i1 = firstDamage.getMaxDamageIE(first)-l;
//
//		if(i1 < 0)
//			i1 = 0;

//		ItemStack ret = new ItemStack(first.getItem(), 1);
//		ItemNBTHelper.putInt(ret, Lib.NBT_DAMAGE, i1);
//		return ret;
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
	{
		NonNullList<ItemStack> ret = super.getRemainingItems(inv);
		int[] relevantStacks = getRelevantStacks(inv);
		if(relevantStacks!=null)
		{
			ret.set(relevantStacks[0], ItemStack.EMPTY);
			ret.set(relevantStacks[1], ItemStack.EMPTY);
		}
		return ret;
	}


	private int[] getRelevantStacks(IInventory inv)
	{
		int[] ret = new int[2];
		int indexInRet = 0;
//		for(int i = 0; i < inv.getSizeInventory(); ++i)
//		{
//			ItemStack curr = inv.getStackInSlot(i);
//
//			if(tool.test(curr)&&curr.getItem() instanceof IItemDamageableIE)
//			{
//				if(indexInRet > 1)
//					return null;
//
//				ret[indexInRet] = i;
//
//				indexInRet++;
//			}
//			else if(!curr.isEmpty())
//				return null;
//		}
		return indexInRet==2?ret: null;
	}

	public Ingredient getToolIngredient()
	{
		return tool;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return IEItemRepairRecipeSerializer.INSTANCE;
	}
}
