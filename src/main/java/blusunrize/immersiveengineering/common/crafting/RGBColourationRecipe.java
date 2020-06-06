/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.List;

public class RGBColourationRecipe implements ICraftingRecipe
{
	private final Ingredient target;
	private final String colorKey;
	private final ResourceLocation id;

	public RGBColourationRecipe(Ingredient target, String colorKey, ResourceLocation id)
	{
		this.target = target;
		this.colorKey = colorKey;
		this.id = id;
	}

	@Override
	public boolean matches(CraftingInventory inv, @Nonnull World world)
	{
		ItemStack itemToColour = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
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
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		ItemStack itemToColour = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(itemToColour.isEmpty()&&target.test(stackInSlot))
				{
					itemToColour = stackInSlot;
					int colour;
					if(itemToColour.hasTag()&&itemToColour.getOrCreateTag().contains(colorKey, NBT.TAG_INT))
						colour = itemToColour.getOrCreateTag().getInt(colorKey);
					else
						colour = 0xff_ff_ff;
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
					float[] afloat = SheepEntity.getDyeRgb(Utils.getDye(stackInSlot));
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
		if(!itemToColour.isEmpty())
		{
			ItemStack newItem = Utils.copyStackWithAmount(itemToColour, 1);
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
			newItem.getOrCreateTag().putInt(colorKey, newColour);
			return newItem;
		}
		return ItemStack.EMPTY;
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

	@Nonnull
	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Override
	public boolean isDynamic()
	{
		return true;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.RGB_SERIALIZER.get();
	}

	public Ingredient getTarget()
	{
		return target;
	}

	public String getColorKey()
	{
		return colorKey;
	}
}