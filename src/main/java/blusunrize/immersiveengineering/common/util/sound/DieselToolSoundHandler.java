/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.items.DieselToolItem;
import blusunrize.immersiveengineering.common.network.MessageDieselToolAttack;
import blusunrize.immersiveengineering.common.network.MessageDieselToolHarvestUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.FORGE)
public class DieselToolSoundHandler
{
	private static Map<LivingEntity, Map<EquipmentSlot, DieselToolSoundGroup>> dieselToolSoundGroups = new HashMap<>();

	private static Map<EquipmentSlot, DieselToolSoundGroup> getSafeDTSGs(LivingEntity entity)
	{
		Map<EquipmentSlot, DieselToolSoundGroup> result = dieselToolSoundGroups.get(entity);
		if(result!=null)
			return result;
		if(isUsableDieselItem(entity.getMainHandItem()) || isUsableDieselItem(entity.getOffhandItem()))
		{
			result = new HashMap<EquipmentSlot, DieselToolSoundGroup>();
			dieselToolSoundGroups.put(entity, result);
		}
		return result;
	}

	/**
	 * For the given entity and slot: creates or returns an existing sound group, or null, if there should be none.
	 * Turns off sound groups that are obsolete and removes them from the mapping.
	 * This should generally be the only point where DieselToolSoundGroups are accessed through.
	 *
	 * @param entity
	 * @param slot
	 * @return a DieselToolSoundGroup for the given slot or null, if the provided slot does not hold a suitable item
	 */
	@Nullable
	public static DieselToolSoundGroup getSafeDTSG(LivingEntity entity, EquipmentSlot slot)
	{
		assert slot.getType().equals(Type.HAND);
		Map<EquipmentSlot, DieselToolSoundGroup> dtsgs = getSafeDTSGs(entity);
		if (dtsgs == null)
			return null;
		DieselToolSoundGroup soundGroup = dtsgs.get(slot);

		ItemStack handItem = entity.getItemBySlot(slot);

		if(soundGroup!=null)
		{
			if(!soundGroup.checkItemMatch(handItem))
			{
				dtsgs.remove(slot);
				soundGroup = null;
				if(dtsgs.isEmpty())
					dieselToolSoundGroups.remove(entity);
			}
		}
		else if(handItem.getItem() instanceof DieselToolItem dieselItem&&dieselItem.canToolBeUsed(handItem))
		{
			soundGroup = new DieselToolSoundGroup(dieselItem, entity, slot);
			dtsgs.put(slot, soundGroup);
		}

		return soundGroup;
	}

	public static void handleHarvestAction(LivingEntity holder, LeftClickBlock.Action action, BlockPos targetBlockPos)
	{
		DieselToolSoundGroup dtsg = getSafeDTSG(holder, EquipmentSlot.MAINHAND);

		if(dtsg!=null)
		{
			switch(action)
			{
				case START:
					dtsg.updateHarvestState(targetBlockPos);
					break;
				case STOP: // stop and abort fire only on the server / are sent from the server, and are non-client-main-player events
				case ABORT:
					dtsg.updateHarvestState(null);
					break;
				case CLIENT_HOLD: // fires only on the client
					dtsg.updateHarvestState(targetBlockPos);
					break;
			}
		}
	}

	public static void handleAttack(LivingEntity holder)
	{
		DieselToolSoundGroup dtsg = getSafeDTSG(holder, EquipmentSlot.MAINHAND);

		if(dtsg!=null)
			dtsg.triggerMotorAttack();
	}

	public static boolean isUsableDieselItem(ItemStack candidate)
	{
		if(candidate.getItem() instanceof DieselToolItem dieselToolItem&&dieselToolItem.canToolBeUsed(candidate))
			return true;
		return false;
	}

	@SubscribeEvent
	public static void toolHeldCheck(LivingTickEvent ev)
	{
		LivingEntity holder = ev.getEntity();

		if(!holder.level().isClientSide()) // client side only
			return;

		DieselToolSoundGroup dtsgMain = DieselToolSoundHandler.getSafeDTSG(holder, EquipmentSlot.MAINHAND);
		DieselToolSoundGroup dtsgOff = DieselToolSoundHandler.getSafeDTSG(holder, EquipmentSlot.OFFHAND);

		// TODO: should this be implicit anyways? as soon as it is switched off it is unhooked, so as long as it is returned here, it must be running?
		if(dtsgMain!=null)
			dtsgMain.switchMotorOnOff(true);
		if(dtsgOff!=null)
			dtsgOff.switchMotorOnOff(true);
	}

	@SubscribeEvent
	public static void harvestCheck(LeftClickBlock ev)
	{
		if(isUsableDieselItem(ev.getItemStack()))
		{
			LivingEntity holder = ev.getEntity();
			BlockPos targetPos = ev.getPos();
			if(ev.getLevel().isClientSide()&&holder.equals(Minecraft.getInstance().player))
				handleHarvestAction(holder, ev.getAction(), targetPos);
			else
			{
				PacketDistributor.TRACKING_ENTITY.with(holder).send(new MessageDieselToolHarvestUpdate(holder, ev.getAction(), targetPos));
			}
		}
	}

	@SubscribeEvent
	public static void attackCheck(LivingAttackEvent ev)
	{
		if(ev.getSource()!=null&&ev.getSource().getEntity() instanceof LivingEntity holder&&isUsableDieselItem(holder.getItemBySlot(EquipmentSlot.MAINHAND)))
		{
			if(holder.level().isClientSide()&&holder.equals(Minecraft.getInstance().player))
			{
				handleAttack(holder);
			}
			else
			{
				PacketDistributor.TRACKING_ENTITY.with(holder).send(new MessageDieselToolAttack(holder));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void stopLeavingSoundSource(EntityLeaveLevelEvent ev)
	{
		Map<EquipmentSlot, DieselToolSoundGroup> dtsgs;
		if(ev.getLevel().isClientSide() && ev.getEntity() instanceof LivingEntity livingEntity && (dtsgs=dieselToolSoundGroups.remove(livingEntity))!=null)
		{
			for (DieselToolSoundGroup dtsg : dtsgs.values())
			{
				dtsg.switchMotorOnOff(false);
			}
		}
	}
}