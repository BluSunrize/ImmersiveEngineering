package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderConnectorStructural extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/connectorStructural.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalDecoration.getIcon(0, BlockMetalDecoration.META_connectorStructural);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, .5, .5);

		TileEntityConnectorStructural connector = (TileEntityConnectorStructural)tile;
		switch(connector.facing)
		{
		case 0:
			break;
		case 1:
			rotationMatrix.rotate(Math.toRadians(180), 0,0,1);
			break;
		case 2:
			rotationMatrix.rotate(Math.toRadians(90), 1,0,0);
			break;
		case 3:
			rotationMatrix.rotate(Math.toRadians(-90), 1,0,0);
			break;
		case 4:
			rotationMatrix.rotate(Math.toRadians(-90), 0,0,1);
			break;
		case 5:
			rotationMatrix.rotate(Math.toRadians(90), 0,0,1);
			break;
		}
		rotationMatrix.rotate(Math.toRadians(connector.rotation), 0,1,0);
		

		model.render(tile, tes, translationMatrix, rotationMatrix, 1, false);
	}

}