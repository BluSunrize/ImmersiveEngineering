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
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EnergyMeterBlockEntity extends ImmersiveConnectableBlockEntity implements IEServerTickableBE,
		IStateBasedDirectional, IHasDummyBlocks, IPlayerInteraction, IComparatorOverride, EnergyConnector, IBlockBounds,
		IModelOffsetProvider
{
	public final DoubleList lastPackets = new DoubleArrayList(20);
	private int nextPacketIndex = 0;
	private int compVal = -1;
	private Connection shuntConnection;

	public EnergyMeterBlockEntity(BlockEntityType<EnergyMeterBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public ItemInteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!heldItem.isEmpty()&&heldItem.getItem() instanceof IWireCoil)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		int transfer = getAveragePower();
		int packets = lastPackets.size();
		if(isDummy())
		{
			BlockEntity below = level.getBlockEntity(getBlockPos().below());
			if(below instanceof EnergyMeterBlockEntity)
				packets = ((EnergyMeterBlockEntity)below).lastPackets.size();
		}
		String transferred = "0";
		if(transfer > 0)
			transferred = Utils.formatDouble(transfer, "0.###");
		ChatUtils.sendServerNoSpamMessages(player, Component.translatable(Lib.CHAT_INFO+"energyTransfered", packets, transferred));
		return ItemInteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
	}

	@Override
	public void tickServer()
	{
		if(((level.getGameTime()&31)==(worldPosition.asLong()&31)||compVal < 0))
			updateComparatorValues();
		EnergyTransferHandler handler = globalNet.getLocalNet(worldPosition)
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
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(isDummy())
		{
			BlockEntity below = level.getBlockEntity(getBlockPos().below());
			if(below instanceof EnergyMeterBlockEntity)
				return ((EnergyMeterBlockEntity)below).canConnectCable(cableType, target, offset);
		}
		if(!(cableType instanceof IEnergyWire))
			return false;
		for(ConnectionPoint cp : getConnectionPoints())
			for(Connection c : globalNet.getLocalNet(worldPosition).getConnections(cp))
				if(!c.isInternal()&&(target.equals(cp)||!c.type.getCategory().equals(cableType.getCategory())))
					return false;
		return true;
	}

	@Override
	public Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		if(isDummy())
			return ImmutableSet.of(
					worldPosition,
					worldPosition.below()
			);
		else
			return ImmutableSet.of(
					worldPosition,
					worldPosition.above()
			);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		ConnectionPoint targetByHit;
		if(getFacing().getAxis()==Axis.X)
			targetByHit = info.hitZ > 0.5?new ConnectionPoint(worldPosition, 0): new ConnectionPoint(worldPosition, 1);
		else
			targetByHit = info.hitX > 0.5?new ConnectionPoint(worldPosition, 0): new ConnectionPoint(worldPosition, 1);
		if(!globalNet.getLocalNet(targetByHit).getConnections(targetByHit).stream().allMatch(Connection::isInternal))
			return new ConnectionPoint(worldPosition, 1-targetByHit.index());
		else
			return targetByHit;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		if(isDummy())
		{
			BlockEntity below = level.getBlockEntity(getBlockPos().below());
			if(below instanceof EnergyMeterBlockEntity)
				((EnergyMeterBlockEntity)below).connectCable(cableType, target, other, otherTarget);
		}
		else
			super.connectCable(cableType, target, other, otherTarget);
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		if(isDummy())
			return worldPosition.below();
		else
			return worldPosition;
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		if(getFacing().getAxis()==Axis.X)
			return new Vec3(.5, 1.4375, here.index()==0?.8125: .1875);
		else
			return new Vec3(here.index()==0?.8125: .1875, 1.4375, .5);
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getBlockPos().below();
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		BlockPos dummyPos = worldPosition.above();
		level.setBlockAndUpdate(dummyPos, IEBaseBlock.applyLocationalWaterlogging(
				state.setValue(IEProperties.MULTIBLOCKSLAVE, true), level, dummyPos
		));
		((EnergyMeterBlockEntity)level.getBlockEntity(dummyPos)).setFacing(this.getFacing());
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		if(isDummy())
			level.removeBlock(pos.below(), false);
		else
			level.removeBlock(pos.above(), false);
	}

	public int getAveragePower()
	{
		EnergyMeterBlockEntity te = this;
		if(te.isDummy())
		{
			BlockEntity below = level.getBlockEntity(getBlockPos().below());
			if(!(below instanceof EnergyMeterBlockEntity))
				return -1;
			te = (EnergyMeterBlockEntity)below;
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
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(isDummy());
	}

	private static final CachedVoxelShapes<Boolean> SHAPES = new CachedVoxelShapes<>(EnergyMeterBlockEntity::getShape);

	private static List<AABB> getShape(Boolean isDummy)
	{
		List<AABB> list = Lists.newArrayList(
				new AABB(.1875f, -.625f, .1875f, .8125f, .8125f, .8125f),
				new AABB(0, -1, 0, 1, .375f-1, 1)
		);
		if(!isDummy)
			for(int i = 0; i < list.size(); ++i)
				list.set(i, list.get(i).move(0, 1, 0));
		return list;
	}

	@Override
	public Property<Direction> getFacingProperty()
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
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
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
			Collection<Connection> conns = globalNet.getLocalNet(cp).getConnections(worldPosition);
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
			BlockEntity te = level.getBlockEntity(worldPosition.below());
			if(te instanceof EnergyMeterBlockEntity)
				((EnergyMeterBlockEntity)te).compVal = compVal;
		}
		if(oldVal!=compVal)
		{
			level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
			level.updateNeighbourForOutputSignal(worldPosition.below(), getBlockState().getBlock());
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
		shuntConnection = new Connection(worldPosition, 0, 1);
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
		return ImmutableList.of(new ConnectionPoint(worldPosition, 0), new ConnectionPoint(worldPosition, 1));
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		if(isDummy())
			return BlockPos.ZERO;
		else
			return new BlockPos(0, -1, 0);
	}
}
