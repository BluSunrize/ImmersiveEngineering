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
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FluidPlacerBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IConfigurableSides,
		IBlockOverlayText, IScrewdriverInteraction
{
	private final Map<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);

	{
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.OUTPUT);
		sideConfig.put(Direction.UP, IOSideConfig.INPUT);
	}

	public FluidTank tank = new FluidTank(4*FluidType.BUCKET_VOLUME);
	public boolean redstoneControlInverted = false;

	private int tickCount = 0;

	private final HashSet<BlockPos> checkedPositions = new HashSet<>();
	private final TreeMap<Integer, Queue<BlockPos>> layeredPlacementQueue = new TreeMap<>();
	private final Queue<BlockPos> tempFluids = new LinkedList<>();

	public FluidPlacerBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.FLUID_PLACER.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		if((isRSPowered()^redstoneControlInverted))
			return;

		if(tickCount%16==0)
		{
			if(tickCount%512==0)//Initial placement
				prepareAreaCheck();
			if(tank.getFluidAmount() >= FluidType.BUCKET_VOLUME&&
					!layeredPlacementQueue.isEmpty())
			{
				Queue<BlockPos> lowestLayer = layeredPlacementQueue.firstEntry().getValue();
				if(lowestLayer==null||lowestLayer.isEmpty())
					layeredPlacementQueue.pollFirstEntry();
				else
				{
					BlockPos targetPos = lowestLayer.poll();
					if(canFill(targetPos)&&tank.getFluid().getFluid().getFluidType().canBePlacedInLevel(level, targetPos, tank.getFluid()))
						if(place(targetPos, tank, level))
						{
							addConnectedSpaces(targetPos);
							handleTempFluids();
						}
				}
			}
		}
		tickCount++;
	}

	private static boolean place(BlockPos pos, FluidTank tank, Level world)
	{
		if(tank.getFluidAmount() < FluidType.BUCKET_VOLUME)
			return false;
		FluidStack stack = tank.getFluid();
		BucketItem bucketitem;
		{
			Item bucket = stack.getFluid().getBucket();
			if(!(bucket instanceof BucketItem))
				return false;
			bucketitem = (BucketItem)bucket;
		}
		if(bucketitem==Items.AIR)
			return false;
		ItemStack bucketStack = new ItemStack(bucketitem);
		if(bucketitem.emptyContents(null, world, pos, null))
		{
			tank.drain(FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
			bucketitem.checkExtraContent(null, world, bucketStack, pos);
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

		addConnectedSpaces(getBlockPos());
		handleTempFluids();
	}

	private Queue<BlockPos> getQueueForYLevel(int yLevel)
	{
		return layeredPlacementQueue.computeIfAbsent(yLevel, k -> new LinkedList<>());
	}

	private void addConnectedSpaces(BlockPos pos)
	{
		for(Direction facing : Direction.values())
			if(facing!=Direction.UP&&(pos!=getBlockPos()||sideConfig.get(facing)==IOSideConfig.OUTPUT))
				addToQueue(pos.relative(facing));
	}

	private void addToQueue(BlockPos pos)
	{
		if(!getLevelNonnull().isOutsideBuildHeight(pos))//Within world borders
			if(checkedPositions.add(pos))//Don't add checked positions
				if(pos.distSqr(getBlockPos()) < 64*64)//Within max range
				{
					if(fluidMatches(pos))
						tempFluids.add(pos);
					if(canFill(pos))
						getQueueForYLevel(pos.getY()).add(pos);
				}
	}

	private boolean fluidMatches(BlockPos targetPos)
	{
		if(!level.isAreaLoaded(targetPos, 1)||tank.getFluid().isEmpty())
			return false;
		BlockState state = level.getBlockState(targetPos);
		return state.getFluidState().getType()==tank.getFluid().getFluid();
	}

	private boolean canFill(BlockPos targetPos)
	{
		if(!level.isAreaLoaded(targetPos, 1))
			return false;
		BlockState state = level.getBlockState(targetPos);
		// Can't fill source blocks
		if(isFullFluidBlock(targetPos, state))
			return false;
		boolean canFill = !state.isSolid();
		if(!canFill&&state.getBlock() instanceof LiquidBlockContainer container)
			canFill = container.canPlaceLiquid(level, targetPos, state, tank.getFluid().getFluid());
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
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		CompoundTag sideConfigNBT = nbt.getCompound("sideConfig");
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.VALUES[sideConfigNBT.getInt(d.getSerializedName())]);
		tank.readFromNBT(nbt.getCompound("tank"));
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		CompoundTag sideConfigNBT = new CompoundTag();
		for(Direction d : DirectionUtils.VALUES)
			sideConfigNBT.putInt(d.getSerializedName(), sideConfig.get(d).ordinal());
		nbt.put("sideConfig", sideConfigNBT);
		nbt.putBoolean("redstoneInverted", redstoneControlInverted);
		nbt.put("tank", tank.writeToNBT(new CompoundTag()));
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return sideConfig.get(side);
	}

	@Override
	public boolean toggleSide(Direction side, Player p)
	{
		sideConfig.computeIfPresent(side, (s, conf) -> IOSideConfig.next(conf));
		prepareAreaCheck();
		this.setChanged();
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
		return true;
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(!level.isClientSide)
		{
			redstoneControlInverted = !redstoneControlInverted;
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn": "invertedOff")),
					true
			);
			setChanged();
			this.markContainingBlockForUpdate(null);
		}
		return InteractionResult.SUCCESS;
	}

	private final ResettableCapability<IFluidHandler> tankCap = registerCapability(tank);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==ForgeCapabilities.FLUID_HANDLER&&
				(facing==null||sideConfig.get(facing)==IOSideConfig.INPUT))
			return tankCap.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public Component[] getOverlayText(Player player, HitResult rtr, boolean hammer)
	{
		if(hammer&&IEClientConfig.showTextOverlay.get()&&rtr instanceof BlockHitResult)
		{
			BlockHitResult brtr = (BlockHitResult)rtr;
			IOSideConfig here = sideConfig.get(brtr.getDirection());
			IOSideConfig opposite = sideConfig.get(brtr.getDirection().getOpposite());
			return TextUtils.sideConfigWithOpposite(Lib.DESC_INFO+"blockSide.connectFluid.", here, opposite);
		}
		return null;
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}
}
