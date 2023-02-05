/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.block.ChuteCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.metal.ChuteBlockEntity;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import org.joml.Matrix4f;
import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChuteCallbacks implements BlockCallback<Key>
{
	public static final ChuteCallbacks INSTANCE = new ChuteCallbacks();

	private static final Key INVALID = new Key(false, Direction.NORTH, ImmutableList.of());

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof ChuteBlockEntity chuteBE))
			return getDefaultKey();
		List<Direction> solidWalls = new ArrayList<>();
		for(Direction dir : DirectionUtils.BY_HORIZONTAL_INDEX)
			if(!chuteBE.isInwardConveyor(dir))
				solidWalls.add(dir);
		return new Key(chuteBE.isDiagonal(), chuteBE.getFacing(), solidWalls);
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public IEObjState getIEOBJState(Key key)
	{
		if(key.diagonal())
		{
			Matrix4f matrix = new Matrix4(key.facing().getOpposite()).toMatrix4f();
			return new IEObjState(VisibilityList.show("diagonal"), new Transformation(matrix));
		}
		else
		{
			List<String> walls = new ArrayList<>(key.solidWalls().stream()
					.map(d -> d.name().toLowerCase(Locale.US))
					.toList());
			walls.add("base");
			return new IEObjState(VisibilityList.show(walls));
		}
	}

	public record Key(boolean diagonal, Direction facing, List<Direction> solidWalls)
	{
	}
}
