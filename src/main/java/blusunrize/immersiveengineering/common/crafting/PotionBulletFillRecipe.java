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
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class PotionBulletFillRecipe extends CustomRecipe
{
	public PotionBulletFillRecipe(CraftingBookCategory category)
	{
		super(category);
	}

	@Override
	public boolean matches(CraftingInput inv, @Nonnull Level world)
	{
		boolean hasBullet = false;
		boolean hasPotion = false;
		for(int i = 0; i < inv.size(); i++)
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
	public ItemStack assemble(CraftingInput inv, Provider access)
	{
		ItemStack bullet = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
				if(bullet.isEmpty()&&isPotionBullet(stackInSlot))
					bullet = stackInSlot;
				else if(potion.isEmpty()&&stackInSlot.getItem() instanceof PotionItem)
					potion = stackInSlot;
		}
		ItemStack newBullet = bullet.copyWithCount(1);
		newBullet.set(DataComponents.POTION_CONTENTS, potion.get(DataComponents.POTION_CONTENTS));
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
		return stack.getItem() instanceof BulletItem<?> bulletItem&&
				bulletItem.getType()==BulletHandler.getBullet(IEBullets.POTION);
	}
}