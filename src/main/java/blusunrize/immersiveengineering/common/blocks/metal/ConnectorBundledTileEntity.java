/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static blusunrize.immersiveengineering.api.wires.WireType.REDSTONE_CATEGORY;

public class ConnectorBundledTileEntity extends ImmersiveConnectableTileEntity implements ITickableTileEntity, IStateBasedDirectional,
		IBlockBounds, IRedstoneConnector
{
	public ConnectorBundledTileEntity()
	{
		this(IETileTypes.CONNECTOR_BUNDLED.get());
	}

	public ConnectorBundledTileEntity(TileEntityType<? extends ConnectorBundledTileEntity> type)
	{
		super(type);
	}

	private CapabilityReference<RedstoneBundleConnection> attached = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(pos.offset(getFacing()), getFacing().getOpposite()),
			CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION);

	@Override
	public void tick()
	{
		if(hasWorld()&&!world.isRemote&&attached.isPresent()&&attached.get().pollDirty())
			globalNet.getLocalNet(pos)
					.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class)
					.updateValues();
	}

	@Override
	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		if(!world.isRemote&&attached.isPresent())
			attached.get().onChange(cp, handler, getFacing().getOpposite());
	}

	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		if(attached.isPresent())
		{
			attached.get().updateInput(signals, cp, getFacing().getOpposite());
		}
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		return REDSTONE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return MiscConnectorBlock.DEFAULT_FACING_PROP;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}


	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vector3d(.5-conRadius*side.getXOffset(), .5-conRadius*side.getYOffset(), .5-conRadius*side.getZOffset());
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		float length = .625f;
		float wMin = .3125f;
		return EnergyConnectorTileEntity.getConnectorBounds(getFacing(), wMin, length);
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers()
	{
		return ImmutableList.of(RedstoneNetworkHandler.ID);
	}
}