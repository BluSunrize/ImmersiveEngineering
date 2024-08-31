/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.items.components.AttachedItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class EarmuffsRecipe implements CraftingRecipe
{
	public EarmuffsRecipe()
	{
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public boolean matches(CraftingInput inv, @Nonnull Level worldIn)
	{
		ItemStack earmuffs = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				final boolean isEarmuffs = stackInSlot.is(Misc.EARMUFFS.asItem());
				if(earmuffs.isEmpty()&&isEarmuffs)
					earmuffs = stackInSlot;
				else if(armor.isEmpty()&&stackInSlot.getItem() instanceof ArmorItem armorItem&&
						armorItem.getEquipmentSlot()==EquipmentSlot.HEAD&&
						!isEarmuffs)
					armor = stackInSlot;
				else
					return false;
			}
		}
		if(armor.isEmpty())
			return false;
		boolean addingEarmuffs = !earmuffs.isEmpty();
		boolean removingEarmuffs = armor.has(IEDataComponents.CONTAINED_EARMUFF);
		return addingEarmuffs!=removingEarmuffs;
	}

	@Nonnull
	@Override
	public ItemStack assemble(CraftingInput inv, Provider access)
	{
		ItemStack earmuffs = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				final boolean isEarmuffs = stackInSlot.is(Misc.EARMUFFS.asItem());
				if(earmuffs.isEmpty()&&isEarmuffs)
					earmuffs = stackInSlot;
				else if(armor.isEmpty()&&stackInSlot.getItem() instanceof ArmorItem&&
						((ArmorItem)stackInSlot.getItem()).getEquipmentSlot()==EquipmentSlot.HEAD&&
						!isEarmuffs)
					armor = stackInSlot;
				// TODO else fail?
			}
		}

		if(!earmuffs.isEmpty())
		{
			ItemStack output;
			if(!armor.isEmpty())
			{
				output = armor.copy();
				output.set(IEDataComponents.CONTAINED_EARMUFF, new AttachedItem(earmuffs));
			}
			else
				output = earmuffs.copy();
			return output;
		}
		else if(!armor.isEmpty()&&armor.has(IEDataComponents.CONTAINED_EARMUFF))
		{
			ItemStack output = armor.copy();
			output.remove(IEDataComponents.CONTAINED_EARMUFF);
			return output;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(Provider access)
	{
		return new ItemStack(Misc.EARMUFFS, 1);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
	{
		NonNullList<ItemStack> remaining = CraftingRecipe.super.getRemainingItems(inv);
		for(int i = 0; i < remaining.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			final var earmuffs = stackInSlot.get(IEDataComponents.CONTAINED_EARMUFF);
			if(earmuffs!=null)
				remaining.set(i, earmuffs.attached());
		}
		return remaining;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.EARMUFF_SERIALIZER.get();
	}

	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.withSize(1, Ingredient.of(Misc.EARMUFFS));
	}

	@Override
	public CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}
}
