/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.conveyor.BasicConveyorType;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.client.render.conveyor.BasicConveyorRender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BasicConveyor extends ConveyorBase
{
	public static final ResourceLocation NAME = new ResourceLocation(ImmersiveEngineering.MODID, "basic");
	public static final IConveyorType<BasicConveyor> TYPE = new BasicConveyorType<>(
			NAME, false, true, BasicConveyor::new, () -> new BasicConveyorRender<>(texture_on, texture_off)
	);

	public BasicConveyor(BlockEntity tile)
	{
		super(tile);
	}

	@Override
	public IConveyorType<BasicConveyor> getType()
	{
		return TYPE;
	}
}
