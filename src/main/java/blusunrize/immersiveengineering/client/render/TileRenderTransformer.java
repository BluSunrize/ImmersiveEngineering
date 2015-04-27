package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelPostTransformer;
import blusunrize.immersiveengineering.client.models.ModelTransformer;
import blusunrize.immersiveengineering.client.models.ModelTransformerHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;

public class TileRenderTransformer extends TileEntitySpecialRenderer
{
	static ModelTransformer model = new ModelTransformer();
	static ModelTransformerHV modelHV = new ModelTransformerHV();
	static ModelPostTransformer modelTransformer = new ModelPostTransformer();

	boolean hv;
	public TileRenderTransformer(boolean hv)
	{
		this.hv=hv;
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityTransformer transf = (TileEntityTransformer)tile;
		if(transf.dummy)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		if(!hv)
		{
			if(transf.postAttached!=0)
			{
				modelTransformer.Transformer.rotateAngleY=(float)Math.toRadians(transf.facing==2?180: transf.facing==3?0: transf.facing==4?-90: 90);
				ClientUtils.bindTexture("immersiveengineering:textures/models/transformer_post.png");
				modelTransformer.render(null, 0,0,0,0,0, .0625f);
			}
			else
			{
				model.Transformer.rotateAngleY=(float)Math.toRadians(transf.facing==2?180: transf.facing==3?0: transf.facing==4?-90: 90);
				ClientUtils.bindTexture("immersiveengineering:textures/models/transformer.png");
				model.render(null, 0, 0, 0, 0, 0, .0625f);
			}
		}
		else
		{
			ClientUtils.bindTexture("immersiveengineering:textures/models/transformerHV.png");
			modelHV.Transformer.rotateAngleY=(float)Math.toRadians(transf.facing==2?180: transf.facing==3?0: transf.facing==4?-90: 90);
			modelHV.ceramicL.isHidden=transf.getLimiter(0)==WireType.STEEL;
			modelHV.ceramicL_HV.isHidden=transf.getLimiter(0)!=WireType.STEEL;
			modelHV.ceramicR.isHidden=transf.getLimiter(1)==WireType.STEEL;
			modelHV.ceramicR_HV.isHidden=transf.getLimiter(1)!=WireType.STEEL;
			modelHV.render(null, 0, 0, 0, 0, 0, .0625f);
		}

		ClientUtils.renderAttachedConnections(transf);
		
		GL11.glPopMatrix();
	}

}