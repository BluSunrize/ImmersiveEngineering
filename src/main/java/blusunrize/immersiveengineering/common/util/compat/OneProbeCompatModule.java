/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.util.compat.top.*;
import com.google.common.base.Function;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.fml.InterModComms;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author Robustprogram - 26.1.2021
 */
public class OneProbeCompatModule extends IECompatModule implements Function<ITheOneProbe, Void>
{
	@Override
	public void preInit()
	{
		Supplier<Function<ITheOneProbe, Void>> supplier = () -> this;
		InterModComms.sendTo("theoneprobe", "getTheOneProbe", supplier);
	}

	@Override
	public void registerRecipes() { }

	@Override
	public void init() { }

	@Override
	public void postInit() { }

	@Nullable
	@Override
	public Void apply(@Nullable ITheOneProbe input)
	{
		EnergyInfoProvider energyInfo = new EnergyInfoProvider();
		input.registerProvider(energyInfo);
		input.registerProbeConfigProvider(energyInfo);
		input.registerProvider(new ProcessProvider());
		input.registerProvider(new TeslaCoilProvider());
		input.registerProvider(new SideConfigProvider());
		input.registerProvider(new FluidInfoProvider());
		input.registerBlockDisplayOverride(new MultiblockDisplayOverride());
		return null;
	}
}