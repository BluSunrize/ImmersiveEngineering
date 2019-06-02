/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.item.crafting.RecipeSerializers.SimpleSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class RecipeRevolver implements IRecipe
{
	public static final IRecipeSerializer<RecipeRevolver> SERIALIZER = RecipeSerializers.register(
			new SimpleSerializer<>(ImmersiveEngineering.MODID+":revolver", RecipeRevolver::new)
	);

	private final ResourceLocation id;

	public RecipeRevolver(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean matches(IInventory inv, @Nonnull World world)
	{
		ItemStack revolver = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				if(revolver.isEmpty()&&stackInSlot.getItem()==IEContent.itemRevolver)
					revolver = stackInSlot;
				else
					return false;
			}
		}
		return !revolver.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(IInventory inv)
	{
		ItemStack revolver = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				if(revolver.isEmpty()&&stackInSlot.getItem()==IEContent.itemRevolver)
					revolver = stackInSlot.copy();
				else
					return ItemStack.EMPTY;
			}
		}
		return revolver;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}
}