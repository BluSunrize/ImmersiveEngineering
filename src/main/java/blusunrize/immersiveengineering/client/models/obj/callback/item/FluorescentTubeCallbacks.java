/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import static blusunrize.immersiveengineering.common.items.FluorescentTubeItem.*;

public class FluorescentTubeCallbacks implements ItemCallback<FluorescentTubeCallbacks.Key>
{
	public static final FluorescentTubeCallbacks INSTANCE = new FluorescentTubeCallbacks();

	@Override
	public Key extractKey(ItemStack object, LivingEntity owner)
	{
		boolean lit = isLit(object);
		float min = .3F+(lit?ItemNBTHelper.getFloat(object, LIT_STRENGTH)*.68F: 0);
		float mult = min+(lit?Utils.RAND.nextFloat()*Mth.clamp(1-min, 0, .1F): 0);
		float[] colors = getRGBFloat(object, mult);
		return new Key(new Vector4f(colors[0], colors[1], colors[2], colors[3]));
	}

	private static final String[][] special = {{"tube"}};

	@Override
	public String[][] getSpecialGroups(ItemStack stack, ItemTransforms.TransformType transform, LivingEntity entity)
	{
		if(isLit(stack))
			return special;
		return EMPTY_STRING_A;
	}

	@Override
	public boolean areGroupsFullbright(ItemStack stack, String[] groups)
	{
		return groups.length==1&&"tube".equals(groups[0])&&isLit(stack);
	}

	@Override
	public Vector4f getRenderColor(Key object, String group, Vector4f original)
	{
		if("tube".equals(group))
			return object.color();
		else
			return new Vector4f(.067f, .067f, .067f, 1);
	}

	public record Key(Vector4f color)
	{
	}
}
