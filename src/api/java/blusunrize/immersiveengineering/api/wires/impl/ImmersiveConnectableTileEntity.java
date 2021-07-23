/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.wires.impl;

import blusunrize.immersiveengineering.api.IEProperties.ConnectionModelData;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.api.wires.ConnectorTileHelper;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;

public abstract class ImmersiveConnectableTileEntity extends BlockEntity implements IImmersiveConnectable
{
	protected GlobalWireNetwork globalNet;

	public ImmersiveConnectableTileEntity(BlockEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void setLevelAndPosition(@Nonnull Level worldIn, @Nonnull BlockPos pos)
	{
		super.setLevelAndPosition(worldIn, pos);
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}

	@Nonnull
	@Override
	public IModelData getModelData()
	{
		ConnectionModelData state = ConnectorTileHelper.genConnBlockState(level, this);
		return CombinedModelData.combine(
				new SinglePropertyModelData<>(state, Model.CONNECTIONS), super.getModelData()
		);
	}

	@Override
	public void onChunkUnloaded()
	{
		super.onChunkUnloaded();
		ConnectorTileHelper.onChunkUnload(globalNet, this);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		ConnectorTileHelper.onChunkLoad(this, level);
	}

	@Override
	public void setRemoved()
	{
		super.setRemoved();
		ConnectorTileHelper.remove(level, this);
	}

	private final Int2ObjectMap<LocalWireNetwork> cachedLocalNets = new Int2ObjectArrayMap<>();

	protected LocalWireNetwork getLocalNet(int cpIndex)
	{
		return ConnectorTileHelper.getLocalNetWithCache(globalNet, getBlockPos(), cpIndex, cachedLocalNets);
	}
}
