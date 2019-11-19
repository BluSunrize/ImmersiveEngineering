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
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnergyMeterTileEntity extends ImmersiveConnectableTileEntity implements ITickableTileEntity, IStateBasedDirectional,
		IHasDummyBlocks, IAdvancedCollisionBounds, IAdvancedSelectionBounds, IPlayerInteraction, IComparatorOverride,
		EnergyConnector
{
	public static TileEntityType<EnergyMeterTileEntity> TYPE;

	public final DoubleList lastPackets = new DoubleArrayList(25);
	private int compVal = -1;
	private Connection shuntConnection;

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
		if(isDummy())
		{
			TileEntity below = world.getTileEntity(getPos().down());
			if(below instanceof EnergyMeterTileEntity)
				packets = ((EnergyMeterTileEntity)below).lastPackets.size();
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
		if(isDummy()||world.isRemote)
			return;
		if(((world.getGameTime()&31)==(pos.toLong()&31)||compVal < 0))
			updateComparatorValues();
		EnergyTransferHandler handler = globalNet.getLocalNet(pos)
				.getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
		double transferred = 0;
		if(handler!=null)
		{
			Object2DoubleMap<Connection> map = handler.getTransferredInTick();
			transferred = map.getDouble(shuntConnection);
		}
		lastPackets.add(transferred);
		if(lastPackets.size() > 20)
			lastPackets.remove(0);
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(isDummy())
		{
			TileEntity below = world.getTileEntity(getPos().down());
			if(below instanceof EnergyMeterTileEntity)
				return ((EnergyMeterTileEntity)below).canConnectCable(cableType, target, offset);
		}
		//TODO correct condition. Energy wire + same voltage as any existing ones?
		return cableType instanceof IEnergyWire;
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		if(getFacing().getAxis()==Axis.X)
			return info.hitZ > 0.5?new ConnectionPoint(pos, 0): new ConnectionPoint(pos, 1);
		else
			return info.hitX > 0.5?new ConnectionPoint(pos, 0): new ConnectionPoint(pos, 1);
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		if(isDummy())
		{
			TileEntity below = world.getTileEntity(getPos().down());
			if(below instanceof EnergyMeterTileEntity)
				((EnergyMeterTileEntity)below).connectCable(cableType, target, other, otherTarget);
		}
		else
			super.connectCable(cableType, target, other, otherTarget);
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		if(isDummy())
			return pos.down();
		else
			return pos;
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		if(getFacing().getAxis()==Axis.X)
			return new Vec3d(.5, 1.4375, here.getIndex()==0?.8125: .1875);
		else
			return new Vec3d(here.getIndex()==0?.8125: .1875, 1.4375, .5);
	}

	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		world.setBlockState(pos.add(0, 1, 0), state.with(IEProperties.MULTIBLOCKSLAVE, true));
		((EnergyMeterTileEntity)world.getTileEntity(pos.add(0, 1, 0))).setFacing(this.getFacing());
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		if(isDummy())
			world.removeBlock(pos.down(), false);
		else
			world.removeBlock(pos.up(), false);
	}

	public int getAveragePower()
	{
		EnergyMeterTileEntity te = this;
		if(te.isDummy())
		{
			TileEntity below = world.getTileEntity(getPos().down());
			if(!(below instanceof EnergyMeterTileEntity))
				return -1;
			te = (EnergyMeterTileEntity)below;
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
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		List<AxisAlignedBB> list = Lists.newArrayList(
				new AxisAlignedBB(.1875f, -.625f, .1875f, .8125f, .8125f, .8125f),
				new AxisAlignedBB(0, -1, 0, 1, .375f-1, 1)
		);
		if(!isDummy())
			for(int i = 0; i < list.size(); ++i)
				list.set(i, list.get(i).offset(0, 1, 0));
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
	public void onLoad()
	{
		super.onLoad();
		shuntConnection = new Connection(pos, 0, 1);
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		return ImmutableList.of(shuntConnection);
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, 0), new ConnectionPoint(pos, 1));
	}
}