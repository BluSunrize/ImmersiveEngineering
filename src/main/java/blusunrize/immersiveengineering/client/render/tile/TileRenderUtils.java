package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.InvertingVertexBuffer;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.Direction;

public class TileRenderUtils
{
	public static <T extends IMirrorAble & IDirectionalTile> IRenderTypeBuffer mirror(
			T tile,
			MatrixStack mat,
			IRenderTypeBuffer builderIn
	)
	{
		if(!tile.getIsMirrored())
			return builderIn;
		else
		{
			Direction facing = tile.getFacing();
			mat.scale(facing.getXOffset()==0?-1: 1, 1, facing.getZOffset()==0?-1: 1);
			return type -> new InvertingVertexBuffer(4, builderIn.getBuffer(type));
		}
	}
}
