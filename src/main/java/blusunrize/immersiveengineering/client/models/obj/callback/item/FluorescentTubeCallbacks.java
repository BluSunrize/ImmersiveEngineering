/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.obj.callback.item.FluorescentTubeCallbacks.Key;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector4f;

import java.util.List;

import static blusunrize.immersiveengineering.common.items.FluorescentTubeItem.*;

public class FluorescentTubeCallbacks implements ItemCallback<Key>
{
	public static final FluorescentTubeCallbacks INSTANCE = new FluorescentTubeCallbacks();

	@Override
	public Key extractKey(ItemStack object, LivingEntity owner)
	{
		boolean lit = isLit(object);
		float min = .3F+(lit?ItemNBTHelper.getFloat(object, LIT_STRENGTH)*.68F: 0);
		float mult = min+(lit?ApiUtils.RANDOM.nextFloat()*Mth.clamp(1-min, 0, .1F): 0);
		float[] colors = getRGBFloat(object, mult);
		return new Key(new Vector4f(colors[0], colors[1], colors[2], colors[3]));
	}

	private static final List<List<String>> special = List.of(List.of("tube"));

	@Override
	public List<List<String>> getSpecialGroups(ItemStack stack, ItemDisplayContext transform, LivingEntity entity)
	{
		return special;
	}

	@Override
	public boolean areGroupsFullbright(ItemStack stack, List<String> groups)
	{
		return groups.size()==1&&"tube".equals(groups.get(0))&&isLit(stack);
	}

	@Override
	public Vector4f getRenderColor(Key object, String group, String material, ShaderCase shaderCase, Vector4f original)
	{
		if("tube".equals(group))
			return object.color();
		else
			return new Vector4f(.067f, .067f, .067f, 1);
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key(new Vector4f(1, 1, 1, 1));
	}

	public record Key(Vector4f color)
	{
	}
}
