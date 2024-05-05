/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.VeinType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class IECountPlacement extends RepeatingPlacement
{
	public static final MapCodec<IECountPlacement> CODEC = VeinType.CODEC.xmap(IECountPlacement::new, p -> p.type);
	private final VeinType type;

	public IECountPlacement(VeinType type)
	{
		this.type = type;
	}

	//TODO why is this constant? Was it constant before or did I mess up the port?
	@Override
	protected int count(RandomSource p_191913_, BlockPos p_191914_)
	{
		return IEServerConfig.ORES.ores.get(type).veinsPerChunk.get();
	}

	@Override
	public PlacementModifierType<?> type()
	{
		return IEWorldGen.IE_COUNT_PLACEMENT.value();
	}
}

