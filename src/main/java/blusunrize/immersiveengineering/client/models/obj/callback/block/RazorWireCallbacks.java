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
import blusunrize.immersiveengineering.client.models.obj.callback.block.RazorWireCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.metal.RazorWireBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class RazorWireCallbacks implements BlockCallback<Key>
{
	public static final RazorWireCallbacks INSTANCE = new RazorWireCallbacks();
	private static final Key INVALID = new Key(false, false, true, true);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof RazorWireBlockEntity razorWire))
			return getDefaultKey();
		return new Key(
				razorWire.isStacked(), razorWire.isOnGround(), razorWire.renderWall(true), razorWire.renderWall(false)
		);
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public boolean shouldRenderGroup(Key object, String group, RenderType layer)
	{
		if(group==null)
			return false;
		if(!object.stacked()&&!object.onGround())
			return !group.startsWith("wood");
		if(group.startsWith("wood")&&!(group.endsWith("inverted")==object.stacked()))
			return false;
		if(group.startsWith("wood_left"))
			return object.leftWall();
		else if("wire_left".equals(group)||"barbs_left".equals(group))
			return !object.leftWall();
		else if(group.startsWith("wood_right"))
			return object.rightWall();
		else if("wire_right".equals(group)||"barbs_right".equals(group))
			return !object.rightWall();
		return true;
	}

	public record Key(boolean stacked, boolean onGround, boolean leftWall, boolean rightWall)
	{
	}
}
