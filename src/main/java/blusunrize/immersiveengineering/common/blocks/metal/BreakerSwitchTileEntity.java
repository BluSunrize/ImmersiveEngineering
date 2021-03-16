/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectableBlock;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static blusunrize.immersiveengineering.api.wires.WireType.HV_CATEGORY;

public class BreakerSwitchTileEntity extends ImmersiveConnectableTileEntity implements IBlockBounds, IAdvancedDirectionalTile,
		IActiveState, IHammerInteraction, IScrewdriverInteraction, IPlayerInteraction, IRedstoneOutput, IOBJModelCallback<BlockState>, IStateBasedDirectional
{
	public static final int LEFT_INDEX = 0;
	public static final int RIGHT_INDEX = 1;
	public int rotation = 0;
	public int wires = 0;
	public boolean inverted = false;

	public BreakerSwitchTileEntity()
	{
		super(IETileTypes.BREAKER_SWITCH.get());
	}

	public BreakerSwitchTileEntity(TileEntityType<? extends BreakerSwitchTileEntity> type)
	{
		super(type);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset)
	{
		Matrix4 mat = new Matrix4()
				.setIdentity()
				.translate(.5, .5, 0)
				.rotate(-Math.PI/2*rotation, 0, 0, 1)
				.translate(-.5, -.5, 0)
				.multiply(Matrix4.inverseFacing(getFacing()));
		Vector3d transformedHit = mat.apply(new Vector3d(info.hitX, info.hitY, info.hitZ));
		return new ConnectionPoint(pos, transformedHit.x > 0.5?RIGHT_INDEX: LEFT_INDEX);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		if(HV_CATEGORY.equals(cableType.getCategory())&&!canTakeHV())
			return false;
		for(ConnectionPoint cp : getConnectionPoints())
			for(Connection c : globalNet.getLocalNet(cp).getConnections(cp))
				if(!c.isInternal()&&(cp.equals(target)||!cableType.getCategory().equals(c.type.getCategory())))
					return false;
		return true;
	}

	protected boolean canTakeHV()
	{
		return false;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		wires++;
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		WireType type = connection!=null?connection.type: null;
		if(type==null)
			wires = 0;
		else
			wires--;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("rotation", rotation);
		nbt.putInt("wires", wires);
		nbt.putBoolean("inverted", inverted);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		rotation = nbt.getInt("rotation");
		wires = nbt.getInt("wires");
		inverted = nbt.getBoolean("inverted");
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Matrix4 mat = new Matrix4(getFacing());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		boolean isLeft = here.getIndex()==LEFT_INDEX;
		return mat.apply(new Vector3d(isLeft?.25: .75, .5, .125));
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, Vector3d hitVec)
	{
		rotation = (rotation+3)%4;
		for(ConnectionPoint cp : getConnectionPoints())
			for(Connection c : getLocalNet(cp.getIndex()).getConnections(cp))
				if(!c.isInternal())
					globalNet.updateCatenaryData(c, world);
		markDirty();
		markContainingBlockForUpdate(getBlockState());
		return true;
	}

	@Override
	public ActionResultType screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, Vector3d hitVec)
	{
		final boolean oldPassing = allowEnergyToPass();
		inverted = !inverted;
		if(!world.isRemote)
		{
			ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"rsSignal."+(inverted?"invertedOn": "invertedOff")));
			notifyNeighbours();
			if(oldPassing!=allowEnergyToPass())
				updateConductivity();
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!Utils.isHammer(heldItem))
		{
			boolean active = !getIsActive();
			setActive(active);
			world.playSound(null, getPos(), IESounds.direSwitch, SoundCategory.BLOCKS, 2.5F, 1);
			world.addBlockEvent(getPos(), getBlockState().getBlock(), active?1: 0, 0);
			notifyNeighbours();
			updateConductivity();
			return true;
		}
		else
			return false;
	}

	protected void updateConductivity()
	{
		if(allowEnergyToPass())
			globalNet.addConnection(new Connection(pos, LEFT_INDEX, RIGHT_INDEX));
		else
			globalNet.removeConnection(new Connection(pos, LEFT_INDEX, RIGHT_INDEX));
	}

	public void notifyNeighbours()
	{
		markDirty();
		world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
		for(Direction f : DirectionUtils.VALUES)
			world.notifyNeighborsOfStateChange(getPos().offset(f), getBlockState().getBlock());
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(super.receiveClientEvent(id, arg))
			return true;
		this.markContainingBlockForUpdate(null);
		return true;
	}


	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return MiscConnectableBlock.DEFAULT_FACING_PROP;
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

	private static final CachedVoxelShapes<Pair<Direction, Integer>> SHAPES = new CachedVoxelShapes<>(pair -> {
		Vector3d start = new Vector3d(.25, .1875, 0);
		Vector3d end = new Vector3d(.75, .8125, .5);
		Matrix4 mat = new Matrix4(pair.getLeft());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*pair.getRight(), 0, 0, 1).translate(-.5, -.5, 0);
		start = mat.apply(start);
		end = mat.apply(end);
		return ImmutableList.of(new AxisAlignedBB(start, end));
	});

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return SHAPES.get(Pair.of(getFacing(), rotation));
	}

	@Override
	public int getWeakRSOutput(@Nonnull Direction side)
	{
		return (getIsActive()^inverted)?15: 0;
	}

	@Override
	public int getStrongRSOutput(@Nonnull Direction side)
	{
		return side.getOpposite()==getFacing()&&(getIsActive()^inverted)?15: 0;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull Direction side)
	{
		return true;
	}

	@Override
	public TransformationMatrix applyTransformations(BlockState object, String group, TransformationMatrix transform)
	{
		return transform.compose(new TransformationMatrix(
				null,
				new Quaternion(0, 90*rotation, 0, true),
				null, null
		));
	}

	@Override
	public String getCacheKey(BlockState object)
	{
		return rotation+","+getFacing().getIndex();
	}

	@Override
	public void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer)
	{
		Direction f = Direction.SOUTH;
		int rotationSign = -1;
		if(side.getAxis()==Axis.Y)
		{
			float xFromMid = hitX-.5f;
			float zFromMid = hitZ-.5f;
			float max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
			if(max==Math.abs(xFromMid))
				f = xFromMid < 0?Direction.WEST: Direction.EAST;
			else
				f = zFromMid < 0?Direction.NORTH: Direction.SOUTH;
			if(side==Direction.DOWN)
			{
				f = f.getOpposite();
				rotationSign = 1;
			}
		}
		rotation = Direction.NORTH.getHorizontalIndex()+rotationSign*f.getHorizontalIndex();
		rotation = (rotation+4)%4;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, LEFT_INDEX), new ConnectionPoint(pos, RIGHT_INDEX));
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		if(allowEnergyToPass())
			return ImmutableList.of(new Connection(pos, LEFT_INDEX, RIGHT_INDEX));
		else
			return ImmutableList.of();
	}

	protected boolean allowEnergyToPass()
	{
		return getIsActive();
	}
}