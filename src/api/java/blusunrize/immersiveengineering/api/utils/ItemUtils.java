/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Collection;

public class ItemUtils
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static CompoundNBT parseNbtFromJson(JsonElement jsonElement) throws CommandSyntaxException
	{
		if(jsonElement.isJsonObject())
			return JsonToNBT.getTagFromJson(GSON.toJson(jsonElement));
		else
			return JsonToNBT.getTagFromJson(jsonElement.getAsString());
	}

	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		return stackMatchesObject(stack, o, false);
	}

	public static boolean stackMatchesObject(ItemStack stack, Object o, boolean checkNBT)
	{
		if(o instanceof ItemStack)
			return ItemStack.areItemsEqual((ItemStack)o, stack)&&
					(!checkNBT||ItemStack.areItemStackTagsEqual((ItemStack)o, stack));
		else if(o instanceof Collection)
		{
			for(Object io : (Collection)o)
				if(stackMatchesObject(stack, io, checkNBT))
					return true;
		}
		else if(o instanceof IngredientWithSize)
			return ((IngredientWithSize)o).test(stack);
		else if(o instanceof Ingredient)
			return ((Ingredient)o).test(stack);
		else if(o instanceof ItemStack[])
		{
			for(ItemStack io : (ItemStack[])o)
				if(ItemStack.areItemsEqual(io, stack)&&(!checkNBT||ItemStack.areItemStackTagsEqual(io, stack)))
					return true;
		}
		else if(o instanceof FluidStack)
			return FluidUtil.getFluidContained(stack)
					.map(fs -> fs.containsFluid((FluidStack)o))
					.orElse(false);
		else if(o instanceof FluidTagInput)
			return ((FluidTagInput)o).test(FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY));
		else if(o instanceof ResourceLocation)
			return TagUtils.isInBlockOrItemTag(stack, (ResourceLocation)o);
		else
			throw new IllegalArgumentException("Comparison object "+o+" of class "+o.getClass()+" is invalid!");
		return false;
	}

	@Deprecated
	public static ItemStack copyStackWithAmount(ItemStack stack, int amount)
	{
		return ItemHandlerHelper.copyStackWithSize(stack, amount);
	}

	/**
	 * This takes care of mirroring, because HandSide.opposite is Client Only
	 *
	 * @return the side the entity's provided hand
	 */
	public static HandSide getLivingHand(LivingEntity living, Hand hand)
	{
		HandSide handside = living.getPrimaryHand();
		if(hand!=Hand.MAIN_HAND)
			handside = handside==HandSide.LEFT?HandSide.RIGHT: HandSide.LEFT;
		return handside;
	}

	public static void removeTag(ItemStack stack, String key)
	{
		if(stack.hasTag())
		{
			CompoundNBT tag = stack.getOrCreateTag();
			tag.remove(key);
			if(tag.isEmpty())
				stack.setTag(null);
		}
	}

	public static boolean hasTag(ItemStack stack, String key, int type)
	{
		return stack.hasTag()&&stack.getOrCreateTag().contains(key, type);
	}
}
