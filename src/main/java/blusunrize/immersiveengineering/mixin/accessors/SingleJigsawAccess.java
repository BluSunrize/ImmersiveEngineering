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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.LegacySingleJigsawPiece;
import net.minecraft.world.gen.feature.template.StructureProcessorList;
import net.minecraft.world.gen.feature.template.Template;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(LegacySingleJigsawPiece.class)
public interface SingleJigsawAccess
{
	@Invoker("<init>")
	static LegacySingleJigsawPiece construct(
			Either<ResourceLocation, Template> nameOrData, Supplier<StructureProcessorList> processors,
			JigsawPattern.PlacementBehaviour placementBehaviour
	)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
