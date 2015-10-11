package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLantern;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderLantern extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/lantern.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockMetalDecoration.getIcon(0, BlockMetalDecoration.META_lantern);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, 0, .5);

		TileEntityLantern lantern = (TileEntityLantern)tile;
		String[] render = new String[]{"base",""};

		render[1]= lantern.facing==1?"attach_b": lantern.facing==0?"attach_t": "attach_s";
		switch(lantern.facing)
		{
		case 0:
			translationMatrix.translate(0, .0625, .0);
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			rotationMatrix.rotate(Math.toRadians(180), 0,1,0);
			break;
		case 4:
			rotationMatrix.rotate(Math.toRadians(90), 0,1,0);
			break;
		case 5:
			rotationMatrix.rotate(Math.toRadians(-90), 0,1,0);
			break;
		}

		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, render);
	}

}