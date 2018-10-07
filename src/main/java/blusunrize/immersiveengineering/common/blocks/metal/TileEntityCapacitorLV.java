/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nonnull;

public class TileEntityCapacitorLV extends TileEntityIEBase implements ITickable, IIEInternalFluxHandler, IBlockOverlayText, IConfigurableSides, IComparatorOverride, ITileDrop
{
	public SideConfig[] sideConfig = {SideConfig.NONE, SideConfig.INPUT, SideConfig.NONE, SideConfig.NONE, SideConfig.NONE, SideConfig.NONE};
	FluxStorage energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), getMaxOutput());

	public int comparatorOutput = 0;

	@Override
	public void update()
	{
		if(!world.isRemote)
		{
			for(int i = 0; i < 6; i++)
				this.transferEnergy(i);

			if(world.getTotalWorldTime()%32==((getPos().getX()^getPos().getZ())&31))
			{
				int i = scaleStoredEnergyTo(15);
				if(i!=this.comparatorOutput)
				{
					this.comparatorOutput = i;
					world.updateComparatorOutputLevel(getPos(), getBlockType());
				}
			}
		}
	}

	public int scaleStoredEnergyTo(int scale)
	{
		return (int)(scale*(energyStorage.getEnergyStored()/(float)energyStorage.getMaxEnergyStored()));
	}

	protected void transferEnergy(int side)
	{
		if(this.sideConfig[side]!=SideConfig.OUTPUT)
			return;
		EnumFacing fd = EnumFacing.byIndex(side);
		BlockPos outPos = getPos().offset(fd);
		TileEntity tileEntity = Utils.getExistingTileEntity(world, outPos);
		int out = Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
		this.energyStorage.modifyEnergyStored(-EnergyHelper.insertFlux(tileEntity, fd.getOpposite(), out, false));
	}

	@Override
	public IEEnums.SideConfig getSideConfig(int side)
	{
		return this.sideConfig[side];
	}

	@Override
	public boolean toggleSide(int side, EntityPlayer player)
	{
		sideConfig[side] = SideConfig.next(sideConfig[side]);
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

	public int getMaxStorage()
	{
		return IEConfig.Machines.capacitorLV_storage;
	}

	public int getMaxInput()
	{
		return IEConfig.Machines.capacitorLV_input;
	}

	public int getMaxOutput()
	{
		return IEConfig.Machines.capacitorLV_output;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		for(int i = 0; i < 6; i++)
			nbt.setInteger("sideConfig_"+i, sideConfig[i].ordinal());
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		if(nbt.hasKey("sideConfig"))//old NBT style
		{
			int[] old = nbt.getIntArray("sideConfig");
			for(int i = 0; i < old.length; i++)
				sideConfig[i] = SideConfig.values()[old[i]+1];
		}
		else
			for(int i = 0; i < 6; i++)
				sideConfig[i] = SideConfig.values()[nbt.getInteger("sideConfig_"+i)];
		energyStorage.readFromNBT(nbt);
	}


	IEForgeEnergyWrapper[] wrappers = IEForgeEnergyWrapper.getDefaultWrapperArray(this);

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		return this.energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		if(facing==null)
			return SideConfig.NONE;
		return this.sideConfig[facing.ordinal()];
	}

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(facing==null)
			return null;
		return wrappers[facing.ordinal()];
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if(hammer&&IEConfig.colourblindSupport)
		{
			SideConfig i = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.ordinal())];
			SideConfig j = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.getOpposite().ordinal())];
			return new String[]{
					I18n.format(Lib.DESC_INFO+"blockSide.facing")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectEnergy."+i),
					I18n.format(Lib.DESC_INFO+"blockSide.opposite")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectEnergy."+j)
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
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		if(energyStorage.getEnergyStored() > 0)
			ItemNBTHelper.setInt(stack, "energyStorage", energyStorage.getEnergyStored());
		for(int i = 0; i < 6; i++)
			ItemNBTHelper.setInt(stack, "sideConfig_"+i, sideConfig[i].ordinal());
		return stack;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			energyStorage.setEnergy(ItemNBTHelper.getInt(stack, "energyStorage"));
		for(int i = 0; i < 6; i++)
			if(ItemNBTHelper.hasKey(stack, "sideConfig_"+i))
				sideConfig[i] = SideConfig.values()[ItemNBTHelper.getInt(stack, "sideConfig_"+i)];
	}
}