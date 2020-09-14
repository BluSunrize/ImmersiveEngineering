/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

import static blusunrize.immersiveengineering.api.wires.WireType.MV_CATEGORY;

//TODO clean up code duplication with TransformerTE
public class PostTransformerTileEntity extends ImmersiveConnectableTileEntity implements IStateBasedDirectional,
		IBlockBounds
{
	private static final int RIGHT_INDEX = 0;
	private static final int LEFT_INDEX = 1;
	private WireType leftType;
	private WireType rightType;
	protected Set<String> acceptableLowerWires = ImmutableSet.of(WireType.LV_CATEGORY);

	public PostTransformerTileEntity()
	{
		super(IETileTypes.POST_TRANSFORMER.get());
	}

	public PostTransformerTileEntity(TileEntityType<? extends PostTransformerTileEntity> type)
	{
		super(type);
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(leftType!=null)
			nbt.putString("leftType", leftType.getUniqueName());
		if(rightType!=null)
			nbt.putString("rightType", rightType.getUniqueName());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(nbt.contains("leftType"))
			leftType = WireUtils.getWireTypeFromNBT(nbt, "leftType");
		else
			leftType = null;
		if(nbt.contains("rightType"))
			rightType = WireUtils.getWireTypeFromNBT(nbt, "rightType");
		else
			rightType = null;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		switch(target.getIndex())
		{
			case LEFT_INDEX:
				return canAttach(cableType, leftType, rightType);
			case RIGHT_INDEX:
				return canAttach(cableType, rightType, leftType);
		}
		return false;
	}

	private boolean canAttach(WireType toAttach, @Nullable WireType atConn, @Nullable WireType other)
	{
		if(atConn!=null)
			return false;
		String higherCat = getHigherWiretype();
		String attachCat = toAttach.getCategory();
		if(other==null)
			return higherCat.equals(attachCat)||acceptableLowerWires.contains(attachCat);
		boolean isHigher = higherCat.equals(toAttach.getCategory());
		boolean isOtherHigher = higherCat.equals(other.getCategory());
		if(isHigher^isOtherHigher)
		{
			if(isHigher)
				return true;
			else
				return acceptableLowerWires.contains(attachCat);
		}
		else
			return false;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		switch(target.getIndex())
		{
			case LEFT_INDEX:
				this.leftType = cableType;
				break;
			case RIGHT_INDEX:
				this.rightType = cableType;
				break;
		}
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		WireType type = connection!=null?connection.type: null;
		if(type==null)
			leftType = rightType = null;
		else
		{
			switch(connection.getEndFor(pos).getIndex())
			{
				case LEFT_INDEX:
					leftType = null;
					break;
				case RIGHT_INDEX:
					rightType = null;
					break;
			}
		}
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return getConnectionOffset(con, con.getEndFor(pos).getIndex()==RIGHT_INDEX);
	}

	private Vector3d getConnectionOffset(Connection con, boolean right)
	{
		if(right)
			return new Vector3d(.5+(getFacing()==Direction.EAST?.4375: getFacing()==Direction.WEST?-.4375: 0), 1.4375, .5+(getFacing()==Direction.SOUTH?.4375: getFacing()==Direction.NORTH?-.4375: 0));
		else
			return new Vector3d(.5+(getFacing()==Direction.EAST?-.0625: getFacing()==Direction.WEST?.0625: 0), .25, .5+(getFacing()==Direction.SOUTH?-.0625: getFacing()==Direction.NORTH?.0625: 0));
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo target, Vector3i offset)
	{
		if(target.hitY >= .5)
			return new ConnectionPoint(pos, RIGHT_INDEX);
		else
			return new ConnectionPoint(pos, LEFT_INDEX);
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
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
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return VoxelShapes.create(
				getFacing().getAxis()==Axis.Z?.25F: getFacing()==Direction.WEST?-.375F: .6875F,
				0,
				getFacing().getAxis()==Axis.X?.25F: getFacing()==Direction.NORTH?-.375F: .6875F,
				getFacing().getAxis()==Axis.Z?.75F: getFacing()==Direction.EAST?1.375F: .3125F,
				1,
				getFacing().getAxis()==Axis.X?.75F: getFacing()==Direction.SOUTH?1.375F: .3125F
		);
	}


	public String getHigherWiretype()
	{
		return MV_CATEGORY;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, RIGHT_INDEX), new ConnectionPoint(pos, LEFT_INDEX));
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		return ImmutableList.of(new Connection(pos, LEFT_INDEX, RIGHT_INDEX));
	}
}