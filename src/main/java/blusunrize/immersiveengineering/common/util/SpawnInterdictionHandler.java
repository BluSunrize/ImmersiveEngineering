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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.*;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.FORGE)
public class SpawnInterdictionHandler
{
	private static final Map<RegistryKey<World>, Set<ISpawnInterdiction>> interdictionTiles = new HashMap<>();

	@SubscribeEvent
	public static void onEnderTeleport(EnderTeleportEvent event)
	{
		if(shouldCancel(event.getEntity())||event.getEntityLiving().getActivePotionEffect(IEPotions.stunned.get())!=null)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEntitySpawnCheck(LivingSpawnEvent.CheckSpawn event)
	{
		if(event.getResult()==Event.Result.ALLOW||event.getResult()==Event.Result.DENY
				||event.isSpawner())
			return;
		if(shouldCancel(event.getEntity()))
			event.setResult(Event.Result.DENY);
	}

	private static boolean shouldCancel(Entity entity)
	{
		if(entity.getType().getClassification()!=EntityClassification.MONSTER)
			return false;
		RegistryKey<World> dimension = entity.world.getDimensionKey();
		synchronized(interdictionTiles)
		{
			if(!interdictionTiles.containsKey(dimension))
				return false;
			Iterator<ISpawnInterdiction> it = interdictionTiles.get(dimension).iterator();
			while(it.hasNext())
			{
				ISpawnInterdiction interdictor = it.next();
				if(interdictor instanceof TileEntity)
				{
					TileEntity interdictorTE = (TileEntity)interdictor;
					if(interdictorTE.isRemoved()||interdictorTE.getWorld()==null)
						it.remove();
					else if(SafeChunkUtils.isChunkSafe(interdictorTE.getWorld(), interdictorTE.getPos()))
					{
						Vector3d tilePos = Vector3d.copyCentered(interdictorTE.getPos());
						if(tilePos.squareDistanceTo(entity.getPositionVec()) <= interdictor.getInterdictionRangeSquared())
							return true;
					}
				}
			}
		}
		return false;
	}

	public static <T extends TileEntity & ISpawnInterdiction>
	void removeFromInterdictionTiles(T tile)
	{
		synchronized(interdictionTiles)
		{
			Set<ISpawnInterdiction> inDimension = interdictionTiles.get(tile.getWorld().getDimensionKey());
			if(inDimension!=null)
				inDimension.remove(tile);
		}
	}

	public static <T extends TileEntity & ISpawnInterdiction>
	void addInterdictionTile(T tile)
	{
		World world = tile.getWorld();
		if(world!=null&&IEServerConfig.MACHINES.floodlight_spawnPrevent.get())
			synchronized(interdictionTiles)
			{
				Set<ISpawnInterdiction> forDim = interdictionTiles.computeIfAbsent(
						world.getDimensionKey(), x -> new HashSet<>()
				);
				forDim.add(tile);
			}
	}
}
