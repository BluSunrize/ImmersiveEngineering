/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class RevolverCycleRecipe extends CustomRecipe
{
	public RevolverCycleRecipe(CraftingBookCategory category)
	{
		super(category);
	}

	@Override
	public boolean matches(CraftingInput inv, @Nonnull Level world)
	{
		ItemStack revolver = ItemStack.EMPTY;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				if(revolver.isEmpty()&&stackInSlot.getItem()==Weapons.REVOLVER.asItem())
					revolver = stackInSlot;
				else
					return false;
			}
		}
		return !revolver.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack assemble(CraftingInput inv, Provider access)
	{
		ItemStack revolver = ItemStack.EMPTY;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				if(revolver.isEmpty()&&stackInSlot.getItem()==Weapons.REVOLVER.asItem())
					revolver = stackInSlot.copy();
				else
					return ItemStack.EMPTY;
			}
		}
		return revolver;
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
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.REVOLVER_CYCLE_SERIALIZER.get();
	}

}