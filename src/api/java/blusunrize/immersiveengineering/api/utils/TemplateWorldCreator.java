/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.List;
import java.util.function.Predicate;

public interface TemplateWorldCreator
{
	SetRestrictedField<TemplateWorldCreator> CREATOR = SetRestrictedField.common();

	Level makeWorld(List<StructureBlockInfo> blocks, Predicate<BlockPos> shouldShow);
}
