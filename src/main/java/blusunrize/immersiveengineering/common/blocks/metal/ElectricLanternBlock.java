/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import net.minecraft.state.properties.BlockStateProperties;

public class ElectricLanternBlock extends MiscConnectorBlock<ElectricLanternTileEntity>
{
	public ElectricLanternBlock(String name)
	{
		super(name, IETileTypes.ELECTRIC_LANTERN, props -> props.setLightLevel(
				state -> state.get(IEProperties.ACTIVE)?15: 0
		), IEProperties.FACING_TOP_DOWN, IEProperties.ACTIVE, BlockStateProperties.WATERLOGGED);
	}
}
