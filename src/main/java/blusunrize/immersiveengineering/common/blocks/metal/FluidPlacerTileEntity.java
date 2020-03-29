/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FluidPlacerTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IConfigurableSides, IBlockOverlayText
{
	public static TileEntityType<FluidPlacerTileEntity> TYPE;
	public int[] sideConfig = new int[]{1, 0, 1, 1, 1, 1};
	public FluidTank tank = new FluidTank(4000);

	private int tickCount = 0;

	private HashSet<BlockPos> checkedPositions = new HashSet<>();
	private TreeMap<Integer, Queue<BlockPos>> layeredPlacementQueue = new TreeMap<>();
	private HashSet<BlockPos> tempFluids = new HashSet<>();

	public FluidPlacerTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(world.isRemote||world.getRedstonePowerFromNeighbors(getPos())!=0)
			return;

		if(tickCount%16==0)
		{
			if(tickCount%512==0)//Initial placement
				prepareAreaCheck();
			if(tank.getFluidAmount() >= FluidAttributes.BUCKET_VOLUME&&
					!layeredPlacementQueue.isEmpty())
			{
				Queue<BlockPos> lowestLayer = layeredPlacementQueue.firstEntry().getValue();
				if(lowestLayer==null||lowestLayer.isEmpty())
					layeredPlacementQueue.pollFirstEntry();
				else
				{
					BlockPos targetPos = lowestLayer.peek();
					BlockState state = world.getBlockState(targetPos);
					if(((state.getBlock().isAir(state, world, targetPos)||!state.getMaterial().isSolid())&&!isFullFluidBlock(targetPos, state))
							&&tank.getFluid().getFluid().getAttributes().canBePlacedInWorld(world, targetPos, tank.getFluid()))
						if(FluidUtil.tryPlaceFluid(null, world, Hand.MAIN_HAND, targetPos, tank, tank.getFluid()))
						{
							addConnectedSpaces(targetPos);
							handleTempFluids();
							lowestLayer.poll();
						}
				}
			}
		}
		tickCount++;
	}

	private void prepareAreaCheck()
	{
		checkedPositions.clear();
		layeredPlacementQueue.clear();
		tempFluids.clear();

		addConnectedSpaces(getPos());
		handleTempFluids();
	}

	private Queue<BlockPos> getQueueForYLevel(int yLevel)
	{
		Queue<BlockPos> queue = layeredPlacementQueue.get(yLevel);
		if(queue==null)
		{
			queue = new LinkedList<>();
			layeredPlacementQueue.put(yLevel, queue);
		}
		return queue;
	}

	private void addConnectedSpaces(BlockPos pos)
	{
		for(Direction facing : Direction.values())
			if(facing!=Direction.UP&&(pos!=getPos()||sideConfig[facing.ordinal()]==1))
				addToQueue(pos.offset(facing));
	}

	private void addToQueue(BlockPos pos)
	{
		if(pos.getY() >= 0&&pos.getY() <= 255)//Within world borders
			if(checkedPositions.add(pos))//Don't add checked positions
				if(pos.distanceSq(getPos()) < 64*64)//Within max range
					if(world.isBlockLoaded(pos))
					{
						BlockState state = world.getBlockState(pos);
						if(tank.getFluid().getFluid()==state.getFluidState().getFluid())
							tempFluids.add(pos);
						if((state.getBlock().isAir(state, world, pos)||!state.getMaterial().isSolid())&&!isFullFluidBlock(pos, state))
							getQueueForYLevel(pos.getY()).add(pos);
					}
	}

	private void handleTempFluids()
	{
		Set<BlockPos> tempFluidsCopy = tempFluids;//preventing CMEs >_>
		tempFluids = new HashSet<>();
		for(BlockPos pos : tempFluidsCopy)
			addConnectedSpaces(pos);
	}

	private boolean isFullFluidBlock(BlockPos pos, BlockState state)
	{
		return state.getFluidState().isSource();
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig.length!=6)
			sideConfig = new int[]{1, 0, 1, 1, 1, 1};
		tank.readFromNBT(nbt.getCompound("tank"));
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putIntArray("sideConfig", sideConfig);
		nbt.put("tank", tank.writeToNBT(new CompoundNBT()));
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return IOSideConfig.values()[this.sideConfig[side.ordinal()]+1];
	}

	@Override
	public boolean toggleSide(Direction side, PlayerEntity p)
	{
		sideConfig[side.ordinal()]++;
		if(sideConfig[side.ordinal()] > 1)
			sideConfig[side.ordinal()] = -1;
		prepareAreaCheck();
		this.markDirty();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 0, 0);
		return true;
	}

	private LazyOptional<IFluidHandler> tankCap = registerConstantCap(tank);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&(facing==null||sideConfig[facing.ordinal()]==0))
			return tankCap.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult rtr, boolean hammer)
	{
		if(hammer&&IEConfig.GENERAL.showTextOverlay.get()&&rtr instanceof BlockRayTraceResult)
		{
			BlockRayTraceResult brtr = (BlockRayTraceResult)rtr;
			int i = sideConfig[Math.min(sideConfig.length-1, brtr.getFace().ordinal())];
			int j = sideConfig[Math.min(sideConfig.length-1, brtr.getFace().getOpposite().ordinal())];
			return new String[]{
					I18n.format(Lib.DESC_INFO+"blockSide.facing")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectFluid."+i),
					I18n.format(Lib.DESC_INFO+"blockSide.opposite")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectFluid."+j)
			};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}
}