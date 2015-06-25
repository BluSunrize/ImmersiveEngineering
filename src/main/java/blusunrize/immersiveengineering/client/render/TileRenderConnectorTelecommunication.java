package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorTelecommunication;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderConnectorTelecommunication extends TileRenderImmersiveConnectable{

	ModelIEObj model = new ModelIEObj("immersiveengineering:models/connectorLV.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalDevice.getIcon(0, BlockMetalDevices.META_connectorTC);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, .5, .5);

		TileEntityConnectorTelecommunication connector = (TileEntityConnectorTelecommunication)tile;
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

		model.render(tile, tes, translationMatrix, rotationMatrix, true, false);
	}

}
