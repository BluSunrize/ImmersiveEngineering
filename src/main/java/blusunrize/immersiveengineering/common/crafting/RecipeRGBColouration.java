/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RecipeRGBColouration extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
	final Predicate<ItemStack> predicate;
	final Function<ItemStack, Integer> colourGetter;
	final BiConsumer<ItemStack, Integer> colourSetter;

	public RecipeRGBColouration(Predicate<ItemStack> predicate, Function<ItemStack, Integer> colourGetter, BiConsumer<ItemStack, Integer> colourSetter)
	{
		this.predicate = predicate;
		this.colourGetter = colourGetter;
		this.colourSetter = colourSetter;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack itemToColour = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				if(itemToColour.isEmpty()&&predicate.test(stackInSlot))
					itemToColour = stackInSlot;
				else if(Utils.isDye(stackInSlot))
					list.add(stackInSlot);
				else
					return false;
			}
		}
		return !itemToColour.isEmpty()&&!list.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		ItemStack itemToColour = ItemStack.EMPTY;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(itemToColour.isEmpty()&&predicate.test(stackInSlot))
				{
					itemToColour = stackInSlot;
					int colour = colourGetter.apply(itemToColour);
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
					float[] afloat = EntitySheep.getDyeRgb(EnumDyeColor.byDyeDamage(Utils.getDye(stackInSlot)));
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
			colourSetter.accept(newItem, newColour);
			return newItem;
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
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}
}