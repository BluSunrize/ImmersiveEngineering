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
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.client.models.obj.callback.block.ProbeConnectorCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorProbeBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class ProbeConnectorCallbacks implements BlockCallback<Key>
{
	public static final ProbeConnectorCallbacks INSTANCE = new ProbeConnectorCallbacks();
	private static final Key INVALID = new Key(DyeColor.WHITE, DyeColor.WHITE);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof ConnectorProbeBlockEntity probe))
			return getDefaultKey();
		return new Key(probe.redstoneChannel, probe.redstoneChannelSending);
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
	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		if("glass".equals(group))
			return layer==RenderType.translucent();
		return layer==RenderType.cutout();
	}

	@Override
	public Color4 getRenderColor(Key key, String group, String material, ShaderCase shader, Color4 original)
	{
		if("colour_in".equals(group))
			return Color4.from(key.receiving);
		else if("colour_out".equals(group))
			return Color4.from(key.sending);
		return original;
	}

	public record Key(DyeColor receiving, DyeColor sending)
	{
	}
}
