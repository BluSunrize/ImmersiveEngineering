/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class RecipeShaderBags extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack stack = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(stack.isEmpty())
				{
					if(IEContent.itemShaderBag.equals(stackInSlot.getItem())&&ItemNBTHelper.hasKey(stackInSlot, "rarity"))
						stack = stackInSlot;
					else
						return false;
//					if(IEContent.itemShader.equals(stackInSlot.getItem()) && ItemNBTHelper.hasKey(stackInSlot, "shader_name"))
//						stack = stackInSlot;
				}
				else
					return false;
		}
		return !stack.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				ItemStack output = new ItemStack(IEContent.itemShaderBag, IEContent.itemShaderBag.equals(stackInSlot.getItem())?2: 1);
				EnumRarity next = ShaderRegistry.getLowerRarity(stackInSlot.getRarity());
				if(next!=null)
				{
					ItemNBTHelper.setString(output, "rarity", next.toString());
					return output;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(IEContent.itemShaderBag, 2);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}
}