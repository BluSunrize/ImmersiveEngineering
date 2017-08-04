package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(Side.CLIENT)
public class TileRenderImmersiveConnectable extends TileEntitySpecialRenderer<TileEntityImmersiveConnectable>
{
	private static Map<IImmersiveConnectable, VertexBuffer> cache = new HashMap<>();
	private final static VertexFormat FORMAT = DefaultVertexFormats.POSITION_TEX_LMAP_COLOR;

	public TileRenderImmersiveConnectable() {
		IEApi.renderCacheClearers.add(cache::clear);
	}

	@Override
	public void render(TileEntityImmersiveConnectable te, double x, double y, double z, float partialTicks, int destroyStage, float partial)
	{
		GlStateManager.pushMatrix();
		GlStateManager.color(255, 255, 255, 255);


		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.bindTexture(OpenGlHelper.defaultTexUnit);
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();
		if(te != null)
		{
			if (cache.containsKey(te)) {
				GlStateManager.translate(x, y, z);
				VertexBuffer vbo = cache.get(te);
				vbo.bindBuffer();
				int stride = FORMAT.getNextOffset();
				ByteBuffer byteBuff = Tessellator.getInstance().getBuffer().getByteBuffer();

				GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);

				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
				GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

				OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
				GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
				GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);

				GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
				GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12);
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
				GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, stride, 20);
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
				GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 24);

				vbo.drawArrays(GL11.GL_QUADS);
				vbo.unbindBuffer();
				byteBuff.limit(0);
				for (int i = 0; i < FORMAT.getElementCount(); i++)
					FORMAT.getElements().get(i).getUsage().postDraw(FORMAT, i, FORMAT.getNextOffset(), byteBuff);
				byteBuff.position(0);
			}
			else
			{
				buffer.begin(GL11.GL_QUADS, FORMAT);
				VertexBuffer vbo = new VertexBuffer(FORMAT);

				Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(te.getWorld(), Utils.toCC(te));
				if (outputs != null)
				{
					Iterator<Connection> itCon = outputs.iterator();
					while (itCon.hasNext())
					{
						Connection con = itCon.next();
						TileEntity end = getWorld().getTileEntity(con.end);
						if (con.end.compareTo(con.start) > 0 && end instanceof IImmersiveConnectable)
						{
							ClientUtils.tessellateConnection(con, te, (IImmersiveConnectable) end, con.cableType.getIcon(con), buffer,
									 - te.getPos().getX(),
									 - te.getPos().getY(),
									 - te.getPos().getZ());
						}
					}
				}
				buffer.finishDrawing();
				buffer.reset();
				vbo.bufferData(buffer.getByteBuffer());
				cache.put(te, vbo);
			}
			GlStateManager.enableRescaleNormal();
			GlStateManager.popAttrib();
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.popMatrix();
			buffer.setTranslation(0, 0, 0);
		}
	}

	@Override
	public boolean isGlobalRenderer(TileEntityImmersiveConnectable te)
	{
		return true;
	}

	public static void reset(IImmersiveConnectable toRemove) {
		if (cache.containsKey(toRemove)) {
			VertexBuffer vb = cache.get(toRemove);
			vb.deleteGlBuffers();
			cache.remove(toRemove);
		}
	}
}