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
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.IELogger;
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
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

import static blusunrize.immersiveengineering.api.wires.WireType.HV_CATEGORY;

//TODO ConnectionPoints for opening/closing
public class BreakerSwitchTileEntity extends ImmersiveConnectableTileEntity implements IBlockBounds, IAdvancedDirectionalTile,
		IActiveState, IHammerInteraction, IPlayerInteraction, IRedstoneOutput, IOBJModelCallback<BlockState>, IStateBasedDirectional
{
	public static TileEntityType<BreakerSwitchTileEntity> TYPE;

	public static final int LEFT_INDEX = 0;
	public static final int RIGHT_INDEX = 1;
	public int rotation = 0;
	public int wires = 0;
	public boolean inverted = false;

	public BreakerSwitchTileEntity()
	{
		super(TYPE);
	}

	public BreakerSwitchTileEntity(TileEntityType<? extends BreakerSwitchTileEntity> type)
	{
		super(type);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		Matrix4 mat = new Matrix4(getFacing());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		//TODO what is Matrix(facing)^-1?
		mat.invert();
		Vec3d transformedHit = mat.apply(new Vec3d(info.hitX, info.hitY, info.hitZ));
		IELogger.logger.info("Transformed hit: {}, original: {}", transformedHit,
				new Vec3d(info.hitX, info.hitY, info.hitZ));
		return new ConnectionPoint(pos, transformedHit.x > 0.5?RIGHT_INDEX: LEFT_INDEX);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(HV_CATEGORY.equals(cableType.getCategory())&&!canTakeHV())
			return false;
		//TODO
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
	public void removeCable(Connection connection)
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
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		rotation = nbt.getInt("rotation");
		wires = nbt.getInt("wires");
		inverted = nbt.getBoolean("inverted");
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Matrix4 mat = new Matrix4(getFacing());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		boolean isLeft = here.getIndex()==LEFT_INDEX;
		return mat.apply(new Vec3d(isLeft?.25: .75, .5, .125));
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
	{
		if(player.isSneaking())
		{
			inverted = !inverted;
			ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"rsSignal."+(inverted?"invertedOn": "invertedOff")));
			notifyNeighbours();
			updateConductivity();
		}
		else
			rotation = (rotation+3)%4;
		return true;
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
		}
		return true;
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
		for(Direction f : Direction.VALUES)
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
	public float[] getBlockBounds()
	{
		Vec3d start = new Vec3d(.25, .1875, 0);
		Vec3d end = new Vec3d(.75, .8125, .5);
		Matrix4 mat = new Matrix4(getFacing());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		start = mat.apply(start);
		end = mat.apply(end);
		return new float[]{(float)start.x, (float)start.y, (float)start.z,
				(float)end.x, (float)end.y, (float)end.z};
	}

	@Override
	public int getWeakRSOutput(BlockState state, Direction side)
	{
		return (getIsActive()^inverted)?15: 0;
	}

	@Override
	public int getStrongRSOutput(BlockState state, Direction side)
	{
		return side.getOpposite()==getFacing()&&(getIsActive()^inverted)?15: 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, Direction side)
	{
		return true;
	}

	@Override
	public Optional<TRSRTransformation> applyTransformations(BlockState object, String group, Optional<TRSRTransformation> transform)
	{
		Matrix4 mat = transform.map(trsrTransformation -> new Matrix4(trsrTransformation.getMatrixVec())).orElseGet(Matrix4::new);
		mat = mat.translate(.5, 0, .5).rotate(Math.PI/2*rotation, 0, 1, 0).translate(-.5, 0, -.5);
		transform = Optional.of(new TRSRTransformation(mat.toMatrix4f()));
		return transform;
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
		if(side.getAxis()==Axis.Y)
		{
			float xFromMid = hitX-.5f;
			float zFromMid = hitZ-.5f;
			float max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
			if(max==Math.abs(xFromMid))
				f = xFromMid < 0?Direction.WEST: Direction.EAST;
			else
				f = zFromMid < 0?Direction.NORTH: Direction.SOUTH;
			if((side==Direction.UP&&f.getAxis()==Axis.Z)||side==Direction.DOWN)
				f = f.getOpposite();
		}
		rotation = f.getHorizontalIndex();
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