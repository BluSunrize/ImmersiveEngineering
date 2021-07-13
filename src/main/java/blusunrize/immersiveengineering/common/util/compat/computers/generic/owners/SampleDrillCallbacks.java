/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import blusunrize.immersiveengineering.common.items.CoresampleItem.VeinSampleData;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.EnergyCallbacks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SampleDrillCallbacks extends CallbackOwner<SampleDrillTileEntity>
{
	public SampleDrillCallbacks()
	{
		super(SampleDrillTileEntity.class, "sample_drill");
		addAdditional(EnergyCallbacks.INSTANCE);
	}

	@Override
	public boolean canAttachTo(SampleDrillTileEntity candidate)
	{
		return !candidate.isDummy();
	}


	@ComputerCallable
	public float getSampleProgress(CallbackEnvironment<SampleDrillTileEntity> env)
	{
		return env.getObject().getSampleProgress();
	}

	@ComputerCallable
	public boolean isSamplingFinished(CallbackEnvironment<SampleDrillTileEntity> env)
	{
		return env.getObject().isSamplingFinished();
	}

	@ComputerCallable
	public List<String> getVeinNames(CallbackEnvironment<SampleDrillTileEntity> env)
	{
		return getVeinProperties(env, vsd -> vsd.getType().getId().toString());
	}

	@ComputerCallable
	public List<Integer> getVeinIntegrities(CallbackEnvironment<SampleDrillTileEntity> env)
	{
		return getVeinProperties(env, vsd -> ExcavatorHandler.mineralVeinYield-vsd.getDepletion());
	}

	@ComputerCallable
	public List<Double> getVeinWeights(CallbackEnvironment<SampleDrillTileEntity> env)
	{
		return getVeinProperties(env, VeinSampleData::getPercentageInTotalSample);
	}

	@ComputerCallable
	public List<Double> getVeinSaturations(CallbackEnvironment<SampleDrillTileEntity> env)
	{
		return getVeinProperties(env, VeinSampleData::getSaturation);
	}

	@ComputerCallable
	public void reset(CallbackEnvironment<SampleDrillTileEntity> env)
	{
		SampleDrillTileEntity d = env.getObject();
		d.process = 0;
		d.sample = ItemStack.EMPTY;
	}

	@Nullable
	private <T> List<T> getVeinProperties(CallbackEnvironment<SampleDrillTileEntity> env, Function<VeinSampleData, T> get)
	{
		List<VeinSampleData> veins = env.getObject().getVein();
		if(veins==null)
			return null;
		return veins.stream()
				.map(get)
				.collect(Collectors.toList());
	}
}
