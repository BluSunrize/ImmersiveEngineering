package blusunrize.immersiveengineering.common.util;

import com.google.common.base.Preconditions;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class WorldMap<Key, Value>
{
	private final Map<RegistryKey<World>, Map<Key, Value>> map = new HashMap<>();

	@Nullable
	public Value get(World world, Key key)
	{
		Preconditions.checkArgument(!world.isRemote);
		final Map<Key, Value> worldMap = map.get(world.getDimensionKey());
		if(worldMap==null)
			return null;
		return worldMap.get(key);
	}

	public void put(World world, Key key, Value value)
	{
		Preconditions.checkArgument(!world.isRemote);
		final Map<Key, Value> worldMap = map.computeIfAbsent(world.getDimensionKey(), $ -> new HashMap<>());
		worldMap.put(key, value);
	}

	public void clearDimension(World world)
	{
		Preconditions.checkArgument(!world.isRemote);
		map.remove(world.getDimensionKey());
	}

	public void clear()
	{
		map.clear();
	}
}
