/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.IEEnums.IOSideConfig.NONE;
import static blusunrize.immersiveengineering.api.IEEnums.IOSideConfig.OUTPUT;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER;

public class WoodenBarrelBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IBlockOverlayText,
		IConfigurableSides, IPlayerInteraction, IBlockEntityDrop, IComparatorOverride
{
	public static final int IGNITION_TEMPERATURE = 573;
	public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(ImmutableMap.of(
			Direction.DOWN, OUTPUT,
			Direction.UP, IOSideConfig.INPUT
	));
	public FluidTank tank = new FluidTank(12*FluidType.BUCKET_VOLUME, this::isFluidValid);

	public WoodenBarrelBlockEntity(BlockEntityType<? extends WoodenBarrelBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public WoodenBarrelBlockEntity(BlockPos pos, BlockState state)
	{
		this(IEBlockEntities.WOODEN_BARREL.get(), pos, state);
	}

	private final Map<Direction, CapabilityReference<IFluidHandler>> neighbors = ImmutableMap.of(
			Direction.DOWN, CapabilityReference.forNeighbor(this, FLUID_HANDLER, Direction.DOWN),
			Direction.UP, CapabilityReference.forNeighbor(this, FLUID_HANDLER, Direction.UP)
	);

	@Override
	public void tickServer()
	{
		boolean update = false;
		for(Direction side : neighbors.keySet())
			if(tank.getFluidAmount() > 0&&sideConfig.get(side)==OUTPUT)
			{
				int out = Math.min(FluidType.BUCKET_VOLUME, tank.getFluidAmount());
				CapabilityReference<IFluidHandler> capRef = neighbors.get(side);
				IFluidHandler handler = capRef.getNullable();
				if(handler!=null)
				{
					int accepted = handler.fill(Utils.copyFluidStackWithAmount(tank.getFluid(), out, false), FluidAction.SIMULATE);
					FluidStack drained = this.tank.drain(accepted, FluidAction.EXECUTE);
					if(!drained.isEmpty())
					{
						handler.fill(drained, FluidAction.EXECUTE);
						update = true;
					}
				}
			}
		if(update)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public Component[] getOverlayText(Player player, HitResult rtr, boolean hammer)
	{
		if(rtr.getType()==Type.MISS)
			return null;
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)))
			return new Component[]{
					TextUtils.formatFluidStack(tank.getFluid())
			};
		if(!(rtr instanceof BlockHitResult))
			return null;
		BlockHitResult brtr = (BlockHitResult)rtr;
		if(hammer&&IEClientConfig.showTextOverlay.get()&&brtr.getDirection().getAxis()==Axis.Y)
		{
			IOSideConfig side = sideConfig.getOrDefault(brtr.getDirection(), NONE);
			IOSideConfig opposite = sideConfig.getOrDefault(brtr.getDirection().getOpposite(), NONE);
			return TextUtils.sideConfigWithOpposite(Lib.DESC_INFO+"blockSide.connectFluid.", side, opposite);
		}
		return null;
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		int[] sideCfgArray = nbt.getIntArray("sideConfig");
		if(sideCfgArray.length < 2)
			sideCfgArray = new int[]{-1, 0};
		sideConfig.clear();
		for(int i = 0; i < sideCfgArray.length; ++i)
			sideConfig.put(Direction.from3DDataValue(i), IOSideConfig.VALUES[sideCfgArray[i]]);
		this.readTank(nbt);
	}

	public void readTank(CompoundTag nbt)
	{
		tank.readFromNBT(nbt.getCompound("tank"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		int[] sideCfgArray = new int[2];
		sideCfgArray[0] = sideConfig.get(Direction.DOWN).ordinal();
		sideCfgArray[1] = sideConfig.get(Direction.UP).ordinal();
		nbt.putIntArray("sideConfig", sideCfgArray);
		this.writeTank(nbt, false);
	}

	public void writeTank(CompoundTag nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount() > 0;
		CompoundTag tankTag = tank.writeToNBT(new CompoundTag());
		if(!toItem||write)
			nbt.put("tank", tankTag);
	}

	private final Map<Direction, ResettableCapability<IFluidHandler>> sidedFluidHandler = new HashMap<>();

	{
		sidedFluidHandler.put(Direction.DOWN, registerCapability(new SidedFluidHandler(this, Direction.DOWN)));
		sidedFluidHandler.put(Direction.UP, registerCapability(new SidedFluidHandler(this, Direction.UP)));
		sidedFluidHandler.put(null, registerCapability(new SidedFluidHandler(this, null)));
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==FLUID_HANDLER&&sidedFluidHandler.containsKey(facing))
			return sidedFluidHandler.get(facing).cast();
		return super.getCapability(capability, facing);
	}

	static class SidedFluidHandler implements IFluidHandler
	{
		WoodenBarrelBlockEntity barrel;
		@Nullable
		Direction facing;

		SidedFluidHandler(WoodenBarrelBlockEntity barrel, @Nullable Direction facing)
		{
			this.barrel = barrel;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, FluidAction doFill)
		{
			if(resource.isEmpty()||(facing!=null&&barrel.sideConfig.get(facing)!=IOSideConfig.INPUT)||!barrel.isFluidValid(resource))
				return 0;

			int i = barrel.tank.fill(resource, doFill);
			if(i > 0&&doFill.execute())
			{
				barrel.setChanged();
				barrel.markContainingBlockForUpdate(null);
			}
			return i;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			if(resource.isEmpty())
				return FluidStack.EMPTY;
			return this.drain(resource.getAmount(), doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			if(facing!=null&&barrel.sideConfig.get(facing)!=OUTPUT)
				return FluidStack.EMPTY;
			FluidStack f = barrel.tank.drain(maxDrain, doDrain);
			if(!f.isEmpty())
			{
				barrel.setChanged();
				barrel.markContainingBlockForUpdate(null);
			}
			return f;
		}

		@Override
		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank)
		{
			return barrel.tank.getFluidInTank(tank);
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return barrel.tank.getTankCapacity(tank);
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return barrel.tank.isFluidValid(tank, stack);
		}
	}

	public boolean isFluidValid(FluidStack fluid)
	{
		return !fluid.isEmpty()&&fluid.getFluid().getFluidType().getTemperature(fluid) < IGNITION_TEMPERATURE
				&&!fluid.getFluid().is(Tags.Fluids.GASEOUS);
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return sideConfig.getOrDefault(side, NONE);
	}

	@Override
	public boolean toggleSide(Direction side, Player p)
	{
		if(side.getAxis()!=Axis.Y)
			return false;
		sideConfig.compute(side, (s, config) -> IOSideConfig.next(config));
		this.setChanged();
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
		return true;
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		Optional<FluidStack> fOptional = FluidUtil.getFluidContained(heldItem);
		boolean metal = this instanceof MetalBarrelBlockEntity;
		if(!metal)
		{
			Optional<Boolean> ret = fOptional.map((f) -> {
				if(f.getFluid().is(Tags.Fluids.GASEOUS))
				{
					player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"noGasAllowed"), true);
					return true;
				}
				else if(f.getFluid().getFluidType().getTemperature(f) >= WoodenBarrelBlockEntity.IGNITION_TEMPERATURE)
				{
					player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"tooHot"), true);
					return true;
				}
				else
					return false;
			});
			if(ret.orElse(false))
				return true;
		}

		if(FluidUtils.interactWithFluidHandler(player, hand, tank))
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundTag tag = new CompoundTag();
		writeTank(tag, true);
		if(!tag.isEmpty())
			stack.setTag(tag);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		onBEPlaced(ctx.getItemInHand());
	}

	public void onBEPlaced(ItemStack stack)
	{
		if(stack.hasTag())
			readTank(stack.getOrCreateTag());
	}

	@Override
	public int getComparatorInputOverride()
	{
		return (int)(15*(tank.getFluidAmount()/(float)tank.getCapacity()));
	}
}