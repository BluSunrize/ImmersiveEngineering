/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class IEFenceBlock extends FenceBlock
{
	public IEFenceBlock(String name, Properties properties)
	{
		super(properties);
		setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, name));
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new BlockItemIE(this));
	}

	public Map<Direction, BooleanProperty> getFacingStateMap()
	{
		return PROPERTY_BY_DIRECTION;
	}
}
