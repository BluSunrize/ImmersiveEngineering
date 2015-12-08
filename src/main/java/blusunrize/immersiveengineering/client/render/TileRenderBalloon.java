package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.cloth.TileEntityBalloon;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public class TileRenderBalloon extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/balloon.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockClothDevice.getIcon(0, 0);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, 0, .5);

		TileEntityBalloon balloon = (TileEntityBalloon)tile;
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "base");
		float[] col0 = EntitySheep.fleeceColorTable[15-balloon.colour0];
		float[] col1 = EntitySheep.fleeceColorTable[15-balloon.colour1];
		if(balloon.style==0)
		{
			ClientUtils.renderStaticWavefrontModel(tile, model.model, tes, translationMatrix, rotationMatrix, -1,false, col0[0],col0[1],col0[2], "balloon0_0","balloon0_1");
			ClientUtils.renderStaticWavefrontModel(tile, model.model, tes, translationMatrix, rotationMatrix, -1,false, col1[0],col1[1],col1[2], "balloon1_0","balloon1_1");
		}
		else
		{
			ClientUtils.renderStaticWavefrontModel(tile, model.model, tes, translationMatrix, rotationMatrix, -1,false, col0[0],col0[1],col0[2], "balloon0_0","balloon1_0");
			ClientUtils.renderStaticWavefrontModel(tile, model.model, tes, translationMatrix, rotationMatrix, -1,false, col1[0],col1[1],col1[2], "balloon0_1","balloon1_1");
		}
	}

}