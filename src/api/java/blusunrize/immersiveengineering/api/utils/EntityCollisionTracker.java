/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;


import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.Entity;

public class EntityCollisionTracker
{
	private final int ticksInMemory;
	private final TrackedTick[] collidingByTick;
	private final IntSet collidedInRange = new IntOpenHashSet();
	private int nextIndex = 0;
	private TrackedTick collidingInCurrentTick = new TrackedTick();

	public EntityCollisionTracker(int ticksInMemory)
	{
		this.ticksInMemory = ticksInMemory;
		this.collidingByTick = new TrackedTick[ticksInMemory];
		for(int i = 0; i < ticksInMemory; ++i)
			collidingByTick[i] = new TrackedTick();
	}

	private void updateForTick(long currentTick)
	{
		boolean updateRange = false;
		if(currentTick!=collidingInCurrentTick.tick)
		{
			collidingByTick[nextIndex] = collidingInCurrentTick;
			nextIndex = (nextIndex+1)%ticksInMemory;
			collidingInCurrentTick = new TrackedTick(currentTick);
			updateRange = true;
		}
		for(int i = 0; i < ticksInMemory; ++i)
		{
			if(!collidingByTick[i].isRelevant(currentTick))
			{
				updateRange = true;
				collidingByTick[i] = new TrackedTick();
			}
		}
		if(updateRange)
		{
			collidedInRange.clear();
			for(TrackedTick tick : collidingByTick)
				collidedInRange.addAll(tick.entities);
		}
	}

	public void onEntityCollided(Entity collided)
	{
		updateForTick(collided.level.getGameTime());
		collidingInCurrentTick.entities.add(collided.getId());
	}

	public int getCollidedInRange(long currentTick)
	{
		updateForTick(currentTick);
		return collidedInRange.size();
	}

	private class TrackedTick
	{
		private final static long INVALID_TICK = -1;
		private final IntSet entities;
		private final long tick;

		private TrackedTick()
		{
			this(INVALID_TICK);
		}

		private TrackedTick(long tick)
		{
			this.tick = tick;
			this.entities = new IntOpenHashSet();
		}

		public boolean isRelevant(long currentTick)
		{
			return tick==INVALID_TICK||currentTick-this.tick <= ticksInMemory+3;
		}
	}
}
