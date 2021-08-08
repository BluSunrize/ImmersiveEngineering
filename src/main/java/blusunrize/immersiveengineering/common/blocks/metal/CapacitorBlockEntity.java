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
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

public class CapacitorBlockEntity extends IEBaseBlockEntity implements IETickableBlockEntity, IIEInternalFluxHandler, IBlockOverlayText,
		IConfigurableSides, IComparatorOverride, IBlockEntityDrop
{
	public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);
	private final CapacitorConfig configValues;

	FluxStorage energyStorage;

	public int comparatorOutput = 0;

	public CapacitorBlockEntity(CapacitorConfig configValues, BlockPos pos, BlockState state)
	{
		super(configValues.tileType.get(), pos, state);
		this.configValues = configValues;
		for(Direction f : DirectionUtils.VALUES)
		{
			if(f==Direction.UP)
				sideConfig.put(f, IOSideConfig.INPUT);
			else
				sideConfig.put(f, IOSideConfig.NONE);
		}
		energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), getMaxOutput());
	}

	@Override
	public void tickServer()
	{
		for(Direction f : DirectionUtils.VALUES)
			this.transferEnergy(f);

		if(level.getGameTime()%32==((getBlockPos().getX()^getBlockPos().getZ())&31))
		{
			int i = scaleStoredEnergyTo(15);
			if(i!=this.comparatorOutput)
			{
				this.comparatorOutput = i;
				level.updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
			}
		}
	}

	public int scaleStoredEnergyTo(int scale)
	{
		return (int)(scale*(energyStorage.getEnergyStored()/(float)energyStorage.getMaxEnergyStored()));
	}

	protected void transferEnergy(Direction side)
	{
		if(this.sideConfig.get(side)!=IOSideConfig.OUTPUT)
			return;
		BlockPos outPos = getBlockPos().relative(side);
		BlockEntity tileEntity = Utils.getExistingTileEntity(level, outPos);
		int out = Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
		this.energyStorage.modifyEnergyStored(-EnergyHelper.insertFlux(tileEntity, side.getOpposite(), out, false));
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return this.sideConfig.get(side);
	}

	@Override
	public boolean toggleSide(Direction side, Player player)
	{
		sideConfig.put(side, IOSideConfig.next(sideConfig.get(side)));
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

	public final int getMaxStorage()
	{
		return configValues.storage.getAsInt();
	}

	public final int getMaxInput()
	{
		return configValues.input.getAsInt();
	}

	public final int getMaxOutput()
	{
		return configValues.output.getAsInt();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		for(Direction f : DirectionUtils.VALUES)
			nbt.putInt("sideConfig_"+f.ordinal(), sideConfig.get(f).ordinal());
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		for(Direction f : DirectionUtils.VALUES)
			sideConfig.put(f, IOSideConfig.values()[nbt.getInt("sideConfig_"+f.ordinal())]);
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
	public IOSideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		if(facing==null)
			return IOSideConfig.NONE;
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
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(hammer&&IEClientConfig.showTextOverlay.get()&&mop instanceof BlockHitResult)
		{
			BlockHitResult bmop = (BlockHitResult)mop;
			IOSideConfig here = sideConfig.get(bmop.getDirection());
			IOSideConfig opposite = sideConfig.get(bmop.getDirection().getOpposite());
			return TextUtils.sideConfigWithOpposite(Lib.DESC_INFO+"blockSide.connectEnergy.", here, opposite);
		}
		return null;
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}

	@Override
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public List<ItemStack> getBlockEntityDrop(LootContext context)
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