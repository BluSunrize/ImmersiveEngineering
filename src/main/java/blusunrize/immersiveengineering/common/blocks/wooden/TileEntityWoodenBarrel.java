/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMetalBarrel;
import blusunrize.immersiveengineering.common.util.CapabilityHolder;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static blusunrize.immersiveengineering.api.IEEnums.SideConfig.NONE;
import static blusunrize.immersiveengineering.api.IEEnums.SideConfig.OUTPUT;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

public class TileEntityWoodenBarrel extends TileEntityIEBase implements ITickable, IBlockOverlayText, IConfigurableSides, IPlayerInteraction, ITileDrop, IComparatorOverride
{
	public static final int IGNITION_TEMPERATURE = 573;
	public static TileEntityType<TileEntityWoodenBarrel> TYPE;

	public EnumMap<EnumFacing, SideConfig> sideConfig = new EnumMap<>(ImmutableMap.of(
			EnumFacing.DOWN, OUTPUT,
			EnumFacing.UP, SideConfig.INPUT
	));
	public FluidTank tank = new FluidTank(12000);

	public TileEntityWoodenBarrel(TileEntityType<? extends TileEntityWoodenBarrel> type)
	{
		super(type);
	}

	public TileEntityWoodenBarrel()
	{
		this(TYPE);
	}

	private Map<EnumFacing, CapabilityReference<IFluidHandler>> neighbors = ImmutableMap.of(
			EnumFacing.DOWN, CapabilityReference.forNeighbor(this, FLUID_HANDLER_CAPABILITY, EnumFacing.DOWN),
			EnumFacing.UP, CapabilityReference.forNeighbor(this, FLUID_HANDLER_CAPABILITY, EnumFacing.UP)
	);

	@Override
	public void tick()
	{
		if(world.isRemote)
			return;

		boolean update = false;
		for(EnumFacing side : neighbors.keySet())
			if(tank.getFluidAmount() > 0&&sideConfig.get(side)==OUTPUT)
			{
				int out = Math.min(40, tank.getFluidAmount());
				CapabilityReference<IFluidHandler> capRef = neighbors.get(side);
				IFluidHandler handler = capRef.get();
				if(handler!=null)
				{
					int accepted = handler.fill(Utils.copyFluidStackWithAmount(tank.getFluid(), out, false), false);
					FluidStack drained = this.tank.drain(accepted, true);
					if(drained!=null)
					{
						handler.fill(drained, true);
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
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND)))
		{
			String s = null;
			if(tank.getFluid()!=null)
				s = tank.getFluid().getLocalizedName()+": "+tank.getFluidAmount()+"mB";
			else
				s = I18n.format(Lib.GUI+"empty");
			return new String[]{s};
		}
		if(hammer&&IEConfig.colourblindSupport&&mop.sideHit.getAxis()==Axis.Y)
		{
			SideConfig side = sideConfig.getOrDefault(mop.sideHit, NONE);
			SideConfig opposite = sideConfig.getOrDefault(mop.sideHit.getOpposite(), NONE);
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
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		int[] sideCfgArray = nbt.getIntArray("sideConfig");
		if(sideCfgArray.length < 2)
			sideCfgArray = new int[]{-1, 0};
		sideConfig.clear();
		for(int i = 0; i < sideCfgArray.length; ++i)
			sideConfig.put(EnumFacing.byIndex(i), SideConfig.VALUES[sideCfgArray[i]]);
		this.readTank(nbt);
	}

	public void readTank(NBTTagCompound nbt)
	{
		tank.readFromNBT(nbt.getCompound("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		int[] sideCfgArray = new int[2];
		sideCfgArray[0] = sideConfig.get(EnumFacing.DOWN).ordinal();
		sideCfgArray[1] = sideConfig.get(EnumFacing.UP).ordinal();
		nbt.setIntArray("sideConfig", sideCfgArray);
		this.writeTank(nbt, false);
	}

	public void writeTank(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount() > 0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if(!toItem||write)
			nbt.setTag("tank", tankTag);
	}

	private Map<EnumFacing, CapabilityHolder<IFluidHandler>> sidedFluidHandler = new HashMap<>();

	{
		sidedFluidHandler.put(EnumFacing.DOWN, CapabilityHolder.of(() -> new SidedFluidHandler(this, EnumFacing.DOWN)));
		sidedFluidHandler.put(EnumFacing.UP, CapabilityHolder.of(() -> new SidedFluidHandler(this, EnumFacing.UP)));
		sidedFluidHandler.put(null, CapabilityHolder.of(() -> new SidedFluidHandler(this, null)));
		caps.addAll(sidedFluidHandler.values());
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability==FLUID_HANDLER_CAPABILITY&&(facing==null||facing.getAxis()==Axis.Y))
			return sidedFluidHandler.getOrDefault(facing, CapabilityHolder.empty()).getAndCast();
		return super.getCapability(capability, facing);
	}

	static class SidedFluidHandler implements IFluidHandler
	{
		TileEntityWoodenBarrel barrel;
		@Nullable
		EnumFacing facing;

		SidedFluidHandler(TileEntityWoodenBarrel barrel, @Nullable EnumFacing facing)
		{
			this.barrel = barrel;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			if(resource==null||(facing!=null&&barrel.sideConfig.get(facing)!=SideConfig.INPUT)||!barrel.isFluidValid(resource))
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
		public FluidStack drain(FluidStack resource, boolean doDrain)
		{
			if(resource==null)
				return null;
			return this.drain(resource.amount, doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain)
		{
			if(facing!=null&&barrel.sideConfig.get(facing)!=OUTPUT)
				return null;
			FluidStack f = barrel.tank.drain(maxDrain, doDrain);
			if(f!=null&&f.amount > 0)
			{
				barrel.markDirty();
				barrel.markContainingBlockForUpdate(null);
			}
			return f;
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			return barrel.tank.getTankProperties();
		}
	}

	public boolean isFluidValid(FluidStack fluid)
	{
		return fluid!=null&&fluid.getFluid()!=null
				&&fluid.getFluid().getTemperature(fluid) < IGNITION_TEMPERATURE
				&&!fluid.getFluid().isGaseous(fluid);
	}

	@Override
	public SideConfig getSideConfig(EnumFacing side)
	{
		return sideConfig.getOrDefault(side, NONE);
	}

	@Override
	public boolean toggleSide(EnumFacing side, EntityPlayer p)
	{
		if(side.getAxis()!=Axis.Y)
			return false;
		sideConfig.compute(side, (s, config) -> SideConfig.next(config));
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
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		LazyOptional<FluidStack> fOptional = FluidUtil.getFluidContained(heldItem);
		boolean metal = this instanceof TileEntityMetalBarrel;
		if(!metal)
		{
			LazyOptional<Boolean> ret = fOptional.map((f) -> {
				if(f.getFluid().isGaseous(f))
				{
					ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"noGasAllowed"));
					return true;
				}
				else if(f.getFluid().getTemperature(f) >= TileEntityWoodenBarrel.IGNITION_TEMPERATURE)
				{
					ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"tooHot"));
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
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1);
		NBTTagCompound tag = new NBTTagCompound();
		writeTank(tag, true);
		if(!tag.isEmpty())
			stack.setTag(tag);
		return stack;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
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