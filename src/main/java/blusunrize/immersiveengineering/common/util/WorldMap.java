/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class WorldMap<Key, Value>
{
	private final Map<ResourceKey<Level>, Map<Key, Value>> map = new HashMap<>();

	@Nullable
	public Value get(Level world, Key key)
	{
		Preconditions.checkArgument(!world.isClientSide);
		final Map<Key, Value> worldMap = map.get(world.dimension());
		if(worldMap==null)
			return null;
		return worldMap.get(key);
	}

	public void put(Level world, Key key, Value value)
	{
		Preconditions.checkArgument(!world.isClientSide);
		final Map<Key, Value> worldMap = map.computeIfAbsent(world.dimension(), $ -> new HashMap<>());
		worldMap.put(key, value);
	}

	public void clearDimension(Level world)
	{
		Preconditions.checkArgument(!world.isClientSide);
		map.remove(world.dimension());
	}

	public void clear()
	{
		map.clear();
	}
}
