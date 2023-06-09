/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.IESounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class GliderItem extends IEBaseItem implements Equipable
{
	public GliderItem()
	{
		super(new Properties().stacksTo(1).durability(216));
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack material)
	{
		return material.is(IETags.fabricHemp);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		ItemStack heldItem = player.getItemInHand(hand);
		EquipmentSlot slot = Mob.getEquipmentSlotForItem(heldItem);
		ItemStack equipped = player.getItemBySlot(slot);
		if(equipped.isEmpty())
		{
			player.setItemSlot(slot, heldItem.copy());
			if(!level.isClientSide())
				player.awardStat(Stats.ITEM_USED.get(this));
			heldItem.setCount(0);
			return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide());
		}
		else
			return InteractionResultHolder.fail(heldItem);
	}

	@Override
	public boolean canElytraFly(ItemStack stack, LivingEntity entity)
	{
		return ElytraItem.isFlyEnabled(stack);
	}

	@Override
	public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks)
	{
		if(!entity.level().isClientSide)
		{
			int nextFlightTick = flightTicks+1;
			if(nextFlightTick%10==0)
			{
				// if the player goes too fast, the glider takes additional damage
				double speed = entity.getDeltaMovement().length();
				int itemDamage = speed > 1.5?3: 1;
				if(itemDamage > 1&&entity instanceof Player player)
					player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"glider.too_fast"), true);
				// It also makes worrying noises!
				if(itemDamage>1 && (nextFlightTick+40)%60==0)
					entity.level().playSound(null, entity, IESounds.glider.get(), SoundSource.PLAYERS, 1, 1);

				if(nextFlightTick%20==0)
				{
					stack.hurtAndBreak(itemDamage, entity, e -> e.broadcastBreakEvent(net.minecraft.world.entity.EquipmentSlot.CHEST));
					// Unlike an Elytra, this glider can completely break due to the extra damage at speed
					if(itemDamage>1 && !ElytraItem.isFlyEnabled(stack))
						stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(net.minecraft.world.entity.EquipmentSlot.CHEST));
				}
				entity.gameEvent(GameEvent.ELYTRA_GLIDE);
			}
		}
		return true;
	}


	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack)
	{
		return EquipmentSlot.CHEST;
	}

	@Override
	@NotNull
	public EquipmentSlot getEquipmentSlot()
	{
		return EquipmentSlot.CHEST;
	}
}
