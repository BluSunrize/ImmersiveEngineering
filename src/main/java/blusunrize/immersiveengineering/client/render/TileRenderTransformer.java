package blusunrize.immersiveengineering.client.render;

import java.util.ArrayList;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderTransformer extends TileRenderIE
{
	//	static ModelTransformer model = new ModelTransformer();
	//	static ModelTransformerHV modelHV = new ModelTransformerHV();
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
		//		rotationMatrix.rotate(Math.toRadians(arm.facing==2?270: arm.facing==3?90: arm.facing==4?0: 180 ), 0,1,0);
		//		rotationMatrix.rotate(arm.inverted?3.14159f:0, 1,0,0);

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
			modelPost.render(tile, tes, translationMatrix, rotationMatrix, false);
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
			model.render(tile, tes, translationMatrix, rotationMatrix, true, list.toArray(new String[0]));
		}
	}

	//	boolean hv;
	//	public TileRenderTransformer(boolean hv)
	//	{
	//		this.hv=hv;
	//	}

	//	@Override
	//	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	//	{
	//		TileEntityTransformer transf = (TileEntityTransformer)tile;
	//		if(transf.dummy)
	//			return;
	//		GL11.glPushMatrix();
	//
	//		GL11.glTranslated(x, y, z);
	//		if(!hv)
	//		{
	//			if(transf.postAttached!=0)
	//			{
	//				modelTransformer.Transformer.rotateAngleY=(float)Math.toRadians(transf.facing==2?180: transf.facing==3?0: transf.facing==4?-90: 90);
	//				ClientUtils.bindTexture("immersiveengineering:textures/models/transformer_post.png");
	//				modelTransformer.render(null, 0,0,0,0,0, .0625f);
	//			}
	//			else
	//			{
	//				model.Transformer.rotateAngleY=(float)Math.toRadians(transf.facing==2?180: transf.facing==3?0: transf.facing==4?-90: 90);
	//				ClientUtils.bindTexture("immersiveengineering:textures/models/transformer.png");
	//				model.render(null, 0, 0, 0, 0, 0, .0625f);
	//			}
	//		}
	//		else
	//		{
	//			ClientUtils.bindTexture("immersiveengineering:textures/models/transformerHV.png");
	//			modelHV.Transformer.rotateAngleY=(float)Math.toRadians(transf.facing==2?180: transf.facing==3?0: transf.facing==4?-90: 90);
	//			modelHV.ceramicL.isHidden=transf.getLimiter(0)==WireType.STEEL;
	//			modelHV.ceramicL_HV.isHidden=transf.getLimiter(0)!=WireType.STEEL;
	//			modelHV.ceramicR.isHidden=transf.getLimiter(1)==WireType.STEEL;
	//			modelHV.ceramicR_HV.isHidden=transf.getLimiter(1)!=WireType.STEEL;
	//			modelHV.render(null, 0, 0, 0, 0, 0, .0625f);
	//		}
	//
	//		ClientUtils.renderAttachedConnections(transf);
	//		
	//		GL11.glPopMatrix();
	//	}

}