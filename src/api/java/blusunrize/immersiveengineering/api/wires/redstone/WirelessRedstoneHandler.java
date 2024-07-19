/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.redstone;

import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class WirelessRedstoneHandler
{
	private final static Map<ServerLevel, WirelessRedstoneHandler> LEVEL_CACHE = new ConcurrentHashMap();

	public static WirelessRedstoneHandler getHandler(ServerLevel level)
	{
		return LEVEL_CACHE.computeIfAbsent(level, WirelessRedstoneHandler::new);
	}

	private final ServerLevel level;
	private final Map<BlockPos, IWirelessRedstoneComponent> registeredComponents = new HashMap<>();

	public WirelessRedstoneHandler(ServerLevel level)
	{
		this.level = level;
	}

	public void register(BlockPos pos, IWirelessRedstoneComponent component)
	{
		this.registeredComponents.put(pos, component);
	}

	public boolean isRegistered(BlockPos pos)
	{
		return this.registeredComponents.containsKey(pos);
	}

	public void sendSignal(BlockPos fromPos, IWirelessRedstoneComponent fromComponent, byte[] signal)
	{
		this.registeredComponents.entrySet().stream()
				.filter(target -> SafeChunkUtils.isChunkSafe(level, target.getKey())) // filter to loaded
				.filter(target -> target.getValue().getFrequency()==fromComponent.getFrequency()) // filter to same frequency
				.filter(target -> !fromPos.equals(target.getKey()))
				.filter(target -> {
					// check both points to be within distance
					double chunkDist = fromPos.distSqr(target.getKey())/256; // divide by 16²
					return chunkDist < target.getValue().getChunkRangeSq()&&chunkDist < fromComponent.getChunkRangeSq();
				})
				.forEach(target -> target.getValue().receiveSignal(signal));
	}

	public List<Vec3> getRelativeComponentsInRange(BlockPos fromPos, IWirelessRedstoneComponent fromComponent)
	{
		final Vec3 fromVec = Vec3.atCenterOf(fromPos);
		return this.registeredComponents.entrySet().stream()
				.filter(target -> target.getValue().getFrequency()==fromComponent.getFrequency()) // filter to same frequency
				.filter(target -> !fromPos.equals(target.getKey()))
				.filter(target -> {
					// check both points to be within distance
					double chunkDist = fromPos.distSqr(target.getKey())/256; // divide by 16²
					return chunkDist < target.getValue().getChunkRangeSq()&&chunkDist < fromComponent.getChunkRangeSq();
				})
				.map(Entry::getKey)
				.map(blockPos -> Vec3.atCenterOf(blockPos).subtract(fromVec))
				.toList();
	}

	public interface IWirelessRedstoneComponent
	{
		int getChunkRangeSq();

		int getFrequency();

		void receiveSignal(byte[] signal);
	}
}
