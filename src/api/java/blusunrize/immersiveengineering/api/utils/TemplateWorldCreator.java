/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import java.util.List;
import java.util.function.Predicate;

public interface TemplateWorldCreator
{
	SetRestrictedField<TemplateWorldCreator> CREATOR = SetRestrictedField.common();

	World makeWorld(List<BlockInfo> blocks, Predicate<BlockPos> shouldShow);
}
