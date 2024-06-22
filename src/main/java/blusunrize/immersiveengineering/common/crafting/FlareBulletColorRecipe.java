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
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class FlareBulletColorRecipe extends CustomRecipe
{
	public FlareBulletColorRecipe(CraftingBookCategory category)
	{
		super(category);
	}

	@Override
	public boolean matches(CraftingInput inv, @Nonnull Level world)
	{
		boolean hasBullet = false;
		boolean hasDye = false;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
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
	public ItemStack assemble(CraftingInput inv, Provider access)
	{
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		ItemStack bullet = ItemStack.EMPTY;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
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
					int color = Utils.getDye(stackInSlot).getTextureDiffuseColor();
					int r = (color>>16)&255;
					int g = (color>>8)&255;
					int b = color&255;
					j += Math.max(r, Math.max(g, b));
					colourArray[0] += r;
					colourArray[1] += g;
					colourArray[2] += b;
					++totalColourSets;
				}
		}
		if(!bullet.isEmpty())
		{
			ItemStack newBullet = bullet.copyWithCount(1);

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
	public boolean canCraftInDimensions(int width, int height)
	{
		return width*height >= 2;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.FLARE_BULLET_COLOR.get();
	}

	private boolean isFlareBullet(ItemStack stack)
	{
		return stack.getItem() instanceof BulletItem<?> bulletItem&&
				bulletItem.getType()==BulletHandler.getBullet(IEBullets.FLARE);
	}
}