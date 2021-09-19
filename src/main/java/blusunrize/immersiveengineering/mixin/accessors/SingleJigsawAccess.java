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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(LegacySinglePoolElement.class)
public interface SingleJigsawAccess
{
	@Invoker("<init>")
	static LegacySinglePoolElement construct(
			Either<ResourceLocation, StructureTemplate> nameOrData, Supplier<StructureProcessorList> processors,
			StructureTemplatePool.Projection placementBehaviour
	)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
