package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.Set;
// not used any more
public class TileRenderImmersiveConnectable extends TileEntitySpecialRenderer
{
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		//		Tessellator tes = ClientUtils.tes();
		//		WorldRenderer worldrenderer = tes.getWorldRenderer();
		//		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		//		worldrenderer.pos(1, .5, 0).color(1f, 1f, 1f, 1f).endVertex();
		//		worldrenderer.pos(1, 2, 0).color(1f, 1f, 1f, 1f).endVertex();
		//		worldrenderer.pos(0, 2, 0).color(1f, 1f, 1f, 1f).endVertex();
		//		worldrenderer.pos(0, .5, 0).color(1f, 1f, 1f, 1f).endVertex();
		//		
		//		worldrenderer.pos(0, .5, 1).color(1f, 1f, 1f, 1f).endVertex();
		//		worldrenderer.pos(0, 2, 1).color(1f, 1f, 1f, 1f).endVertex();
		//		worldrenderer.pos(0, 2, 0).color(1f, 1f, 1f, 1f).endVertex();
		//		worldrenderer.pos(0, .5, 0).color(1f, 1f, 1f, 1f).endVertex();
		//		
		//		tes.draw();
		if(te instanceof IImmersiveConnectable && te.getWorld()!=null)
		{
			Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(te.getWorld(), Utils.toCC(te));
			if(outputs!=null)
			{
				VertexBuffer worldrenderer = ClientUtils.tes().getBuffer();
				worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				//		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
				//		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
				GlStateManager.disableLighting();
				GlStateManager.disableCull();
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				Iterator<Connection> itCon = outputs.iterator();
				while(itCon.hasNext())
				{
					Connection con = itCon.next();
					TileEntity tileEnd = te.getWorld().getTileEntity(con.end);
					if(tileEnd instanceof IImmersiveConnectable)
					{
						ClientUtils.tessellateConnection(con, (IImmersiveConnectable)te, ApiUtils.toIIC(tileEnd, te.getWorld()), con.cableType.getIcon(con));
					}
				}

				GlStateManager.enableLighting();
				GlStateManager.enableTexture2D();
				ClientUtils.tes().draw();
			}
		}
		GlStateManager.popMatrix();
	}
}