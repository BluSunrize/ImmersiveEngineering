/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.client.utils.BasicClientProperties;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.OptionalDouble;
import java.util.function.Consumer;

public class BucketWheelMultiblock extends IETemplateMultiblock
{
	public BucketWheelMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/bucket_wheel"),
				new BlockPos(3, 3, 0), new BlockPos(3, 3, 0), new BlockPos(7, 7, 1),
				IEMultiblockLogic.BUCKET_WHEEL);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public void initializeClient(Consumer<MultiblockManualData> consumer)
	{
		consumer.accept(new BasicClientProperties(this, OptionalDouble.of(-Mth.HALF_PI)));
	}
}