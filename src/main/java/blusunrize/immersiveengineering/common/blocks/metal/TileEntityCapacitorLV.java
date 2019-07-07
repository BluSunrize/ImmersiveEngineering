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
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;

public class TileEntityCapacitorLV extends TileEntityIEBase implements ITickable, IIEInternalFluxHandler, IBlockOverlayText,
		IConfigurableSides, IComparatorOverride, ITileDrop
{
	public static TileEntityType<TileEntityCapacitorLV> TYPE;

	public EnumMap<Direction, SideConfig> sideConfig = new EnumMap<>(Direction.class);

	{
		for(Direction f : Direction.VALUES)
			if(f==Direction.UP)
				sideConfig.put(f, SideConfig.INPUT);
			else
				sideConfig.put(f, SideConfig.NONE);
	}

	FluxStorage energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), getMaxOutput());

	public int comparatorOutput = 0;

	public TileEntityCapacitorLV(TileEntityType<? extends TileEntityCapacitorLV> type)
	{
		super(type);
	}

	public TileEntityCapacitorLV()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(!world.isRemote)
		{
			for(Direction f : Direction.VALUES)
				this.transferEnergy(f);

			if(world.getGameTime()%32==((getPos().getX()^getPos().getZ())&31))
			{
				int i = scaleStoredEnergyTo(15);
				if(i!=this.comparatorOutput)
				{
					this.comparatorOutput = i;
					world.updateComparatorOutputLevel(getPos(), getBlockState().getBlock());
				}
			}
		}
	}

	public int scaleStoredEnergyTo(int scale)
	{
		return (int)(scale*(energyStorage.getEnergyStored()/(float)energyStorage.getMaxEnergyStored()));
	}

	protected void transferEnergy(Direction side)
	{
		if(this.sideConfig.get(side)!=SideConfig.OUTPUT)
			return;
		BlockPos outPos = getPos().offset(side);
		TileEntity tileEntity = Utils.getExistingTileEntity(world, outPos);
		int out = Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
		this.energyStorage.modifyEnergyStored(-EnergyHelper.insertFlux(tileEntity, side.getOpposite(), out, false));
	}

	@Override
	public IEEnums.SideConfig getSideConfig(Direction side)
	{
		return this.sideConfig.get(side);
	}

	@Override
	public boolean toggleSide(Direction side, PlayerEntity player)
	{
		sideConfig.put(side, SideConfig.next(sideConfig.get(side)));
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
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		for(Direction f : Direction.VALUES)
			nbt.setInt("sideConfig_"+f.ordinal(), sideConfig.get(f).ordinal());
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		for(Direction f : Direction.VALUES)
			sideConfig.put(f, SideConfig.values()[nbt.getInt("sideConfig_"+f.ordinal())]);
		energyStorage.readFromNBT(nbt);
	}

	private IEForgeEnergyWrapper[] wrappers = IEForgeEnergyWrapper.getDefaultWrapperArray(this);

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		return this.energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		if(facing==null)
			return SideConfig.NONE;
		return this.sideConfig.get(facing);
	}

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing==null)
			return null;
		return wrappers[facing.ordinal()];
	}

	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(hammer&&IEConfig.colourblindSupport)
		{
			SideConfig i = sideConfig.get(mop.sideHit);
			SideConfig j = sideConfig.get(mop.sideHit.getOpposite());
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
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public ItemStack getTileDrop(@Nullable PlayerEntity player, BlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1);
		writeCustomNBT(stack.getOrCreateTag(), false);
		return stack;
	}

	@Override
	public void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack)
	{
		readCustomNBT(stack.getOrCreateTag(), false);
	}
}