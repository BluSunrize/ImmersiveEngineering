/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.Iterator;

public class RecipeShapelessIngredient extends ShapelessOreRecipe
{
	int nbtCopyTargetSlot = -1;
	int toolDamageSlot = -1;

	public RecipeShapelessIngredient(ResourceLocation group, ItemStack result, Object... recipe)
	{
		super(group, result, wrapIngredients(recipe));
	}

	public RecipeShapelessIngredient(ResourceLocation group, ItemStack result, NonNullList<Ingredient> input)
	{
		super(group, input, result);
	}

	private static Object[] wrapIngredients(Object... recipe)
	{
		Object[] out = new Object[recipe.length];
		for(int i = 0; i < recipe.length; i++)
			if(recipe[i] instanceof IngredientStack)
				out[i] = new IngredientIngrStack((IngredientStack)recipe[i]);
			else
				out[i] = recipe[i];
		return out;
	}

	public RecipeShapelessIngredient setNBTCopyTargetRecipe(int slot)
	{
		this.nbtCopyTargetSlot = slot;
		return this;
	}

	public RecipeShapelessIngredient setToolDamageRecipe(int slot)
	{
		this.toolDamageSlot = slot;
		return this;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting matrix)
	{
		if(nbtCopyTargetSlot >= 0&&nbtCopyTargetSlot < getIngredients().size())
			for(int i = 0; i < matrix.getSizeInventory(); i++)
			{
				ItemStack slot = matrix.getStackInSlot(i);
				if(getIngredients().get(nbtCopyTargetSlot).apply(slot))
				{
					ItemStack out = output.copy();
					if(!matrix.getStackInSlot(nbtCopyTargetSlot).isEmpty()&&matrix.getStackInSlot(nbtCopyTargetSlot).hasTagCompound())
						out.setTagCompound(matrix.getStackInSlot(nbtCopyTargetSlot).getTagCompound().copy());
					return out;
				}
			}
		return super.getCraftingResult(matrix);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		NonNullList<ItemStack> remains = super.getRemainingItems(inv);
		for(int i = 0; i < remains.size(); i++)
		{
			ItemStack s = inv.getStackInSlot(i);
			ItemStack remain = remains.get(i);
			if(toolDamageSlot >= 0&&toolDamageSlot < getIngredients().size())
			{
				ItemStack tool = ItemStack.EMPTY;
				if(remain.isEmpty()&&!s.isEmpty()&&getIngredients().get(toolDamageSlot).apply(s))
					tool = s.copy();
				else if(!remain.isEmpty()&&getIngredients().get(toolDamageSlot).apply(remain))
					tool = remain;
				if(!tool.isEmpty()&&tool.isItemStackDamageable())
				{
					tool.setItemDamage(tool.getItemDamage()+1);
					if(tool.getItemDamage() > tool.getMaxDamage())
						tool = ItemStack.EMPTY;
					remains.set(i, tool);
				}
			}
			if(!s.isEmpty()&&remain.isEmpty()&&s.getItem() instanceof UniversalBucket)
			{
				ItemStack empty = ((UniversalBucket)s.getItem()).getEmpty();
				if(!empty.isEmpty())
					remains.set(i, empty.copy());
			}
		}
		return remains;
	}

	@Override
	public boolean matches(InventoryCrafting matrix, World world)
	{
		ArrayList<Ingredient> required = new ArrayList(getIngredients());

		for(int i = 0; i < matrix.getSizeInventory(); i++)
		{
			ItemStack slot = matrix.getStackInSlot(i);
			if(!slot.isEmpty())
			{
				boolean inRecipe = false;
				Iterator<Ingredient> iterator = required.iterator();
				while(iterator.hasNext())
				{
					Ingredient next = iterator.next();
					if(next.apply(slot))
					{
						inRecipe = true;
						iterator.remove();
						break;
					}
				}
				if(!inRecipe)
					return false;
			}
		}
		return required.isEmpty();
	}
}