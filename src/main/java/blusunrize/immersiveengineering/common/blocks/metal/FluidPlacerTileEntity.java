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
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FluidPlacerTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IConfigurableSides, IBlockOverlayText
{
	private final Map<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);

	{
		for(Direction d : Direction.VALUES)
			sideConfig.put(d, IOSideConfig.OUTPUT);
		sideConfig.put(Direction.UP, IOSideConfig.INPUT);
	}

	public FluidTank tank = new FluidTank(4000);

	private int tickCount = 0;

	private HashSet<BlockPos> checkedPositions = new HashSet<>();
	private TreeMap<Integer, Queue<BlockPos>> layeredPlacementQueue = new TreeMap<>();
	private HashSet<BlockPos> tempFluids = new HashSet<>();

	public FluidPlacerTileEntity()
	{
		super(IETileTypes.FLUID_PLACER.get());
	}

	@Override
	public void tick()
	{
		if(world.isRemote||isRSPowered())
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
					if(canFill(targetPos, false)&&tank.getFluid().getFluid().getAttributes().canBePlacedInWorld(world, targetPos, tank.getFluid()))
						if(place(targetPos, tank, world))
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

	private static boolean place(BlockPos pos, FluidTank tank, World world)
	{
		if(tank.getFluidAmount() < 1000)
			return false;
		FluidStack stack = tank.getFluid();
		BucketItem bucketitem;
		{
			Item bucket = stack.getFluid().getFilledBucket();
			if(!(bucket instanceof BucketItem))
				return false;
			bucketitem = (BucketItem)bucket;
		}
		if(bucketitem==null||bucketitem==Items.AIR)
			return false;
		ItemStack bucketStack = new ItemStack(bucketitem);
		if(bucketitem.tryPlaceContainedLiquid(null, world, pos, null))
		{
			tank.drain(1000, FluidAction.EXECUTE);
			bucketitem.onLiquidPlaced(world, bucketStack, pos);
			return true;
		}
		else
		{
			return false;
		}
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
			if(facing!=Direction.UP&&(pos!=getPos()||sideConfig.get(facing)==IOSideConfig.OUTPUT))
				addToQueue(pos.offset(facing));
	}

	private void addToQueue(BlockPos pos)
	{
		if(pos.getY() >= 0&&pos.getY() <= 255)//Within world borders
			if(checkedPositions.add(pos))//Don't add checked positions
				if(pos.distanceSq(getPos()) < 64*64)//Within max range
					if(canFill(pos, true))
						getQueueForYLevel(pos.getY()).add(pos);
	}

	private boolean canFill(BlockPos targetPos, boolean allowMatchingFull)
	{
		if(!world.isAreaLoaded(targetPos, 1))
			return false;
		BlockState state = world.getBlockState(targetPos);
		boolean canFill = !state.getMaterial().isSolid();
		boolean fluidMatches = state.getFluidState().getFluid()==tank.getFluid().getFluid();
		if(isFullFluidBlock(targetPos, state)&&(!allowMatchingFull||!fluidMatches))
			canFill = false;
		if(!canFill&&state.getBlock() instanceof IWaterLoggable)
			canFill = ((IWaterLoggable)state.getBlock()).canContainFluid(world, targetPos, state, tank.getFluid().getFluid());
		return canFill;
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
		CompoundNBT sideConfigNBT = nbt.getCompound("sideConfig");
		for(Direction d : Direction.VALUES)
			sideConfig.put(d, IOSideConfig.VALUES[sideConfigNBT.getInt(d.getString())]);
		tank.readFromNBT(nbt.getCompound("tank"));
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		CompoundNBT sideConfigNBT = new CompoundNBT();
		for(Direction d : Direction.VALUES)
			sideConfigNBT.putInt(d.getString(), sideConfig.get(d).ordinal());
		nbt.put("sideConfig", sideConfigNBT);
		nbt.put("tank", tank.writeToNBT(new CompoundNBT()));
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return sideConfig.get(side);
	}

	@Override
	public boolean toggleSide(Direction side, PlayerEntity p)
	{
		sideConfig.computeIfPresent(side, (s, conf) -> IOSideConfig.next(conf));
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
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&
				(facing==null||sideConfig.get(facing)==IOSideConfig.INPUT))
			return tankCap.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public ITextComponent[] getOverlayText(PlayerEntity player, RayTraceResult rtr, boolean hammer)
	{
		if(hammer&&IEConfig.GENERAL.showTextOverlay.get()&&rtr instanceof BlockRayTraceResult)
		{
			BlockRayTraceResult brtr = (BlockRayTraceResult)rtr;
			IOSideConfig here = sideConfig.get(brtr.getFace());
			IOSideConfig opposite = sideConfig.get(brtr.getFace().getOpposite());
			return TextUtils.sideConfigWithOpposite(Lib.DESC_INFO+"blockSide.connectFluid.", here, opposite);
		}
		return null;
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}
}