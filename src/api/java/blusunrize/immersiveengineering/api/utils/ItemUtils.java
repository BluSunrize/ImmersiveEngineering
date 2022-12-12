/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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
					toInsert.discard();
				else if(temp.getCount() < stack.getCount())
					toInsert.setItem(temp);
			}
		});
	}

	public static boolean isSameIgnoreDurability(ItemStack stackA, ItemStack stackB)
	{
		if(stackA==stackB)
			// Stack is same as itself
			return true;
		else if(stackA.isEmpty()!=stackB.isEmpty())
			// Empty and non-empty are never the same
			return false;
		else if(stackA.isEmpty())
			// If one is empty at this point, the other is as well, and all empty stacks should be equivalent
			return true;
		else if(!stackA.is(stackB.getItem()))
			// Different items => never the same
			return false;
		else if(!stackA.isDamageableItem())
			// Not damageable, same item => always the same
			return true;
		else
			// Damageable, same item => same if damage matches
			return stackA.getDamageValue()==stackB.getDamageValue();
	}
}
