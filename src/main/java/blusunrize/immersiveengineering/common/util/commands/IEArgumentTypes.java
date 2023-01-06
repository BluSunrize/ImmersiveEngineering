/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class IEArgumentTypes
{
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTER = DeferredRegister.create(
			Registries.COMMAND_ARGUMENT_TYPE, Lib.MODID
	);
	public static final RegistryObject<SingletonArgumentInfo<MineralArgument>> MINERAL = REGISTER.register(
			"mineral", () -> ArgumentTypeInfos.registerByClass(
					MineralArgument.class, SingletonArgumentInfo.contextFree(MineralArgument::new)
			)
	);

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
