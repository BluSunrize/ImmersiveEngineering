/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;


import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class ImmersiveConnectableTileEntity extends IEBaseTileEntity implements IImmersiveConnectable
{
	protected GlobalWireNetwork globalNet;

	public ImmersiveConnectableTileEntity(TileEntityType<? extends ImmersiveConnectableTileEntity> type)
	{
		super(type);
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return getPos();
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		this.markDirty();
	}

	@Override
	public void setWorldAndPos(@Nonnull World worldIn, @Nonnull BlockPos pos)
	{
		super.setWorldAndPos(worldIn, pos);
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset)
	{
		return new ConnectionPoint(pos, 0);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
	}

	@Nonnull
	@Override
	public IModelData getModelData()
	{
		return CombinedModelData.combine(
				new SinglePropertyModelData<>(
						ConnectorTileHelper.genConnBlockState(world, this), Model.CONNECTIONS
				), super.getModelData()
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
		ConnectorTileHelper.onChunkLoad(this, world);
	}

	@Override
	public void remove()
	{
		super.remove();
		ConnectorTileHelper.remove(world, this);
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, 0));
	}

	private final Int2ObjectMap<LocalWireNetwork> cachedLocalNets = new Int2ObjectArrayMap<>();

	protected LocalWireNetwork getLocalNet(int cpIndex)
	{
		return ConnectorTileHelper.getLocalNetWithCache(globalNet, getPos(), cpIndex, cachedLocalNets);
	}

	@Override
	public BlockPos getPosition()
	{
		return pos;
	}
}
