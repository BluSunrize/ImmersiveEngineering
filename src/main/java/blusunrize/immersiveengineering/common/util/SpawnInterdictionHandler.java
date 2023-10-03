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
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEPotions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.*;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.FORGE)
public class SpawnInterdictionHandler
{
	private static final Map<ResourceKey<Level>, Set<ISpawnInterdiction>> interdictionTiles = new HashMap<>();

	@SubscribeEvent
	public static void onEnderTeleport(EntityTeleportEvent.EnderEntity event)
	{
		if(shouldCancel(event.getEntity())||event.getEntityLiving().getEffect(IEPotions.STUNNED.get())!=null)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEntitySpawnCheck(MobSpawnEvent.FinalizeSpawn event)
	{
		if(event.getResult()==Event.Result.ALLOW||event.getResult()==Event.Result.DENY||event.getSpawner()!=null)
			return;
		if(shouldCancel(event.getEntity()))
			event.setResult(Event.Result.DENY);
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

	private static boolean shouldCancel(Entity entity)
	{
		if(entity.getType().getCategory()!=MobCategory.MONSTER)
			return false;
		ResourceKey<Level> dimension = entity.level().dimension();
		synchronized(interdictionTiles)
		{
			if(!interdictionTiles.containsKey(dimension))
				return false;
			Iterator<ISpawnInterdiction> it = interdictionTiles.get(dimension).iterator();
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
						if(tilePos.distanceToSqr(entity.position()) <= interdictor.getInterdictionRangeSquared())
							return true;
					}
				}
			}
		}
		return false;
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
