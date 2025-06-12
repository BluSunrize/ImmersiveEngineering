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
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Optional;
import java.util.function.Predicate;

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

	public record EquippedItem(EquipmentSlot slot, ItemStack stack){}

	public static Optional<Pair<EquippedItem, EquippedItem>> isHoldingBoth(Player player, TagKey<Item> tag1, TagKey<Item> tag2)
	{
		return isHoldingBoth(player, s -> s.is(tag1), s -> s.is(tag2));
	}

	public static Optional<Pair<EquippedItem, EquippedItem>> isHoldingBoth(Player player, Predicate<ItemStack> check1, Predicate<ItemStack> check2)
	{
		if(check1.test(player.getMainHandItem())&&check2.test(player.getOffhandItem()))
			return Optional.of(Pair.of(
					new EquippedItem(EquipmentSlot.MAINHAND,player.getMainHandItem()),
					new EquippedItem(EquipmentSlot.OFFHAND,player.getOffhandItem())
			));
		if(check1.test(player.getOffhandItem())&&check2.test(player.getMainHandItem()))
			return Optional.of(Pair.of(
					new EquippedItem(EquipmentSlot.OFFHAND,player.getOffhandItem()),
					new EquippedItem(EquipmentSlot.MAINHAND,player.getMainHandItem())
			));
		return Optional.empty();
	}


	public static void tryInsertEntity(Level level, BlockPos pos, Direction side, ItemEntity toInsert)
	{
		IItemHandler itemHandler = CapabilityUtils.findItemHandlerAtPos(level, pos, side, true);
		if(itemHandler==null)
			return;
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

	public static ItemStack damageCopy(ItemStack tool, int amount)
	{
		ItemStack copy = tool.copy();
		damageDirect(copy, amount);
		return copy;
	}

	public static void damageDirect(ItemStack tool, int amount)
	{
		final int maxDamage = tool.getMaxDamage();
		final int newDamage = tool.getDamageValue()+amount;
		if(newDamage > maxDamage)
		{
			tool.setDamageValue(0);
			tool.shrink(1);
		}
		else
			tool.setDamageValue(newDamage);
	}

	public static void damageStackableItem(ItemStack stack, Level level, int amount)
	{
		// HACK: We cannot set the default value "the usual way" because MC stops you from having an item that
		// is both stackable and damageable.
		if(!stack.has(DataComponents.DAMAGE))
			stack.set(DataComponents.DAMAGE, 0);
		stack.hurtAndBreak(amount, (ServerLevel)level, null, (item) -> {
		});
	}
}
