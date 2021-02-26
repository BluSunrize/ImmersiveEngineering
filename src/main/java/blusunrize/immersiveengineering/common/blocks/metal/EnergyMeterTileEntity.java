/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EnergyMeterTileEntity extends ImmersiveConnectableTileEntity implements ITickableTileEntity, IStateBasedDirectional,
		IHasDummyBlocks, IPlayerInteraction, IComparatorOverride, EnergyConnector, IBlockBounds, IModelOffsetProvider
{
	public final DoubleList lastPackets = new DoubleArrayList(20);
	private int nextPacketIndex = 0;
	private int compVal = -1;
	private Connection shuntConnection;

	public EnergyMeterTileEntity()
	{
		super(IETileTypes.ENERGY_METER.get());
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
		checkForNeedlessTicking();
		if(isDummy()||world.isRemote)
			return;
		if(((world.getGameTime()&31)==(pos.toLong()&31)||compVal < 0))
			updateComparatorValues();
		EnergyTransferHandler handler = globalNet.getLocalNet(pos)
				.getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
		double transferred = 0;
		if(handler!=null)
		{
			Object2DoubleMap<Connection> map = handler.getTransferredLastTick();
			transferred = map.getDouble(shuntConnection);
		}
		if(nextPacketIndex >= lastPackets.size())
			lastPackets.add(transferred);
		else
			lastPackets.set(nextPacketIndex, transferred);
		nextPacketIndex = (nextPacketIndex+1)%20;
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		if(isDummy())
		{
			TileEntity below = world.getTileEntity(getPos().down());
			if(below instanceof EnergyMeterTileEntity)
				return ((EnergyMeterTileEntity)below).canConnectCable(cableType, target, offset);
		}
		if(!(cableType instanceof IEnergyWire))
			return false;
		for(ConnectionPoint cp : getConnectionPoints())
			for(Connection c : globalNet.getLocalNet(pos).getConnections(cp))
				if(!c.isInternal()&&(target.equals(cp)||!c.type.getCategory().equals(cableType.getCategory())))
					return false;
		return true;
	}

	@Override
	public Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		if(isDummy())
			return ImmutableSet.of(
					pos,
					pos.down()
			);
		else
			return ImmutableSet.of(
					pos,
					pos.up()
			);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset)
	{
		ConnectionPoint targetByHit;
		if(getFacing().getAxis()==Axis.X)
			targetByHit = info.hitZ > 0.5?new ConnectionPoint(pos, 0): new ConnectionPoint(pos, 1);
		else
			targetByHit = info.hitX > 0.5?new ConnectionPoint(pos, 0): new ConnectionPoint(pos, 1);
		if(!globalNet.getLocalNet(targetByHit).getConnections(targetByHit).stream().allMatch(Connection::isInternal))
			return new ConnectionPoint(pos, 1-targetByHit.getIndex());
		else
			return targetByHit;
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
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		if(getFacing().getAxis()==Axis.X)
			return new Vector3d(.5, 1.4375, here.getIndex()==0?.8125: .1875);
		else
			return new Vector3d(here.getIndex()==0?.8125: .1875, 1.4375, .5);
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getPos().down();
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		BlockPos dummyPos = pos.up();
		world.setBlockState(dummyPos, IEBaseBlock.applyLocationalWaterlogging(
				state.with(IEProperties.MULTIBLOCKSLAVE, true), world, dummyPos
		));
		((EnergyMeterTileEntity)world.getTileEntity(dummyPos)).setFacing(this.getFacing());
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
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return SHAPES.get(isDummy());
	}

	private static final CachedVoxelShapes<Boolean> SHAPES = new CachedVoxelShapes<>(EnergyMeterTileEntity::getShape);

	private static List<AxisAlignedBB> getShape(Boolean isDummy)
	{
		List<AxisAlignedBB> list = Lists.newArrayList(
				new AxisAlignedBB(.1875f, -.625f, .1875f, .8125f, .8125f, .8125f),
				new AxisAlignedBB(0, -1, 0, 1, .375f-1, 1)
		);
		if(!isDummy)
			for(int i = 0; i < list.size(); ++i)
				list.set(i, list.get(i).offset(0, 1, 0));
		return list;
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

	private void updateComparatorValues()
	{
		final int oldVal = compVal;
		int maxTrans = Integer.MAX_VALUE;
		for(ConnectionPoint cp : getConnectionPoints())
		{
			int maxTransForPoint = 0;
			Collection<Connection> conns = globalNet.getLocalNet(cp).getConnections(pos);
			for(Connection c : conns)
				if(!c.isInternal()&&c.type instanceof IEnergyWire)
					maxTransForPoint += ((IEnergyWire)c.type).getTransferRate();
			maxTrans = Math.min(maxTrans, maxTransForPoint);
		}
		if(maxTrans==0)
			compVal = 0;
		else
		{
			final double val = getAveragePower()/(double)maxTrans;
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
		shuntConnection = new Connection(pos, 0, 1);
		super.onLoad();
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		if(shuntConnection!=null)
			return ImmutableList.of(shuntConnection);
		else
			return ImmutableList.of();
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, 0), new ConnectionPoint(pos, 1));
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vector3i size)
	{
		if(isDummy())
			return BlockPos.ZERO;
		else
			return new BlockPos(0, -1, 0);
	}
}
