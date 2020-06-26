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
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class FlareBulletColorRecipe extends SpecialRecipe
{
	public FlareBulletColorRecipe(ResourceLocation resourceLocation)
	{
		super(resourceLocation);
	}

	@Override
	public boolean matches(CraftingInventory inv, @Nonnull World world)
	{
		boolean hasBullet = false;
		boolean hasDye = false;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				if(isFlareBullet(stackInSlot))
				{
					if(hasBullet)
						return false;
					hasBullet = true;
				}
				else if(Utils.isDye(stackInSlot))
					hasDye = true;
				else
					return false;
			}
		}
		return hasBullet&&hasDye;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		ItemStack bullet = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(bullet.isEmpty()&&isFlareBullet(stackInSlot))
				{
					bullet = stackInSlot;

					int colour = ((IColouredItem)bullet.getItem()).getColourForIEItem(bullet, 1);
					float r = (float)(colour >> 16&255)/255.0F;
					float g = (float)(colour >> 8&255)/255.0F;
					float b = (float)(colour&255)/255.0F;
					j = (int)((float)j+Math.max(r, Math.max(g, b))*255.0F);
					colourArray[0] = (int)((float)colourArray[0]+r*255.0F);
					colourArray[1] = (int)((float)colourArray[1]+g*255.0F);
					colourArray[2] = (int)((float)colourArray[2]+b*255.0F);
					++totalColourSets;
				}
				else if(Utils.isDye(stackInSlot))
				{
					float[] afloat = Utils.getDye(stackInSlot).getColorComponentValues();
					int r = (int)(afloat[0]*255.0F);
					int g = (int)(afloat[1]*255.0F);
					int b = (int)(afloat[2]*255.0F);
					j += Math.max(r, Math.max(g, b));
					colourArray[0] += r;
					colourArray[1] += g;
					colourArray[2] += b;
					++totalColourSets;
				}
		}
		if(!bullet.isEmpty())
		{
			ItemStack newBullet = Utils.copyStackWithAmount(bullet, 1);

			int r = colourArray[0]/totalColourSets;
			int g = colourArray[1]/totalColourSets;
			int b = colourArray[2]/totalColourSets;
			float colourMod = (float)j/(float)totalColourSets;
			float highestColour = (float)Math.max(r, Math.max(g, b));
			r = (int)((float)r*colourMod/highestColour);
			g = (int)((float)g*colourMod/highestColour);
			b = (int)((float)b*colourMod/highestColour);
			int newColour = (r<<8)+g;
			newColour = (newColour<<8)+b;
			ItemNBTHelper.putInt(newBullet, "flareColour", newColour);
			return newBullet;
		}
		return ItemStack.EMPTY;
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
		return RecipeSerializers.FLARE_BULLET_COLOR.get();
	}

	private boolean isFlareBullet(ItemStack stack)
	{
		return stack.getItem() instanceof BulletItem&&
				((BulletItem)stack.getItem()).getType()==BulletHandler.getBullet(BulletItem.FLARE);
	}
}