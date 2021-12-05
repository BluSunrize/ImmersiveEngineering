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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Collection;

public class ItemUtils
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static CompoundTag parseNbtFromJson(JsonElement jsonElement) throws CommandSyntaxException
	{
		if(jsonElement.isJsonObject())
			return TagParser.parseTag(GSON.toJson(jsonElement));
		else
			return TagParser.parseTag(jsonElement.getAsString());
	}

	@Deprecated
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		return stackMatchesObject(stack, o, false);
	}

	@Deprecated
	public static boolean stackMatchesObject(ItemStack stack, Object o, boolean checkNBT)
	{
		if(o instanceof ItemStack)
			return ItemStack.isSame((ItemStack)o, stack)&&
					(!checkNBT||ItemStack.tagMatches((ItemStack)o, stack));
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
				if(ItemStack.isSame(io, stack)&&(!checkNBT||ItemStack.tagMatches(io, stack)))
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
	public static HumanoidArm getLivingHand(LivingEntity living, InteractionHand hand)
	{
		HumanoidArm handside = living.getMainArm();
		if(hand!=InteractionHand.MAIN_HAND)
			handside = handside==HumanoidArm.LEFT?HumanoidArm.RIGHT: HumanoidArm.LEFT;
		return handside;
	}

	public static void removeTag(ItemStack stack, String key)
	{
		if(stack.hasTag())
		{
			CompoundTag tag = stack.getOrCreateTag();
			tag.remove(key);
			if(tag.isEmpty())
				stack.setTag(null);
		}
	}

	public static boolean hasTag(ItemStack stack, String key, int type)
	{
		return stack.hasTag()&&stack.getOrCreateTag().contains(key, type);
	}

	public static void tryInsertEntity(Level level, BlockPos pos, Direction side, ItemEntity toInsert)
	{
		LazyOptional<IItemHandler> cap = CapabilityUtils.findItemHandlerAtPos(level, pos, side, true);
		cap.ifPresent(itemHandler -> {
			ItemStack stack = toInsert.getItem();
			ItemStack temp = ItemHandlerHelper.insertItem(itemHandler, stack, true);
			if(temp.getCount() < stack.getCount())
			{
				temp = ItemHandlerHelper.insertItem(itemHandler, stack, false);
				if(temp.isEmpty())
					toInsert.remove();
				else if(temp.getCount() < stack.getCount())
					toInsert.setItem(temp);
			}
		});
	}
}
