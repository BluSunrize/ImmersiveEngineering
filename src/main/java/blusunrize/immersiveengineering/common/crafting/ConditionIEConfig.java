/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEConfig;
import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IConditionSerializer;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

/**
 * @author BluSunrize
 * @since 09.07.2017
 */
public class ConditionIEConfig implements IConditionSerializer
{
	static
	{
		CraftingHelper.register(new ResourceLocation(ImmersiveEngineering.MODID, "config"),
				new ConditionIEConfig());
	}

	@Nonnull
	@Override
	public BooleanSupplier parse(@Nonnull JsonObject json)
	{
		String key = JSONUtils.getString(json, "key");
		boolean value = JSONUtils.getBoolean(json, "value", true);
		return () -> IEConfig.ALL.<Boolean>get(key)==value;
	}
}
