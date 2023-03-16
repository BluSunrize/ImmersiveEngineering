/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods;

import blusunrize.immersiveengineering.common.datafix.RotateMultiblocksFix;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DataFixers.class)
public class DataFixersMixin
{
	@ModifyVariable(
			method = "addFixers",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/datafix/fixes/OptionsAmbientOcclusionFix;<init>(Lcom/mojang/datafixers/schemas/Schema;)V"
			),
			ordinal = 173
	)
	private static Schema addMultiblockFixes(Schema newSchema, DataFixerBuilder builder)
	{
		RotateMultiblocksFix.registerFix(newSchema, builder);
		return newSchema;
	}
}
