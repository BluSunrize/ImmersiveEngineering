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
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelTileEntity;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static blusunrize.immersiveengineering.api.IEEnums.IOSideConfig.NONE;
import static blusunrize.immersiveengineering.api.IEEnums.IOSideConfig.OUTPUT;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

public class WoodenBarrelTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IBlockOverlayText, IConfigurableSides, IPlayerInteraction, ITileDrop, IComparatorOverride
{
	public static final int IGNITION_TEMPERATURE = 573;
	public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(ImmutableMap.of(
			Direction.DOWN, OUTPUT,
			Direction.UP, IOSideConfig.INPUT
	));
	public FluidTank tank = new FluidTank(12*FluidAttributes.BUCKET_VOLUME, this::isFluidValid);

	public WoodenBarrelTileEntity(TileEntityType<? extends WoodenBarrelTileEntity> type)
	{
		super(type);
	}

	public WoodenBarrelTileEntity()
	{
		this(IETileTypes.WOODEN_BARREL.get());
	}

	private Map<Direction, CapabilityReference<IFluidHandler>> neighbors = ImmutableMap.of(
			Direction.DOWN, CapabilityReference.forNeighbor(this, FLUID_HANDLER_CAPABILITY, Direction.DOWN),
			Direction.UP, CapabilityReference.forNeighbor(this, FLUID_HANDLER_CAPABILITY, Direction.UP)
	);

	@Override
	public void tick()
	{
		if(world.isRemote)
			return;

		boolean update = false;
		for(Direction side : neighbors.keySet())
			if(tank.getFluidAmount() > 0&&sideConfig.get(side)==OUTPUT)
			{
				int out = Math.min(40, tank.getFluidAmount());
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
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult rtr, boolean hammer)
	{
		if(rtr.getType()==Type.MISS)
			return new String[0];
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(Hand.MAIN_HAND)))
		{
			String s = null;
			if(!tank.getFluid().isEmpty())
				s = tank.getFluid().getDisplayName().getFormattedText()+": "+tank.getFluidAmount()+"mB";
			else
				s = I18n.format(Lib.GUI+"empty");
			return new String[]{s};
		}
		if(!(rtr instanceof BlockRayTraceResult))
			return new String[0];
		BlockRayTraceResult brtr = (BlockRayTraceResult)rtr;
		if(hammer&&IEConfig.GENERAL.showTextOverlay.get()&&brtr.getFace().getAxis()==Axis.Y)
		{
			IOSideConfig side = sideConfig.getOrDefault(brtr.getFace(), NONE);
			IOSideConfig opposite = sideConfig.getOrDefault(brtr.getFace().getOpposite(), NONE);
			return new String[]{
					I18n.format(Lib.DESC_INFO+"blockSide.facing")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectFluid."+side.getName()),
					I18n.format(Lib.DESC_INFO+"blockSide.opposite")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectFluid."+opposite.getName())
			};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		int[] sideCfgArray = nbt.getIntArray("sideConfig");
		if(sideCfgArray.length < 2)
			sideCfgArray = new int[]{-1, 0};
		sideConfig.clear();
		for(int i = 0; i < sideCfgArray.length; ++i)
			sideConfig.put(Direction.byIndex(i), IOSideConfig.VALUES[sideCfgArray[i]]);
		this.readTank(nbt);
	}

	public void readTank(CompoundNBT nbt)
	{
		tank.readFromNBT(nbt.getCompound("tank"));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		int[] sideCfgArray = new int[2];
		sideCfgArray[0] = sideConfig.get(Direction.DOWN).ordinal();
		sideCfgArray[1] = sideConfig.get(Direction.UP).ordinal();
		nbt.putIntArray("sideConfig", sideCfgArray);
		this.writeTank(nbt, false);
	}

	public void writeTank(CompoundNBT nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount() > 0;
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		if(!toItem||write)
			nbt.put("tank", tankTag);
	}

	private Map<Direction, LazyOptional<IFluidHandler>> sidedFluidHandler = new HashMap<>();

	{
		sidedFluidHandler.put(Direction.DOWN, registerCap(() -> new SidedFluidHandler(this, Direction.DOWN)));
		sidedFluidHandler.put(Direction.UP, registerCap(() -> new SidedFluidHandler(this, Direction.UP)));
		sidedFluidHandler.put(null, registerCap(() -> new SidedFluidHandler(this, null)));
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==FLUID_HANDLER_CAPABILITY&&(facing==null||facing.getAxis()==Axis.Y))
			return sidedFluidHandler.getOrDefault(facing, LazyOptional.empty()).cast();
		return super.getCapability(capability, facing);
	}

	static class SidedFluidHandler implements IFluidHandler
	{
		WoodenBarrelTileEntity barrel;
		@Nullable
		Direction facing;

		SidedFluidHandler(WoodenBarrelTileEntity barrel, @Nullable Direction facing)
		{
			this.barrel = barrel;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, FluidAction doFill)
		{
			if(resource==null||(facing!=null&&barrel.sideConfig.get(facing)!=IOSideConfig.INPUT)||!barrel.isFluidValid(resource))
				return 0;

			int i = barrel.tank.fill(resource, doFill);
			if(i > 0)
			{
				barrel.markDirty();
				barrel.markContainingBlockForUpdate(null);
			}
			return i;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			if(resource==null)
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
				barrel.markDirty();
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
		return !fluid.isEmpty()&&fluid.getFluid()!=null
				&&fluid.getFluid().getAttributes().getTemperature(fluid) < IGNITION_TEMPERATURE
				&&!fluid.getFluid().getAttributes().isGaseous(fluid);
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return sideConfig.getOrDefault(side, NONE);
	}

	@Override
	public boolean toggleSide(Direction side, PlayerEntity p)
	{
		if(side.getAxis()!=Axis.Y)
			return false;
		sideConfig.compute(side, (s, config) -> IOSideConfig.next(config));
		this.markDirty();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 0, 0);
		return true;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		LazyOptional<FluidStack> fOptional = FluidUtil.getFluidContained(heldItem);
		boolean metal = this instanceof MetalBarrelTileEntity;
		if(!metal)
		{
			LazyOptional<Boolean> ret = fOptional.map((f) -> {
				if(f.getFluid().getAttributes().isGaseous(f))
				{
					ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"noGasAllowed"));
					return true;
				}
				else if(f.getFluid().getAttributes().getTemperature(f) >= WoodenBarrelTileEntity.IGNITION_TEMPERATURE)
				{
					ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"tooHot"));
					return true;
				}
				else
					return false;
			});
			if(ret.orElse(false))
				return true;
		}

		if(FluidUtil.interactWithFluidHandler(player, hand, tank))
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundNBT tag = new CompoundNBT();
		writeTank(tag, true);
		if(!tag.isEmpty())
			stack.setTag(tag);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
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