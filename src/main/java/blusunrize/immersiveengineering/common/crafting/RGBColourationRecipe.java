/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public record RGBColourationRecipe(Ingredient target) implements CraftingRecipe
{
	@Override
	public boolean matches(CraftingInput inv, @Nonnull Level world)
	{
		ItemStack itemToColour = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				if(itemToColour.isEmpty()&&target.test(stackInSlot))
					itemToColour = stackInSlot;
				else if(Utils.isDye(stackInSlot))
					list.add(stackInSlot);
				else
					return false;
			}
		}
		return !itemToColour.isEmpty()&&!list.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack assemble(CraftingInput inv, Provider access)
	{
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		ItemStack itemToColour = ItemStack.EMPTY;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
				if(itemToColour.isEmpty()&&target.test(stackInSlot))
				{
					itemToColour = stackInSlot;
					int colour;
					if(itemToColour.has(IEDataComponents.COLOR))
						colour = itemToColour.get(IEDataComponents.COLOR).toInt();
					else
						colour = 0xff_ff_ff;
					float r = (float)(colour>>16&255)/255.0F;
					float g = (float)(colour>>8&255)/255.0F;
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
		if(!itemToColour.isEmpty())
		{
			ItemStack newItem = itemToColour.copyWithCount(1);
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
			newItem.set(IEDataComponents.COLOR, Color4.fromRGB(newColour));
			return newItem;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(Provider access)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.RGB_SERIALIZER.get();
	}

	@Override
	public CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}
}