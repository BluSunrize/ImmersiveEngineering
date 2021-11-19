/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.excavator;

import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class MineralWorldInfo
{
	private final List<Pair<MineralVein, Integer>> mineralVeins;
	private final int totalWeight;

	public MineralWorldInfo(List<Pair<MineralVein, Integer>> mineralVeins)
	{
		this.mineralVeins = mineralVeins;
		this.totalWeight = mineralVeins.stream().map(Pair::getSecond).reduce(Integer::sum).orElse(0);
	}

	@Nullable
	public MineralVein getMineralVein(Random rand)
	{
		MineralVein vein = null;
		if(this.totalWeight==0)
			return null;
		int weight = rand.nextInt(this.totalWeight);
		for(Pair<MineralVein, Integer> pair : this.mineralVeins)
		{
			weight -= pair.getSecond();
			if(weight < 0)
			{
				vein = pair.getFirst();
				break;
			}
		}
		return vein;
	}

	public List<Pair<MineralVein, Integer>> getAllVeins()
	{
		return mineralVeins;
	}

	public int getTotalWeight()
	{
		return totalWeight;
	}
}
