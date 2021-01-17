/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.SafeChunkUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.*;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID)
public class NetworkSanitizer
{
	private static final Map<IWorld, Set<ChunkPos>> toSanitize = new WeakHashMap<>();

	// TODO sanitize more errors? This seems to make the networks "validate-clean" for all cases of broken
	// data I have
	static void tick(IWorld w, GlobalWireNetwork global)
	{
		synchronized(toSanitize)
		{
			Set<ChunkPos> relevant = toSanitize.get(w);
			if(relevant==null)
				return;
			for(Iterator<ChunkPos> iterator = relevant.iterator(); iterator.hasNext(); )
			{
				ChunkPos chunk = iterator.next();
				if(!SafeChunkUtils.isChunkSafe(w, chunk.asBlockPos()))
					continue;
				Collection<ConnectionPoint> extraCPs = new ArrayList<>();
				Set<BlockPos> missingConnectors = new HashSet<>();
				for(ConnectionPoint cp : global.getAllConnectorsIn(chunk))
					if(!missingConnectors.contains(cp.getPosition()))
					{
						TileEntity inWorld = w.getTileEntity(cp.getPosition());
						if(!(inWorld instanceof IImmersiveConnectable))
							missingConnectors.add(cp.getPosition());
						else
						{
							IImmersiveConnectable iicWorld = (IImmersiveConnectable)inWorld;
							if(!iicWorld.getConnectionPoints().contains(cp))
								extraCPs.add(cp);
						}
					}
				for(ConnectionPoint cp : extraCPs)
					global.removeCP(cp);
				for(BlockPos pos : missingConnectors)
					global.removeConnector(pos);
				if(!extraCPs.isEmpty()||!missingConnectors.isEmpty())
					IELogger.logger.info("Removed {} extra connection points and {} missing connectors",
							extraCPs.size(), missingConnectors.size());
				iterator.remove();
			}
		}
	}

	@SubscribeEvent
	public static void chunkLoad(ChunkEvent.Load ev)
	{
		synchronized(toSanitize)
		{
			toSanitize.computeIfAbsent(ev.getWorld(), w -> new HashSet<>())
					.add(ev.getChunk().getPos());
		}
	}

	@SubscribeEvent
	public static void chunkUnload(ChunkEvent.Unload ev)
	{
		synchronized(toSanitize)
		{
			Set<ChunkPos> forWorld = toSanitize.get(ev.getWorld());
			if(forWorld!=null)
				forWorld.remove(ev.getChunk().getPos());
		}
	}
}
