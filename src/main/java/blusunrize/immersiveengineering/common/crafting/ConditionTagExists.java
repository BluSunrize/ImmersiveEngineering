/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IConditionSerializer;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

/**
 * @author BluSunrize
 * @since 09.07.2017
 */
public class ConditionTagExists implements IConditionSerializer
{
	static
	{
		CraftingHelper.register(new ResourceLocation(ImmersiveEngineering.MODID, "tag_exists"),
				new ConditionTagExists());
	}

	@Nonnull
	@Override
	public BooleanSupplier parse(@Nonnull JsonObject json)
	{
		String key = JsonUtils.getString(json, "tag");
		boolean value = JsonUtils.getBoolean(json, "value", true);
		return () -> ApiUtils.isExistingOreName(key)==value;
	}
}
