/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods;

import blusunrize.immersiveengineering.common.datafix.RenameMultiblockBEsFix;
import blusunrize.immersiveengineering.common.mixin.CaptureOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BlockEntity.class)
public class BlockEntityMixin
{
	@CaptureOwner(
			method = {"loadStatic", "m_155241_"},
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;",
					ordinal = 0,
					remap = false
			)
	)
	private static Optional<BlockEntityType<?>> fix(
			Optional<BlockEntityType<?>> baseType, BlockPos pos, BlockState state, CompoundTag tag
	)
	{
		return RenameMultiblockBEsFix.replaceIfMissing(baseType, tag);
	}
}
