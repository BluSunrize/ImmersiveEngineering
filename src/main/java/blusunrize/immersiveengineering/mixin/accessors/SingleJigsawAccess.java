/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LegacySinglePoolElement.class)
public interface SingleJigsawAccess
{
	@Invoker("<init>")
	static LegacySinglePoolElement construct(
			Either<ResourceLocation, StructureTemplate> p_210348_, Holder<StructureProcessorList> p_210349_, StructureTemplatePool.Projection p_210350_
	)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
