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
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.OreConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

import java.util.List;
import java.util.Random;

public class IECountPlacement extends RepeatingPlacement
{
	public static final Codec<IECountPlacement> CODEC = RecordCodecBuilder.create(
			app -> app.group(
					Codec.list(Codec.STRING).fieldOf("count").forGetter(f -> f.count)
			).apply(app, IECountPlacement::new)
	);
	private final List<String> count;

	public IECountPlacement(List<String> count)
	{
		this.count = count;
	}

	public IECountPlacement(OreConfig config)
	{
		this(config.veinsPerChunk.getBase().getPath());
	}

	//TODO why is this constant? Was it constant before or did I mess up the port?
	@Override
	protected int count(Random p_191913_, BlockPos p_191914_)
	{
		return IEServerConfig.getRawConfig().getInt(count);
	}

	@Override
	public PlacementModifierType<?> type()
	{
		return IEWorldGen.IE_COUNT_PLACEMENT;
	}
}

