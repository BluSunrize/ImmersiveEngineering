/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.api.tool.INoisyTool;
import blusunrize.immersiveengineering.common.network.MessageNoisyToolAttack;
import blusunrize.immersiveengineering.common.network.MessageNoisyToolHarvestUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

@EventBusSubscriber(modid = MODID, bus = Bus.GAME)
public class NoisyToolSoundHandler
{
	private static final Map<LivingEntity, Map<EquipmentSlot, NoisyToolSoundGroup>> noisyToolSoundGroups = new HashMap<>();

	private static Map<EquipmentSlot, NoisyToolSoundGroup> getSafeNTSGs(LivingEntity entity)
	{
		Map<EquipmentSlot, NoisyToolSoundGroup> result = noisyToolSoundGroups.get(entity);
		if(result!=null)
			return result;
		if(INoisyTool.isAbleNoisyTool(entity.getMainHandItem())||INoisyTool.isAbleNoisyTool(entity.getOffhandItem()))
		{
			result = new HashMap<>();
			noisyToolSoundGroups.put(entity, result);
		}
		return result;
	}

	/**
	 * For the given entity and slot: creates or returns an existing sound group, or null, if there should be none.
	 * Turns off sound groups that are obsolete and removes them from the mapping.
	 * This should generally be the only point through which NoisyToolSoundGroups are accessed.
	 *
	 * @param entity the entity that's supposed to receive a NTSG
	 * @param slot the entities EquipmentSlot. <b>Must</b> be a Type.HAND type
	 * @return a NoisyToolSoundGroup for the given slot or null, if the provided slot does not hold a suitable item
	 */
	@Nullable
	public static NoisyToolSoundGroup getSafeNTSG(LivingEntity entity, EquipmentSlot slot)
	{
		assert slot.getType().equals(Type.HAND);
		Map<EquipmentSlot, NoisyToolSoundGroup> ntsgs = getSafeNTSGs(entity);
		if(ntsgs==null)
			return null;

		NoisyToolSoundGroup soundGroup = ntsgs.get(slot);
		ItemStack handItem = entity.getItemBySlot(slot);
		int hotbarSlot = slot.equals(EquipmentSlot.MAINHAND)&&entity instanceof Player player?player.getInventory().selected: -1;

		if(soundGroup!=null)
		{
			if(!soundGroup.checkItemValid(handItem, hotbarSlot))
			{
				ntsgs.remove(slot);
				soundGroup = null;
				if(ntsgs.isEmpty())
					noisyToolSoundGroups.remove(entity);
			}
		}
		else if(INoisyTool.isAbleNoisyTool(handItem))
		{
			soundGroup = new NoisyToolSoundGroup(handItem, entity, hotbarSlot);
			ntsgs.put(slot, soundGroup);
		}

		return soundGroup;
	}

	public static void handleHarvestAction(LivingEntity noisyToolHolder, LeftClickBlock.Action action, BlockPos targetBlockPos)
	{
		NoisyToolSoundGroup ntsg = getSafeNTSG(noisyToolHolder, EquipmentSlot.MAINHAND);

		if(ntsg!=null)
		{
			switch(action)
			{
				case START:
					ntsg.updateHarvestState(targetBlockPos);
					break;
				case STOP: // stop and abort fire only on the server / are sent from the server, and are non-client-main-player events
				case ABORT:
					ntsg.updateHarvestState(null);
					break;
				case CLIENT_HOLD: // fires only on the client
					ntsg.updateHarvestState(targetBlockPos);
					break;
			}
		}
	}

	public static void handleAttack(LivingEntity noisyToolHolder)
	{
		NoisyToolSoundGroup ntsg = getSafeNTSG(noisyToolHolder, EquipmentSlot.MAINHAND);

		if(ntsg!=null)
			ntsg.triggerAttack();
	}

	@SubscribeEvent // event not cancelable, so no issues
	public static void toolHeldCheck(Post ev)
	{
		if(ev.getEntity() instanceof LivingEntity noisyToolHolder)
		{
			if(!noisyToolHolder.level().isClientSide()) // client side only
				return;

			NoisyToolSoundGroup ntsgMainHand = NoisyToolSoundHandler.getSafeNTSG(noisyToolHolder, EquipmentSlot.MAINHAND);
			NoisyToolSoundGroup ntsgOffHand = NoisyToolSoundHandler.getSafeNTSG(noisyToolHolder, EquipmentSlot.OFFHAND);

			if(ntsgMainHand!=null)
				ntsgMainHand.switchMotorOnOff(true);
			if(ntsgOffHand!=null)
				ntsgOffHand.switchMotorOnOff(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST) //lowest priority, because if the event got cancelled, we don't wanna play the sound
	public static void harvestCheck(LeftClickBlock ev)
	{
		if(INoisyTool.isAbleNoisyTool(ev.getItemStack()))
		{
			LivingEntity noisyToolHolder = ev.getEntity();
			if(noisyToolHolder instanceof Player player&&player.isCreative()) // skip for creative players, because remote creative players don't send stop/abort on block break
			{
				return;
			}
			BlockPos targetPos = ev.getPos();
			if(ev.getLevel().isClientSide()&&noisyToolHolder.equals(Minecraft.getInstance().player))
			{
				handleHarvestAction(noisyToolHolder, ev.getAction(), targetPos);
			}
			else
			{
				PacketDistributor.sendToPlayersTrackingEntity(noisyToolHolder, new MessageNoisyToolHarvestUpdate(noisyToolHolder, ev.getAction(), targetPos));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST) //lowest priority, because if the event got cancelled, we don't wanna play the sound
	public static void clientSideAttackCheck(AttackEntityEvent ev)
	{
		if(ev.getTarget() instanceof LivingEntity) // a) for parity with LivingIncomingDamageEvent, b) MCs extra attack sounds also only happen on LivingEntities
		{
			Player player = ev.getEntity();
			if(player.level().isClientSide()&&INoisyTool.isAbleNoisyTool(player.getItemBySlot(EquipmentSlot.MAINHAND)))
			{
				handleAttack(player);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST) //highest priority, because if the damage get's cancelled, it still was an attack
	public static void serverSideAttackCheck(LivingIncomingDamageEvent ev)
	{
		// no null check for ev.getSource, because ev.getSource() is never null according to intelliJ: "Method 'getSource' inherits container annotation, thus 'non-null'"
		// All I see are final fields and no annotations, but should be the same thing.
		// if stuff burns some day down the line because that changes, here's a place to check, I guess
		if(ev.getSource().getEntity() instanceof LivingEntity noisyToolHolder&&INoisyTool.isAbleNoisyTool(noisyToolHolder.getItemBySlot(EquipmentSlot.MAINHAND)))
		{
			//sends the packet to every tracking player, except noisyToolHolder (if noisyToolHolder is a player)
			PacketDistributor.sendToPlayersTrackingEntity(noisyToolHolder, new MessageNoisyToolAttack(noisyToolHolder));
		}
	}

	/**
	 * handles stopping the sound instances and unlisting the sound group when the entity is removed
	 * consider checking entity.isRemoved() in the ticking sound instances (see MinecartSoundInstance.tick())
	 *
	 * only handled clientside because the server shouldn't have any NTSGS to begin with.
	 *
	 * @param ev the EntityLeaveLevelEvent event
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void stopLeavingSoundSource(EntityLeaveLevelEvent ev)
	{
		Map<EquipmentSlot, NoisyToolSoundGroup> ntsgs;
		if(ev.getLevel().isClientSide()&&ev.getEntity() instanceof LivingEntity livingEntity&&(ntsgs = noisyToolSoundGroups.remove(livingEntity))!=null)
		{
			for(NoisyToolSoundGroup ntsg : ntsgs.values())
			{
				ntsg.switchMotorOnOff(false);
			}
		}
	}

	/**
	 * Clear the list when exiting a level so it doesn't get carried over into other levels.
	 * Currently it is not checked if entities in the list still are tracked in game, they are only removed when they stop being tracked.
	 * This means better cleaning up when the player leaves the level, just to be sure no trash accumulates.
	 * <p>
	 * Also, left in server side, too, for safety, even though the server shouldn't build up any NTSGs anyways.
	 *
	 * @param ev the Unload event
	 */
	@SubscribeEvent
	public static void leaveLevel(Unload ev)
	{
		noisyToolSoundGroups.clear();
	}
}