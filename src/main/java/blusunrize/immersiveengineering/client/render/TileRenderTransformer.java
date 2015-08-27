package blusunrize.immersiveengineering.client.render;

import java.util.ArrayList;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderTransformer extends TileRenderIE
{
	static ModelIEObj model = new ModelIEObj("immersiveengineering:models/transformerHV.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalDevice.getIcon(0, BlockMetalDevices.META_transformerHV);
		}
	};
	static ModelIEObj modelPost = new ModelIEObj("immersiveengineering:models/transformerPost.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalDevice.getIcon(1, BlockMetalDevices.META_transformer);
		}
	};

	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
	}

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, .5, .5);

		TileEntityTransformer transformer = (TileEntityTransformer)tile;

		switch(transformer.facing)
		{
		case 2:
			rotationMatrix.rotate(Math.toRadians(180), 0,1,0);
			break;
		case 3:
			break;
		case 4:
			rotationMatrix.rotate(Math.toRadians(-90), 0,1,0);
			break;
		case 5:
			rotationMatrix.rotate(Math.toRadians(90), 0,1,0);
			break;
		}

		if(transformer.postAttached!=0)
		{
			modelPost.render(tile, tes, translationMatrix, rotationMatrix, -1, false);
		}
		else
		{
			ArrayList<String> list = new ArrayList();
			list.add("Base");
			if(transformer instanceof TileEntityTransformerHV)
			{
				if(transformer.getLimiter(0)==WireType.STEEL)
					list.add("ConnectorHV_Left");
				else
					list.add("Connector_Left");
				if(transformer.getLimiter(1)==WireType.STEEL)
					list.add("ConnectorHV_Right");
				else
					list.add("Connector_Right");
			}
			else
			{
				list.add("Connector_Left");
				list.add("Connector_Right");
			}
			model.render(tile, tes, translationMatrix, rotationMatrix, 1, false, list.toArray(new String[list.size()]));
		}
	}
}