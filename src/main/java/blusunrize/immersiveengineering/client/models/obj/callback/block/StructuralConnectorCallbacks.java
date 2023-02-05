/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorStructuralBlockEntity;
import org.joml.Quaternionf;
import com.mojang.math.Transformation;
import org.joml.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class StructuralConnectorCallbacks implements BlockCallback<Float>
{
	public static final StructuralConnectorCallbacks INSTANCE = new StructuralConnectorCallbacks();

	@Override
	public Float extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return blockEntity instanceof ConnectorStructuralBlockEntity connector?connector.rotation: getDefaultKey();
	}

	@Override
	public Float getDefaultKey()
	{
		return 0F;
	}

	@Override
	public Transformation applyTransformations(Float rotation, String group, Transformation transform)
	{
		return transform.compose(new Transformation(
				new Vector3f(0, 0, 0),
				new Quaternionf().rotateXYZ(0, (float)Math.toRadians(rotation), 0),
				null, null
		));
	}
}
