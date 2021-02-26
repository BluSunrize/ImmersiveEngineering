/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class SpeedloaderLoadRecipe extends SpecialRecipe
{
	private final byte[] offsetPattern = {0, 1, 1, 1, 0, -1, -1, -1};

	public SpeedloaderLoadRecipe(ResourceLocation resourceLocation)
	{
		super(resourceLocation);
	}

	@Override
	public boolean matches(CraftingInventory inv, @Nonnull World world)
	{
		ItemStack stackInSlot;
		int speedloaderX = -1;
		int speedloaderY = -1;
		boolean hasSpeedloader = false;
		NonNullList<ItemStack> speedloaderBullets = null;
		boolean hasBullets = false;
		int width = inv.getWidth();
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.getItem() instanceof SpeedloaderItem)
				{
					if(hasSpeedloader)
						return false;
					if(!((SpeedloaderItem)stackInSlot.getItem()).isEmpty(stackInSlot))
						speedloaderBullets = ((SpeedloaderItem)stackInSlot.getItem()).getBullets(stackInSlot, false);
					speedloaderX = i%width;
					speedloaderY = i/width;
					hasSpeedloader = true;
				}
				else if(stackInSlot.getItem() instanceof BulletItem)
					hasBullets = true;
				else
					return false;
			}
		}
		if(hasSpeedloader&&hasBullets)
		{
			for(int i = 0; i < inv.getSizeInventory(); i++)
			{
				stackInSlot = inv.getStackInSlot(i);
				if(!stackInSlot.isEmpty())
				{
					int curOffsetY = (i/width)-speedloaderY;
					int curOffsetX = (i%width)-speedloaderX;
					if((curOffsetY!=0||curOffsetX!=0)&&
							(Math.abs(curOffsetY) > 1||Math.abs(curOffsetX) > 1||
									(speedloaderBullets!=null&&!speedloaderBullets.get(3+(curOffsetX > 0||(curOffsetX==0&&curOffsetY < 0)?1: -1)*(curOffsetX+curOffsetY-2)).isEmpty())
							)
					)
						return false;
				}
			}
			return true;
		}
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		ItemStack speedloader = null;
		int speedloaderX = -1;
		int speedloaderY = -1;
		int width = inv.getWidth();
		int height = inv.getHeight();
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			if(inv.getStackInSlot(i).getItem() instanceof SpeedloaderItem)
			{
				speedloader = inv.getStackInSlot(i);
				speedloaderX = i%width;
				speedloaderY = i/width;
				break;
			}
		}

		ItemStack out = speedloader.copy();
		NonNullList<ItemStack> fill = ((SpeedloaderItem)out.getItem()).getBullets(out, false);

		for(int i = 0; i < 8; i++)
		{ //8 == offsetPattern.length == # of Revolver Slots
			int curY = speedloaderY+offsetPattern[(i+6)%8];
			if(curY < 0||curY >= height)
				continue;
			int curX = speedloaderX+offsetPattern[i];
			if(curX < 0||curX >= width)
				continue;
			ItemStack curBullet = inv.getStackInSlot(width*curY+curX);
			if(!curBullet.isEmpty())
				fill.set(i, ItemHandlerHelper.copyStackWithSize(curBullet, 1));
		}
		((SpeedloaderItem)out.getItem()).setContainedItems(out, fill);
		return out;
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
		return RecipeSerializers.SPEEDLOADER_LOAD.get();
	}

}