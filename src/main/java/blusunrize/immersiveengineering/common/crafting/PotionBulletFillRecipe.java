/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class PotionBulletFillRecipe extends SpecialRecipe
{
	public PotionBulletFillRecipe(ResourceLocation resourceLocation)
	{
		super(resourceLocation);
	}

	@Override
	public boolean matches(CraftingInventory inv, @Nonnull World world)
	{
		boolean hasBullet = false;
		boolean hasPotion = false;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				if(isPotionBullet(stackInSlot))
				{
					if(hasBullet)
						return false;
					hasBullet = true;
				}
				else if(stackInSlot.getItem() instanceof PotionItem)
				{
					if(hasPotion)
						return false;
					hasPotion = true;
				}
				else
					return false;
			}
		}
		return hasBullet&&hasPotion;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		ItemStack bullet = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(bullet.isEmpty()&&isPotionBullet(stackInSlot))
					bullet = stackInSlot;
				else if(potion.isEmpty()&&stackInSlot.getItem() instanceof PotionItem)
					potion = stackInSlot;
		}
		ItemStack newBullet = ItemHandlerHelper.copyStackWithSize(bullet, 1);
		ItemNBTHelper.setItemStack(newBullet, "potion", potion.copy());
		return newBullet;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width*height >= 2;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.POTION_BULLET_FILL.get();
	}


	private boolean isPotionBullet(ItemStack stack)
	{
		return stack.getItem() instanceof BulletItem&&
				((BulletItem)stack.getItem()).getType()==BulletHandler.getBullet(BulletItem.POTION);
	}
}