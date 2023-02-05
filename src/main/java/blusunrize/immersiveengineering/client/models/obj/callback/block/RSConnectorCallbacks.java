/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.obj.callback.block.RSConnectorCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorRedstoneBlockEntity;
import org.joml.Vector4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class RSConnectorCallbacks implements BlockCallback<Key>
{
	public static final RSConnectorCallbacks INSTANCE = new RSConnectorCallbacks();
	private static final Key INVALID = new Key(IOSideConfig.INPUT, DyeColor.WHITE);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof ConnectorRedstoneBlockEntity connector))
			return getDefaultKey();
		return new Key(connector.ioMode, connector.redstoneChannel);
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		if("io_out".equals(group))
			return key.ioMode()==IOSideConfig.OUTPUT;
		else if("io_in".equals(group))
			return key.ioMode()==IOSideConfig.INPUT;
		return true;
	}

	@Override
	public Vector4f getRenderColor(Key key, String group, String material, ShaderCase shader, Vector4f original)
	{
		if("coloured".equals(group))
		{
			float[] rgb = key.channel().getTextureDiffuseColors();
			return new Vector4f(rgb[0], rgb[1], rgb[2], 1);
		}
		return original;
	}

	public record Key(IOSideConfig ioMode, DyeColor channel)
	{
	}
}
