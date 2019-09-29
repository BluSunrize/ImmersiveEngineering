/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnergyMeterTileEntity extends ImmersiveConnectableTileEntity implements ITickableTileEntity, IStateBasedDirectional,
		IHasDummyBlocks, IAdvancedCollisionBounds, IAdvancedSelectionBounds, IPlayerInteraction, IComparatorOverride,
		EnergyConnector
{
	public static TileEntityType<EnergyMeterTileEntity> TYPE;

	public double lastEnergyPassed = 0;
	public final ArrayList<Double> lastPackets = new ArrayList<>(25);
	public boolean lower = true;
	private int compVal = -1;

	public EnergyMeterTileEntity()
	{
		super(TYPE);
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!heldItem.isEmpty()&&heldItem.getItem() instanceof IWireCoil)
			return false;
		int transfer = getAveragePower();
		int packets = lastPackets.size();
		if(lower)
		{
			TileEntity above = world.getTileEntity(getPos().add(0, 1, 0));
			if(above instanceof EnergyMeterTileEntity)
				packets = ((EnergyMeterTileEntity)above).lastPackets.size();
		}
		String transferred = "0";
		if(transfer > 0)
			transferred = Utils.formatDouble(transfer, "0.###");
		ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"energyTransfered", packets, transferred));
		return true;
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(lower||world.isRemote)
			return;
		if(((world.getGameTime()&31)==(pos.toLong()&31)||compVal < 0))
			updateComparatorValues();
		//Yes, this might tick in between different connectors sending power, but since this is a block for statistical evaluation over a tick, that is irrelevant.
		lastPackets.add(lastEnergyPassed);
		if(lastPackets.size() > 20)
			lastPackets.remove(0);
		lastEnergyPassed = 0;
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(lower)
		{
			TileEntity above = world.getTileEntity(getPos().add(0, 1, 0));
			if(above instanceof EnergyMeterTileEntity)
				return ((EnergyMeterTileEntity)above).canConnectCable(cableType, target, offset);
		}
		return false;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		if(lower)
		{
			TileEntity above = world.getTileEntity(getPos().add(0, 1, 0));
			if(above instanceof EnergyMeterTileEntity)
				((EnergyMeterTileEntity)above).connectCable(cableType, target, other, otherTarget);
		}
		else
			super.connectCable(cableType, target, other, otherTarget);
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		if(lower)
			return pos.up();
		else
			return pos;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putBoolean("dummy", lower);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		lower = nbt.getBoolean("dummy");
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos other = con.getOtherEnd(here).getPosition();
		int xDif = other.getX()-pos.getX();
		int zDif = other.getZ()-pos.getZ();
		if(getFacing().getAxis()==Axis.X)
			return new Vec3d(.5, .4375, zDif > 0?.8125: .1875);
		else
			return new Vec3d(xDif > 0?.8125: .1875, .4375, .5);
	}

	@Override
	public boolean isDummy()
	{
		return !lower;
	}

	@Override
	public boolean isLogicDummy()
	{
		return lower;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		world.setBlockState(pos.add(0, 1, 0), state);
		((EnergyMeterTileEntity)world.getTileEntity(pos.add(0, 1, 0))).lower = false;
		((EnergyMeterTileEntity)world.getTileEntity(pos.add(0, 1, 0))).setFacing(this.getFacing());
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 1; i++)
			if(world.getTileEntity(getPos().add(0, !lower?-1: 0, 0).add(0, i, 0)) instanceof EnergyMeterTileEntity)
				world.removeBlock(getPos().add(0, !lower?-1: 0, 0).add(0, i, 0), false);
	}

	public int getAveragePower()
	{
		EnergyMeterTileEntity te = this;
		if(te.lower)
		{
			TileEntity tmp = world.getTileEntity(getPos().add(0, 1, 0));
			if(!(tmp instanceof EnergyMeterTileEntity))
				return -1;
			te = (EnergyMeterTileEntity)tmp;
		}
		if(te.lastPackets.size()==0)
			return 0;
		double sum = 0;
		synchronized(te.lastPackets)
		{
			for(double transfer : te.lastPackets)
				sum += transfer;
		}
		return (int)Math.round(sum/te.lastPackets.size());
	}

	@Override
	public float[] getBlockBounds()
	{
//		return new float[]{0,0,0,1,1,1};
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(.1875f, -.625f, .1875f, .8125f, .8125f, .8125f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		if(lower)
		{
			list.set(0, list.get(0).offset(0, 1, 0));
			list.add(new AxisAlignedBB(0, 0, 0, 1, .375f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
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

	private void updateComparatorValues()
	{
		int oldVal = compVal;
		int maxTrans = 0;
		Collection<Connection> conns = globalNet.getLocalNet(pos).getConnections(pos);
		if(conns==null)
			compVal = 0;
		else
		{
			for(Connection c : conns)
				if(c.type instanceof IEnergyWire)
					maxTrans += ((IEnergyWire)c.type).getTransferRate();
			maxTrans /= 2;
			double val = getAveragePower()/(double)maxTrans;
			compVal = (int)Math.ceil(15*val);
			TileEntity te = world.getTileEntity(pos.down());
			if(te instanceof EnergyMeterTileEntity)
				((EnergyMeterTileEntity)te).compVal = compVal;
		}
		if(oldVal!=compVal)
		{
			world.updateComparatorOutputLevel(pos, getBlockState().getBlock());
			world.updateComparatorOutputLevel(pos.down(), getBlockState().getBlock());
		}
	}

	@Override
	public int getComparatorInputOverride()
	{
		return compVal;
	}

	@Override
	public boolean isSource(ConnectionPoint cp)
	{
		return false;
	}

	@Override
	public boolean isSink(ConnectionPoint cp)
	{
		return false;
	}

	@Override
	public void onEnergyPassedThrough(double amount)
	{
		lastEnergyPassed += amount;
	}
}