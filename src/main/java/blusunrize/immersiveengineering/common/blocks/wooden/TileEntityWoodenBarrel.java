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
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TileEntityWoodenBarrel extends TileEntityIEBase implements ITickable, IBlockOverlayText, IConfigurableSides, IPlayerInteraction, ITileDrop, IComparatorOverride
{
	public int[] sideConfig = {1, 0};
	public FluidTank tank = new FluidTank(12000);
	public static final int IGNITION_TEMPERATURE = 573;

	@Override
	public void update()
	{
		if(world.isRemote)
			return;

		boolean update = false;
		for(int i = 0; i < 2; i++)
			if(tank.getFluidAmount() > 0&&sideConfig[i]==1)
			{
				EnumFacing f = EnumFacing.byIndex(i);
				int out = Math.min(40, tank.getFluidAmount());
				TileEntity te = world.getTileEntity(getPos().offset(f));
				if(te!=null&&te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite()))
				{
					IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite());
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
			int i = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.ordinal())];
			int j = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.getOpposite().ordinal())];
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
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null||sideConfig.length < 2)
			sideConfig = new int[]{-1, 0};
		this.readTank(nbt);
	}

	public void readTank(NBTTagCompound nbt)
	{
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		this.writeTank(nbt, false);
	}

	public void writeTank(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount() > 0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if(!toItem||write)
			nbt.setTag("tank", tankTag);
	}

	SidedFluidHandler[] sidedFluidHandler = {new SidedFluidHandler(this, EnumFacing.DOWN), new SidedFluidHandler(this, EnumFacing.UP)};
	SidedFluidHandler nullsideFluidHandler = new SidedFluidHandler(this, null);

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&(facing==null||facing.getAxis()==Axis.Y))
			return true;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&(facing==null||facing.getAxis()==Axis.Y))
			return (T)(facing==null?nullsideFluidHandler: sidedFluidHandler[facing.ordinal()]);
		return super.getCapability(capability, facing);
	}

	static class SidedFluidHandler implements IFluidHandler
	{
		TileEntityWoodenBarrel barrel;
		EnumFacing facing;

		SidedFluidHandler(TileEntityWoodenBarrel barrel, EnumFacing facing)
		{
			this.barrel = barrel;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			if(resource==null||(facing!=null&&barrel.sideConfig[facing.ordinal()]!=0)||!barrel.isFluidValid(resource))
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
			if(facing!=null&&barrel.sideConfig[facing.ordinal()]!=1)
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
		return fluid!=null&&fluid.getFluid()!=null&&fluid.getFluid().getTemperature(fluid) < IGNITION_TEMPERATURE&&!fluid.getFluid().isGaseous(fluid);
	}

	@Override
	public SideConfig getSideConfig(int side)
	{
		if(side > 1)
			return SideConfig.NONE;
		return SideConfig.values()[this.sideConfig[side]+1];
	}

	@Override
	public boolean toggleSide(int side, EntityPlayer p)
	{
		if(side!=0&&side!=1)
			return false;
		sideConfig[side]++;
		if(sideConfig[side] > 1)
			sideConfig[side] = -1;
		this.markDirty();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
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
		FluidStack f = FluidUtil.getFluidContained(heldItem);
		boolean metal = this instanceof TileEntityMetalBarrel;
		if(f!=null)
			if(!metal&&f.getFluid().isGaseous(f))
			{
				ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"noGasAllowed"));
				return true;
			}
			else if(!metal&&f.getFluid().getTemperature(f) >= TileEntityWoodenBarrel.IGNITION_TEMPERATURE)
			{
				ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"tooHot"));
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
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		NBTTagCompound tag = new NBTTagCompound();
		writeTank(tag, true);
		if(!tag.isEmpty())
			stack.setTagCompound(tag);
		return stack;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if(stack.hasTagCompound())
			readTank(stack.getTagCompound());
	}

	@Override
	public int getComparatorInputOverride()
	{
		return (int)(15*(tank.getFluidAmount()/(float)tank.getCapacity()));
	}
}