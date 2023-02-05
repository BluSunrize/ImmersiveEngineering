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
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.block.FloodlightCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.metal.FloodlightBlockEntity;
import com.mojang.math.Transformation;
import org.joml.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class FloodlightCallbacks implements BlockCallback<Key>
{
	public static final FloodlightCallbacks INSTANCE = new FloodlightCallbacks();
	private static final Key INVALID = new Key(Direction.NORTH, Direction.DOWN, 0, 0);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof FloodlightBlockEntity floodlight))
			return getDefaultKey();
		return new Key(
				floodlight.facing, floodlight.getFacing(), floodlight.rotX, floodlight.rotY
		);
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public boolean dependsOnLayer()
	{
		return true;
	}

	@Override
	public boolean shouldRenderGroup(Key object, String group, RenderType layer)
	{
		if("glass".equals(group))
			return layer==RenderType.translucent();
		else
			return layer==RenderType.solid();
	}

	@Override
	public Transformation applyTransformations(Key object, String group, Transformation transform)
	{
		Vector3f transl = new Vector3f(.5f, .5f, .5f);

		double yaw = 0;
		double pitch = 0;
		double roll = 0;
		Direction side = object.side();
		Direction facing = object.facing();
		float rotX = object.rotX();
		float rotY = object.rotY();

		//		pitch, yaw, roll
		if(side.getAxis()==Axis.Y)
		{
			yaw = facing==Direction.SOUTH?180: facing==Direction.WEST?90: facing==Direction.EAST?-90: 0;
			if(side==Direction.DOWN)
				roll = 180;
		}
		else //It's a mess, but it works!
		{
			if(side==Direction.NORTH)
			{
				pitch = 90;
				yaw = 180;
			}
			if(side==Direction.SOUTH)
				pitch = 90;
			if(side==Direction.WEST)
			{
				pitch = 90;
				yaw = -90;
			}
			if(side==Direction.EAST)
			{
				pitch = 90;
				yaw = 90;
			}

			if(facing==Direction.DOWN)
				roll += 180;
			else if(side.getAxis()==Axis.X&&facing.getAxis()==Axis.Z)
				roll += 90*facing.getAxisDirection().getStep()*side.getAxisDirection().getStep();
			else if(side.getAxis()==Axis.Z&&facing.getAxis()==Axis.X)
				roll += -90*facing.getAxisDirection().getStep()*side.getAxisDirection().getStep();
		}

		transl.add(new Vector3f(side.getStepX()*.125f, side.getStepY()*.125f, side.getStepZ()*.125f));
		if("axis".equals(group)||"light".equals(group)||"off".equals(group)||"glass".equals(group))
		{
			if(side.getAxis()==Axis.Y)
				yaw += rotY;
			else
				roll += rotY;
			if("light".equals(group)||"off".equals(group)||"glass".equals(group))
				pitch += rotX;
		}
		return new Transformation(
				transl,
				ClientUtils.degreeToQuaterion(pitch, yaw, roll),
				null, null
		).blockCornerToCenter();
	}

	public record Key(
			Direction facing, Direction side, float rotX, float rotY
	)
	{
	}
}
