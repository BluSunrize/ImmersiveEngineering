/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RevolverCycleRecipe extends CustomRecipe
{
	public RevolverCycleRecipe(ResourceLocation id)
	{
		super(id);
	}

	@Override
	public boolean matches(CraftingContainer inv, @Nonnull Level world)
	{
		ItemStack revolver = ItemStack.EMPTY;
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				if(revolver.isEmpty()&&stackInSlot.getItem()==Weapons.revolver)
					revolver = stackInSlot;
				else
					return false;
			}
		}
		return !revolver.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack assemble(CraftingContainer inv)
	{
		ItemStack revolver = ItemStack.EMPTY;
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				if(revolver.isEmpty()&&stackInSlot.getItem()==Weapons.revolver)
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
	public ItemStack getResultItem()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.REVOLVER_CYCLE_SERIALIZER.get();
	}

}