/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.LightningRodLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.LightningRodShapes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LightningRodLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>
{
	public static final BlockPos MASTER_OFFSET = new BlockPos(1, 1, 1);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		if(state.energy.getEnergyStored() > 0)
			for(final CapabilityReference<IEnergyStorage> outputRef : state.energyOutputs)
			{
				final IEnergyStorage output = outputRef.getNullable();
				if(output!=null)
				{
					final int accepted = output.receiveEnergy(state.energy.getEnergyStored(), false);
					state.energy.modifyEnergyStored(-accepted);
				}
			}

		if(level.shouldTickModulo(256))
			state.fenceNet = null;
		if(state.fenceNet==null)
			state.fenceNet = getFenceNet(level.getRawLevel(), level.toAbsolute(MASTER_OFFSET));
		if(state.fenceNet.isValid()
				&&level.shouldTickModulo(128)
				&&(level.isThundering()||(level.isRaining()&&ApiUtils.RANDOM.nextInt(10)==0)))
		{
			final BlockPos strikePosition = state.fenceNet.getAbsoluteStrikePosition(level);
			if(strikePosition!=null)
			{
				state.energy.setStoredEnergy(IEServerConfig.MACHINES.lightning_output.get());
				LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(level.getRawLevel());
				lightningboltentity.moveTo(Vec3.atBottomCenterOf(strikePosition));
				lightningboltentity.setVisualOnly(true);
				level.getRawLevel().addFreshEntity(lightningboltentity);
			}
		}
	}

	@Nonnull
	private static FenceNet getFenceNet(Level level, BlockPos absoluteMasterPos)
	{
		int height = 0;
		boolean broken = false;
		BlockPos lastFence = null;
		for(int i = absoluteMasterPos.getY()+2; i < level.getMaxBuildHeight()-1; i++)
		{
			BlockPos pos = new BlockPos(absoluteMasterPos.getX(), i, absoluteMasterPos.getZ());
			if(!broken&&isFence(level, pos))
			{
				height++;
				lastFence = pos;
			}
			else if(!level.isEmptyBlock(pos))
				return FenceNet.INVALID;
			else
			{
				if(!broken)
					broken = true;
			}
		}
		if(lastFence==null)
			return FenceNet.INVALID;

		ArrayList<BlockPos> openList = new ArrayList<>();
		ArrayList<BlockPos> closedList = new ArrayList<>();
		openList.add(lastFence);
		while(!openList.isEmpty()&&closedList.size() < 256)
		{
			BlockPos next = openList.get(0);
			if(!closedList.contains(next)&&isFence(level, next))
			{
				closedList.add(next);
				openList.add(next.relative(Direction.WEST));
				openList.add(next.relative(Direction.EAST));
				openList.add(next.relative(Direction.NORTH));
				openList.add(next.relative(Direction.SOUTH));
				openList.add(next.relative(Direction.UP));
			}
			openList.remove(0);
		}
		return new FenceNet(height, closedList);
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public <T> LazyOptional<T> getCapability(
			IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap
	)
	{
		final State state = ctx.getState();
		final BlockPos posInMultiblock = position.posInMultiblock();
		if(cap==ForgeCapabilities.ENERGY&&(position.side()==null||(posInMultiblock.getY()==1&&(posInMultiblock.getX()+posInMultiblock.getZ())%2==1)))
			return ForgeCapabilities.ENERGY.orEmpty(cap, state.energyCap.get(ctx));
		else
			return LazyOptional.empty();
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return LightningRodShapes.SHAPE_GETTER;
	}

	private static boolean isFence(Level level, BlockPos pos)
	{
		return Utils.isBlockAt(level, pos, MetalDecoration.STEEL_FENCE.get());
	}

	public static class State implements IMultiblockState
	{
		private final MutableEnergyStorage energy = new MutableEnergyStorage(
				IEServerConfig.MACHINES.lightning_output.get()
		);
		private final ImmutableList<CapabilityReference<IEnergyStorage>> energyOutputs;
		@Nullable
		private FenceNet fenceNet = null;
		private final StoredCapability<IEnergyStorage> energyCap = new StoredCapability<>(energy);

		public State(IInitialMultiblockContext<State> capabilitySource)
		{
			ImmutableList.Builder<CapabilityReference<IEnergyStorage>> builder = ImmutableList.builder();
			for(RelativeBlockFace face : RelativeBlockFace.HORIZONTAL)
				builder.add(capabilitySource.getCapabilityAt(
						ForgeCapabilities.ENERGY,
						face.offsetRelative(MASTER_OFFSET, 2),
						face.getOpposite()
				));
			this.energyOutputs = builder.build();
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			EnergyHelper.serializeTo(energy, nbt);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			EnergyHelper.deserializeFrom(energy, nbt);
		}
	}

	private record FenceNet(int height, List<BlockPos> absoluteFencePositions)
	{
		public static final FenceNet INVALID = new FenceNet(0, List.of());

		public boolean isValid()
		{
			return !absoluteFencePositions.isEmpty();
		}

		public BlockPos getAbsoluteStrikePosition(IMultiblockLevel level)
		{
			int i = height+absoluteFencePositions.size();
			final int masterY = level.getAbsoluteOrigin().getY();
			if(ApiUtils.RANDOM.nextInt(4096*level.getMaxBuildHeight()) < i*(masterY+i))
				return absoluteFencePositions.get(ApiUtils.RANDOM.nextInt(absoluteFencePositions.size()));
			else
				return null;
		}
	}
}
