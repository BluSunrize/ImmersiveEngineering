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
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.storage.loot.LootContext.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

public class CapacitorLVTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInternalFluxHandler, IBlockOverlayText,
		IConfigurableSides, IComparatorOverride, ITileDrop
{
	public static TileEntityType<CapacitorLVTileEntity> TYPE;

	public EnumMap<Direction, SideConfig> sideConfig = new EnumMap<>(Direction.class);

	FluxStorage energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), getMaxOutput());

	public int comparatorOutput = 0;

	public CapacitorLVTileEntity(TileEntityType<? extends CapacitorLVTileEntity> type)
	{
		super(type);
		for(Direction f : Direction.VALUES)
		{
			if(f==Direction.UP)
				sideConfig.put(f, SideConfig.INPUT);
			else
				sideConfig.put(f, SideConfig.NONE);
		}
	}

	public CapacitorLVTileEntity()
	{
		this(TYPE);
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
		return IEConfig.MACHINES.capacitorLvStorage.get();
	}

	public int getMaxInput()
	{
		return IEConfig.MACHINES.capacitorLvInput.get();
	}

	public int getMaxOutput()
	{
		return IEConfig.MACHINES.capacitorLvOutput.get();
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		for(Direction f : Direction.VALUES)
			nbt.putInt("sideConfig_"+f.ordinal(), sideConfig.get(f).ordinal());
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
		if(hammer&&IEConfig.GENERAL.colourblindSupport.get()&&mop instanceof BlockRayTraceResult)
		{
			BlockRayTraceResult bmop = (BlockRayTraceResult)mop;
			SideConfig here = sideConfig.get(bmop.getFace());
			SideConfig opposite = sideConfig.get(bmop.getFace().getOpposite());
			return new String[]{
					I18n.format(Lib.DESC_INFO+"blockSide.facing")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectEnergy."+here),
					I18n.format(Lib.DESC_INFO+"blockSide.opposite")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectEnergy."+opposite)
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
	public List<ItemStack> getTileDrops(Builder context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		writeCustomNBT(stack.getOrCreateTag(), false);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack)
	{
		if(stack.hasTag())
			readCustomNBT(stack.getOrCreateTag(), false);
	}
}