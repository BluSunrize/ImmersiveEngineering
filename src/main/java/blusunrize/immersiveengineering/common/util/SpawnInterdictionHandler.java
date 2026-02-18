/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.Raytracer;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent.SpawnPlacementCheck;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent.SpawnPlacementCheck.Result;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.village.VillageSiegeEvent;

import java.util.*;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.GAME)
public class SpawnInterdictionHandler
{
	private static final Map<ResourceKey<Level>, Set<ISpawnInterdiction>> interdictionTiles = new HashMap<>();

	@SubscribeEvent
	public static void onEnderTeleport(EntityTeleportEvent.EnderEntity event)
	{
		LivingEntity living = event.getEntityLiving();
		if(shouldCancel(null, living.getType(), living.blockPosition(), living.level())||living.getEffect(IEPotions.STUNNED)!=null)
			event.setCanceled(true);
		else if(checkForLeadedBlocks(event, living))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEnderpearlTeleport(EntityTeleportEvent.EnderPearl event)
	{
		if(checkForLeadedBlocks(event, event.getPlayer()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onChorusTeleport(EntityTeleportEvent.ChorusFruit event)
	{
		if(checkForLeadedBlocks(event, event.getEntityLiving()))
			event.setCanceled(true);
	}

	private static boolean checkForLeadedBlocks(EntityTeleportEvent event, LivingEntity living)
	{
		Level level = living.level();
		Set<BlockPos> blocksBetween = Raytracer.rayTrace(living.getEyePosition(), event.getTarget(), level);
		return blocksBetween.stream().anyMatch(blockPos -> {
			BlockState blockState = level.getBlockState(blockPos);
			return blockState.is(IEBlocks.StoneDecoration.CONCRETE_LEADED.get());
		});
	}

	@SubscribeEvent
	public static void onVillageSiegeSpawnCheck(VillageSiegeEvent event)
	{
		event.setCanceled(isInterdictedPosition(event.getAttemptedSpawnPos(), event.getLevel()));
	}

	@SubscribeEvent
	public static void onEntitySpawnCheck(SpawnPlacementCheck event)
	{
		if(shouldCancel(event.getSpawnType(), event.getEntityType(), event.getPos(), event.getLevel().getLevel()))
			event.setResult(Result.FAIL);
	}

	/**
	 * Mob spawn types are as followed:
	 * Natural - standard spawning
	 * Jockey - chicken jockey, etc
	 * Triggered - warden, skeleton horses
	 * Reinforcement - zombie summoning reinforcements
	 * Chunk Generation - spawning of mobs from worldgen (which may be in range, but is unlikely)
	 * Structure - things like Pillagers spawning in their outposts; you can take over one by lighting it now
	 * We do not block Event because it can contain raids, and blocking those causes issues (see #6321)
	 * Event would include Zombie Sieges, thus why we have a check to block sieges in range directly.
	 * We do not block Conversion because converting villagers is not "spawning" & it causes issues (see #6344)
	 * We do not block Patrol because light would have no result on that, they can spawn in daylight
	 * We do not block spawners (either type) because that restricts the utility of spawners in bases with lanterns
	 * We do not block other types because they are intentional spawns
	 */
	private static final List<MobSpawnType> BLOCKED_TYPES = List.of(MobSpawnType.NATURAL, MobSpawnType.JOCKEY, MobSpawnType.TRIGGERED, MobSpawnType.REINFORCEMENT, MobSpawnType.CHUNK_GENERATION, MobSpawnType.STRUCTURE);

	private static boolean shouldCancel(MobSpawnType spawnType, EntityType<?> type, BlockPos pos, Level level)
	{
		if((spawnType != null && !BLOCKED_TYPES.contains(spawnType)) || type.getCategory()!=MobCategory.MONSTER)
			return false;
		return isInterdictedPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ()), level);
	}

	private static boolean isInterdictedPosition(Vec3 pos, Level level)
	{
		synchronized(interdictionTiles)
		{
			if(!interdictionTiles.containsKey(level.dimension()))
				return false;
			Iterator<ISpawnInterdiction> it = interdictionTiles.get(level.dimension()).iterator();
			while(it.hasNext())
			{
				ISpawnInterdiction interdictor = it.next();
				if(interdictor instanceof BlockEntity interdictorTE)
				{
					if(interdictorTE.isRemoved()||interdictorTE.getLevel()==null)
						it.remove();
					else if(SafeChunkUtils.isChunkSafe(interdictorTE.getLevel(), interdictorTE.getBlockPos()))
					{
						Vec3 tilePos = Vec3.atCenterOf(interdictorTE.getBlockPos());
						if(tilePos.distanceToSqr(pos) <= interdictor.getInterdictionRangeSquared())
							return true;
					}
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	public static void onWorldUnload(LevelEvent.Unload event)
	{
		if(event.getLevel().isClientSide()||!(event.getLevel() instanceof Level realLevel))
			return;
		synchronized(interdictionTiles)
		{
			interdictionTiles.remove(realLevel.dimension());
		}
	}

	public static <T extends BlockEntity & ISpawnInterdiction>
	void removeFromInterdictionTiles(T tile)
	{
		Level level = tile.getLevel();
		if(level!=null&&!level.isClientSide)
			synchronized(interdictionTiles)
			{
				Set<ISpawnInterdiction> inDimension = interdictionTiles.get(level.dimension());
				if(inDimension!=null)
					inDimension.remove(tile);
			}
	}

	public static <T extends BlockEntity & ISpawnInterdiction>
	void addInterdictionTile(T tile)
	{
		Level world = tile.getLevel();
		if(world!=null&&!world.isClientSide()&&IEServerConfig.MACHINES.floodlight_spawnPrevent.get())
			synchronized(interdictionTiles)
			{
				Set<ISpawnInterdiction> forDim = interdictionTiles.computeIfAbsent(
						world.dimension(), x -> new HashSet<>()
				);
				forDim.add(tile);
			}
	}
}
