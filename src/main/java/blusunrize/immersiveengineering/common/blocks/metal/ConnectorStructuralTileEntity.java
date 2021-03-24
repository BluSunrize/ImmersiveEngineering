/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.api.wires.WireType.STRUCTURE_CATEGORY;

public class ConnectorStructuralTileEntity extends ImmersiveConnectableTileEntity implements IHammerInteraction,
		IOBJModelCallback<BlockState>, IBlockBounds, IStateBasedDirectional
{
	public float rotation = 0;

	public ConnectorStructuralTileEntity()
	{
		super(IETileTypes.CONNECTOR_STRUCTURAL.get());
	}

	public ConnectorStructuralTileEntity(TileEntityType<? extends ConnectorStructuralTileEntity> type)
	{
		super(type);
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, Vector3d hitVec)
	{
		if(!world.isRemote)
		{
			rotation += player.isSneaking()?-22.5f: 22.5f;
			rotation %= 360;
			markDirty();
			world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 254, 0);
		}
		return true;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putFloat("rotation", rotation);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		rotation = nbt.getFloat("rotation");
		if(world!=null&&world.isRemote)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = .03125;
		return new Vector3d(.5+side.getXOffset()*(-.125-conRadius),
				.5+side.getYOffset()*(-.125-conRadius),
				.5+side.getZOffset()*(-.125-conRadius));
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		//TODO are ropes and cables meant to be mixed?
		return STRUCTURE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public TransformationMatrix applyTransformations(BlockState object, String group, TransformationMatrix transform)
	{
		return transform.compose(new TransformationMatrix(
				new Vector3f(0, 0, 0),
				new Quaternion(0, rotation, 0, true),
				null, null
		));
	}

	@Override
	public String getCacheKey(BlockState object)
	{
		return Float.toString(rotation);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return EnergyConnectorTileEntity.getConnectorBounds(getFacing(), .3125F, .5F);
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
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}
}