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
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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
	private static final Map<DimensionType, Set<ISpawnInterdiction>> interdictionTiles = new HashMap<>();

	@SubscribeEvent
	public static void onEnderTeleport(EnderTeleportEvent event)
	{
		if(event.getEntityLiving().getType().getClassification()==EntityClassification.MONSTER)
		{
			synchronized(interdictionTiles)
			{
				Set<ISpawnInterdiction> dimSet = interdictionTiles.get(event.getEntity().world.getDimension().getType());
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
							else if(((TileEntity)interdictor).getDistanceSq(event.getEntity().getPosX(), event.getEntity().getPosY(), event.getEntity().getPosZ()) <= interdictor.getInterdictionRangeSquared())
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
				DimensionType dimension = event.getEntity().world.getDimension().getType();
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
							else if(((TileEntity)interdictor).getDistanceSq(event.getEntity().getPosX(), event.getEntity().getPosY(), event.getEntity().getPosZ()) <= interdictor.getInterdictionRangeSquared())
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
			Set<ISpawnInterdiction> inDimension = interdictionTiles.get(tile.getWorld().getDimension().getType());
			if(inDimension!=null)
				inDimension.remove(tile);
		}
	}

	public static <T extends TileEntity & ISpawnInterdiction>
	void addInterdictionTile(T tile)
	{
		World world = tile.getWorld();
		if(world!=null&&IEConfig.MACHINES.floodlight_spawnPrevent.get())
			synchronized(interdictionTiles)
			{
				Set<ISpawnInterdiction> forDim = interdictionTiles.computeIfAbsent(
						world.getDimension().getType(), x -> new HashSet<>()
				);
				forDim.add(tile);
			}
	}
}
