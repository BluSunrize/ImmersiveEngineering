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
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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

public class FluidPlacerTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IConfigurableSides, IBlockOverlayText, IScrewdriverInteraction
{
	private final Map<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);

	{
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.OUTPUT);
		sideConfig.put(Direction.UP, IOSideConfig.INPUT);
	}

	public FluidTank tank = new FluidTank(4*FluidAttributes.BUCKET_VOLUME);
	public boolean redstoneControlInverted = false;

	private int tickCount = 0;

	private final HashSet<BlockPos> checkedPositions = new HashSet<>();
	private final TreeMap<Integer, Queue<BlockPos>> layeredPlacementQueue = new TreeMap<>();
	private final Queue<BlockPos> tempFluids = new LinkedList<>();

	public FluidPlacerTileEntity()
	{
		super(IETileTypes.FLUID_PLACER.get());
	}

	@Override
	public void tick()
	{
		if(world.isRemote||(isRSPowered()^redstoneControlInverted))
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
					BlockPos targetPos = lowestLayer.poll();
					if(canFill(targetPos)&&tank.getFluid().getFluid().getAttributes().canBePlacedInWorld(world, targetPos, tank.getFluid()))
						if(place(targetPos, tank, world))
						{
							addConnectedSpaces(targetPos);
							handleTempFluids();
						}
				}
			}
		}
		tickCount++;
	}

	private static boolean place(BlockPos pos, FluidTank tank, World world)
	{
		if(tank.getFluidAmount() < FluidAttributes.BUCKET_VOLUME)
			return false;
		FluidStack stack = tank.getFluid();
		BucketItem bucketitem;
		{
			Item bucket = stack.getFluid().getFilledBucket();
			if(!(bucket instanceof BucketItem))
				return false;
			bucketitem = (BucketItem)bucket;
		}
		if(bucketitem==Items.AIR)
			return false;
		ItemStack bucketStack = new ItemStack(bucketitem);
		if(bucketitem.tryPlaceContainedLiquid(null, world, pos, null))
		{
			tank.drain(FluidAttributes.BUCKET_VOLUME, FluidAction.EXECUTE);
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
		return layeredPlacementQueue.computeIfAbsent(yLevel, k -> new LinkedList<>());
	}

	private void addConnectedSpaces(BlockPos pos)
	{
		for(Direction facing : Direction.values())
			if(facing!=Direction.UP&&(pos!=getPos()||sideConfig.get(facing)==IOSideConfig.OUTPUT))
				addToQueue(pos.offset(facing));
	}

	private void addToQueue(BlockPos pos)
	{
		if(!World.isOutsideBuildHeight(pos))//Within world borders
			if(checkedPositions.add(pos))//Don't add checked positions
				if(pos.distanceSq(getPos()) < 64*64)//Within max range
				{
					if(fluidMatches(pos))
						tempFluids.add(pos);
					if(canFill(pos))
						getQueueForYLevel(pos.getY()).add(pos);
				}
	}

	private boolean fluidMatches(BlockPos targetPos)
	{
		if(!world.isAreaLoaded(targetPos, 1))
			return false;
		BlockState state = world.getBlockState(targetPos);
		return state.getFluidState().getFluid()==tank.getFluid().getFluid();
	}

	private boolean canFill(BlockPos targetPos)
	{
		if(!world.isAreaLoaded(targetPos, 1))
			return false;
		BlockState state = world.getBlockState(targetPos);
		// Can't fill source blocks
		if(isFullFluidBlock(targetPos, state))
			return false;
		boolean canFill = !state.getMaterial().isSolid();
		if(!canFill&&state.getBlock() instanceof IWaterLoggable)
			canFill = ((IWaterLoggable)state.getBlock()).canContainFluid(world, targetPos, state, tank.getFluid().getFluid());
		return canFill;
	}

	private void handleTempFluids()
	{
		while(!tempFluids.isEmpty())
			addConnectedSpaces(tempFluids.poll());
	}

	private boolean isFullFluidBlock(BlockPos pos, BlockState state)
	{
		return state.getFluidState().isSource();
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		CompoundNBT sideConfigNBT = nbt.getCompound("sideConfig");
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.VALUES[sideConfigNBT.getInt(d.getString())]);
		tank.readFromNBT(nbt.getCompound("tank"));
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		CompoundNBT sideConfigNBT = new CompoundNBT();
		for(Direction d : DirectionUtils.VALUES)
			sideConfigNBT.putInt(d.getString(), sideConfig.get(d).ordinal());
		nbt.put("sideConfig", sideConfigNBT);
		nbt.putBoolean("redstoneInverted", redstoneControlInverted);
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

	@Override
	public ActionResultType screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, Vector3d hitVec)
	{
		if(!world.isRemote)
		{
			redstoneControlInverted = !redstoneControlInverted;
			ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn": "invertedOff")));
			markDirty();
			this.markContainingBlockForUpdate(null);
		}
		return ActionResultType.SUCCESS;
	}

	private final LazyOptional<IFluidHandler> tankCap = registerConstantCap(tank);

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
		if(hammer&&IEClientConfig.showTextOverlay.get()&&rtr instanceof BlockRayTraceResult)
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
