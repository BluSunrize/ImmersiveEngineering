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
import blusunrize.immersiveengineering.api.wires.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import java.util.Optional;

import static blusunrize.immersiveengineering.api.wires.WireType.STRUCTURE_CATEGORY;

public class ConnectorStructuralTileEntity extends ImmersiveConnectableTileEntity implements IHammerInteraction,
		IOBJModelCallback<BlockState>, IBlockBounds, IStateBasedDirectional
{
	public float rotation = 0;

	public static TileEntityType<ConnectorStructuralTileEntity> TYPE;

	public ConnectorStructuralTileEntity()
	{
		super(TYPE);
	}

	public ConnectorStructuralTileEntity(TileEntityType<? extends ConnectorStructuralTileEntity> type)
	{
		super(type);
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
	{
		rotation += player.isSneaking()?-22.5f: 22.5f;
		rotation %= 360;
		markDirty();
		world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 254, 0);
		return true;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putFloat("rotation", rotation);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		rotation = nbt.getFloat("rotation");
		if(world!=null&&world.isRemote)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = .03125;
		return new Vec3d(.5+side.getXOffset()*(-.125-conRadius),
				.5+side.getYOffset()*(-.125-conRadius),
				.5+side.getZOffset()*(-.125-conRadius));
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		//TODO are ropes and cables meant to be mixed?
		return STRUCTURE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Optional<TRSRTransformation> applyTransformations(BlockState object, String group, Optional<TRSRTransformation> transform)
	{
		Matrix4 mat = transform.map(trsrTransformation -> new Matrix4(trsrTransformation.getMatrixVec())).orElseGet(Matrix4::new);
		mat = mat.translate(.5, 0, .5).rotate(Math.toRadians(rotation), 0, 1, 0).translate(-.5, 0, -.5);
		transform = Optional.of(new TRSRTransformation(mat.toMatrix4f()));
		return transform;
	}

	@Override
	public String getCacheKey(BlockState object)
	{
		return Float.toString(rotation);
	}

	@Override
	public float[] getBlockBounds()
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
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}
}