/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class RecipePotionBullets extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack bullet = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(bullet.isEmpty()&&IEContent.itemBullet.equals(stackInSlot.getItem())&&"potion".equals(ItemNBTHelper.getString(stackInSlot, "bullet")))
					bullet = stackInSlot;
				else if(potion.isEmpty()&&stackInSlot.getItem() instanceof ItemPotion)
					potion = stackInSlot;
				else
					return false;
		}
		return !bullet.isEmpty()&&!potion.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack bullet = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(bullet.isEmpty()&&IEContent.itemBullet.equals(stackInSlot.getItem())&&"potion".equals(ItemNBTHelper.getString(stackInSlot, "bullet")))
					bullet = stackInSlot;
				else if(potion.isEmpty()&&stackInSlot.getItem() instanceof ItemPotion)
					potion = stackInSlot;
		}
		ItemStack newBullet = Utils.copyStackWithAmount(bullet, 1);
		ItemNBTHelper.setItemStack(newBullet, "potion", potion.copy());
		return newBullet;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return BulletHandler.getBulletStack("potion");
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}
}