/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.accessors.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.CompiledSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

// TODO is this unused or is IntelliJ acting up?
@Mixin(CompiledSection.class)
public interface CompiledSectionAccess
{
	@Accessor
	Set<RenderType> getHasBlocks();
}
