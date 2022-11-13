/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.InvertingVertexBuffer;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalBE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public class BERenderUtils
{
	public static <T extends IMirrorAble & IDirectionalBE> MultiBufferSource mirror(
			T tile, PoseStack mat, MultiBufferSource builderIn
	)
	{
		mirror(tile, mat);
		if(!tile.getIsMirrored())
			return builderIn;
		else
			return type -> new InvertingVertexBuffer(4, builderIn.getBuffer(type));
	}

	public static MultiBufferSource mirror(
			MultiblockOrientation orientation, PoseStack mat, MultiBufferSource builderIn
	)
	{
		if(orientation.mirrored())
		{
			Direction facing = orientation.front();
			mat.scale(facing.getStepX()==0?-1: 1, 1, facing.getStepZ()==0?-1: 1);
			return type -> new InvertingVertexBuffer(4, builderIn.getBuffer(type));
		}
		else
			return builderIn;
	}

	public static <T extends IMirrorAble & IDirectionalBE> void mirror(T tile, PoseStack mat)
	{
		if(tile.getIsMirrored())
		{
			Direction facing = tile.getFacing();
			mat.scale(facing.getStepX()==0?-1: 1, 1, facing.getStepZ()==0?-1: 1);
		}
	}
}
