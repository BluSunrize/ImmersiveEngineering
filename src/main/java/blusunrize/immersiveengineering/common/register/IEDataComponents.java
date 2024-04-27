/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IEDataComponents
{
	public static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(
			Registries.DATA_COMPONENT_TYPE, Lib.MODID
	);

	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BALLOON_OFFSET = REGISTER.register(
			"balloon_offset", () -> DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.build()
	);
}
