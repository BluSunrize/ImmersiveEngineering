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
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class PotionBulletFillRecipe extends CustomRecipe
{
	public PotionBulletFillRecipe(ResourceLocation resourceLocation)
	{
		super(resourceLocation, CraftingBookCategory.MISC);
	}

	@Override
	public boolean matches(CraftingContainer inv, @Nonnull Level world)
	{
		boolean hasBullet = false;
		boolean hasPotion = false;
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
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
	public ItemStack assemble(CraftingContainer inv, RegistryAccess access)
	{
		ItemStack bullet = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
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
	public boolean canCraftInDimensions(int width, int height)
	{
		return width*height >= 2;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.POTION_BULLET_FILL.get();
	}


	private boolean isPotionBullet(ItemStack stack)
	{
		return stack.getItem() instanceof BulletItem&&
				((BulletItem)stack.getItem()).getType()==BulletHandler.getBullet(BulletItem.POTION);
	}
}