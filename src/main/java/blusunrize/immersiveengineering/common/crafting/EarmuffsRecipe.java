/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class EarmuffsRecipe implements ICraftingRecipe
{
	private final ResourceLocation id;

	public EarmuffsRecipe(ResourceLocation rl)
	{
		id = rl;
	}

	@Override
	public boolean matches(CraftingInventory inv, @Nonnull World worldIn)
	{
		ItemStack earmuffs = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(earmuffs.isEmpty()&&Misc.earmuffs.equals(stackInSlot.getItem()))
					earmuffs = stackInSlot;
				else if(armor.isEmpty()&&stackInSlot.getItem() instanceof ArmorItem&&
						((ArmorItem)stackInSlot.getItem()).getEquipmentSlot()==EquipmentSlotType.HEAD&&
						!Misc.earmuffs.equals(stackInSlot.getItem()))
					armor = stackInSlot;
				else if(Utils.isDye(stackInSlot))
					list.add(stackInSlot);
				else
					return false;
		}
		if(!earmuffs.isEmpty()&&(!armor.isEmpty()||!list.isEmpty()))
			return true;
		else return !armor.isEmpty()&&ItemNBTHelper.hasKey(armor, Lib.NBT_Earmuffs)&&earmuffs.isEmpty()&&list.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		ItemStack earmuffs = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(earmuffs.isEmpty()&&Misc.earmuffs.equals(stackInSlot.getItem()))
				{
					earmuffs = stackInSlot;
					int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
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
				else if(armor.isEmpty()&&stackInSlot.getItem() instanceof ArmorItem&&
						((ArmorItem)stackInSlot.getItem()).getEquipmentSlot()==EquipmentSlotType.HEAD&&
						!Misc.earmuffs.equals(stackInSlot.getItem()))
					armor = stackInSlot;
		}

		if(!earmuffs.isEmpty())
		{
			if(totalColourSets > 1)
			{
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
				ItemNBTHelper.putInt(earmuffs, Lib.NBT_EarmuffColour, newColour);
			}
			ItemStack output;
			if(!armor.isEmpty())
			{
				output = armor.copy();
				ItemNBTHelper.setItemStack(output, Lib.NBT_Earmuffs, earmuffs.copy());
			}
			else
				output = earmuffs.copy();
			return output;
		}
		else if(!armor.isEmpty()&&ItemNBTHelper.hasKey(armor, Lib.NBT_Earmuffs))
		{
			ItemStack output = armor.copy();
			ItemNBTHelper.remove(output, Lib.NBT_Earmuffs);
			return output;
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
		return new ItemStack(Misc.earmuffs, 1);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
	{
		NonNullList<ItemStack> remaining = ICraftingRecipe.super.getRemainingItems(inv);
		for(int i = 0; i < remaining.size(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty()&&ItemNBTHelper.hasKey(stackInSlot, Lib.NBT_Earmuffs))
				remaining.set(i, ItemNBTHelper.getItemStack(stackInSlot, Lib.NBT_Earmuffs));
		}
		return remaining;
	}

	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.EARMUFF_SERIALIZER.get();
	}

	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.withSize(1, Ingredient.fromItems(Misc.earmuffs));
	}
}
