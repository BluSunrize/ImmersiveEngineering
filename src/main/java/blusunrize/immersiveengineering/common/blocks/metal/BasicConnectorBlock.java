/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Locale;

public class BasicConnectorBlock<T extends TileEntity & IImmersiveConnectable> extends MiscConnectableBlock<T>
{
	public BasicConnectorBlock(String name, RegistryObject<TileEntityType<T>> type)
	{
		super(name, type);
	}

	public static BasicConnectorBlock<EnergyConnectorTileEntity> forPower(String voltage, boolean relay)
	{
		return new BasicConnectorBlock<>("connector_"+voltage.toLowerCase(Locale.US)+(relay?"_relay": ""),
				EnergyConnectorTileEntity.SPEC_TO_TYPE.get(Pair.of(voltage, relay))
		);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
	}
}
