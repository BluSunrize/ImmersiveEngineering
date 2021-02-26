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
		if(event.getEntityLiving().getType().getClassification()==EntityClassification.MONSTER)
		{
			synchronized(interdictionTiles)
			{
				Set<ISpawnInterdiction> dimSet = interdictionTiles.get(event.getEntity().world.getDimensionKey());
				if(dimSet!=null)
				{
					Iterator<ISpawnInterdiction> it = dimSet.iterator();
					while(it.hasNext())
					{
						ISpawnInterdiction interdictor = it.next();
						if(interdictor instanceof TileEntity)
						{
							if(((TileEntity)interdictor).isRemoved()||((TileEntity)interdictor).getWorld()==null)
								it.remove();
							else if(Vector3d.copy(((TileEntity)interdictor).getPos()).squareDistanceTo(event.getEntity().getPositionVec()) <= interdictor.getInterdictionRangeSquared())
								event.setCanceled(true);
						}
						else if(interdictor instanceof Entity)
						{
							if(!((Entity)interdictor).isAlive()||((Entity)interdictor).world==null)
								it.remove();
							else if(((Entity)interdictor).getDistanceSq(event.getEntity()) <= interdictor.getInterdictionRangeSquared())
								event.setCanceled(true);
						}
					}
				}
			}
		}
		if(event.getEntityLiving().getActivePotionEffect(IEPotions.stunned)!=null)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEntitySpawnCheck(LivingSpawnEvent.CheckSpawn event)
	{
		if(event.getResult()==Event.Result.ALLOW||event.getResult()==Event.Result.DENY
				||event.isSpawner())
			return;
		if(event.getEntityLiving().getType().getClassification()==EntityClassification.MONSTER)
		{
			synchronized(interdictionTiles)
			{
				RegistryKey<World> dimension = event.getEntity().world.getDimensionKey();
				if(interdictionTiles.containsKey(dimension))
				{
					Iterator<ISpawnInterdiction> it = interdictionTiles.get(dimension).iterator();
					while(it.hasNext())
					{
						ISpawnInterdiction interdictor = it.next();
						if(interdictor instanceof TileEntity)
						{
							if(((TileEntity)interdictor).isRemoved()||((TileEntity)interdictor).getWorld()==null)
								it.remove();
							else if(Vector3d.copy(((TileEntity)interdictor).getPos()).squareDistanceTo(event.getEntity().getPositionVec()) <= interdictor.getInterdictionRangeSquared())
							{
								event.setResult(Event.Result.DENY);
								break;
							}
						}
					}
				}
			}
		}
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
