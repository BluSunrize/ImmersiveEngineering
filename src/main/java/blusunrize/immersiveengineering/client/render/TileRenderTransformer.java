package blusunrize.immersiveengineering.client.render;

import java.util.Iterator;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelPostTransformer;
import blusunrize.immersiveengineering.client.models.ModelTransformer;
import blusunrize.immersiveengineering.client.models.ModelTransformerHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.util.Utils;

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
		GL11.glPushMatrix();
		GL11.glTranslated(.5, .5, .5);
		switch(transf.facing)
		{
		case 2:
			GL11.glRotatef(180, 0, 1, 0);
			break;
		case 3:
			break;
		case 4:
			GL11.glRotatef(-90, 0, 1, 0);
			break;
		case 5:
			GL11.glRotatef(90, 0, 1, 0);
			break;
		}
		GL11.glRotatef(180, 1, 0, 0);
		if(!hv)
		{
			if(transf.postAttached!=0)
			{
				GL11.glTranslated(0,-1,1);
				ClientUtils.bindTexture("immersiveengineering:textures/models/transformer_post.png");
				modelTransformer.render(null, 0,0,0,0,0, .0625f);
			}
			else
			{
				ClientUtils.bindTexture("immersiveengineering:textures/models/transformer.png");
				model.render(null, 0, 0, 0, 0, 0, .0625f);
			}
		}
		else
		{
			ClientUtils.bindTexture("immersiveengineering:textures/models/transformerHV.png");
			modelHV.render(transf.getLimiter(0)==WireType.STEEL, transf.getLimiter(1)==WireType.STEEL);
		}
		GL11.glPopMatrix();


		if(tile.getWorldObj()!=null)
		{
			ClientUtils.bindTexture("immersiveengineering:textures/models/white.png");
			Iterator<ImmersiveNetHandler.Connection> itCon = ImmersiveNetHandler.getConnections(transf.getWorldObj(), Utils.toCC(transf)).iterator();
			while(itCon.hasNext())
			{
				ImmersiveNetHandler.Connection con = itCon.next();
				TileEntity tileEnd = transf.getWorldObj().getTileEntity(con.end.posX,con.end.posY,con.end.posZ);
				if(tileEnd instanceof IImmersiveConnectable)
					ClientUtils.drawConnection(con, transf, Utils.toIIC(tileEnd, transf.getWorldObj()));
			}
		}
		GL11.glPopMatrix();
	}

}