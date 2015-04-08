package blusunrize.immersiveengineering.client;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;

public class ClientUtils
{
	// MOD SPECIFIC METHODS
	public static void drawConnection(ImmersiveNetHandler.Connection connection, IImmersiveConnectable start, IImmersiveConnectable end)
	{
		Vec3 startOffset = start.getConnectionOffset(connection);
		Vec3 endOffset = end.getConnectionOffset(connection);
		double dx = (connection.end.posX+endOffset.xCoord)-(connection.start.posX+startOffset.xCoord);
		double dy = (connection.end.posY+endOffset.yCoord)-(connection.start.posY+startOffset.yCoord);
		double dz = (connection.end.posZ+endOffset.zCoord)-(connection.start.posZ+startOffset.zCoord);
		double dw = Math.sqrt(dx*dx + dz*dz);

		GL11.glDisable(GL11.GL_CULL_FACE);
		int col = connection.cableType.getColour();
		double r = connection.cableType==WireType.STEEL?.03125:.015625;
		double rmodx = dz/dw;
		double rmodz = dx/dw;
		GL11.glTranslated(startOffset.xCoord,startOffset.yCoord,startOffset.zCoord);
		Tessellator tes = tes();
		World world = ((TileEntity)start).getWorldObj();

		double k = Math.sqrt(dx*dx + dy*dy + dz*dz) * 1.005;
		double l = 0;
		int limiter = 0;
		boolean vertical = Math.abs(dx)<.05&&Math.abs(dz)<.05;
		while(!vertical && true && limiter<100)
		{
			limiter++;
			l += 0.01;
			if (Math.sinh(l)/l >= Math.sqrt(k*k - dy*dy)/dw)
				break;
		}
		if(limiter>=100)
			System.out.println("Catenary loop greatly exceeded its maximum at "+limiter);
		double a = dw/2/l;
		double p = (0+dw-a*Math.log((k+dy)/(k-dy)))*0.5;
		double q = (dy+0-k*Math.cosh(l)/Math.sinh(l))*0.5;

		int vertices = 16;
		if(vertical)
		{
			tes.startDrawing(GL11.GL_QUADS);
			tes.setColorOpaque_I(col);
			tes.setBrightness(calcBrightness(world, connection.start.posX-r,connection.start.posY,connection.start.posZ));
			tes.addVertex(0-r, 0, 0);
			tes.setBrightness(calcBrightness(world, connection.start.posX-r,connection.start.posY+dy,connection.start.posZ));
			tes.addVertex(0-r, dy, 0);
			tes.setBrightness(calcBrightness(world, connection.start.posX+r,connection.start.posY+dy,connection.start.posZ));
			tes.addVertex(0+r, dy, 0);
			tes.setBrightness(calcBrightness(world, connection.start.posX+r,connection.start.posY,connection.start.posZ));
			tes.addVertex(0+r, 0, 0);
			tes.draw();
			tes.startDrawing(GL11.GL_QUADS);
			tes.setColorOpaque_I(col);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY,connection.start.posZ-r));
			tes.addVertex(0, 0, 0-r);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY+dy,connection.start.posZ-r));
			tes.addVertex(0, dy, 0-r);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY+dy,connection.start.posZ+r));
			tes.addVertex(0, dy, 0+r);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY,connection.start.posZ+r));
			tes.addVertex(0, 0, 0+r);
			tes.draw();
		}
		else
			for(int i=0; i<vertices; i++)
			{
				float n0 = i/(float)vertices;
				float n1 = (i+1)/(float)vertices;

				double x0 = 0 + dx * n0;
				double z0 = 0 + dz * n0;
				double y0 = a * Math.cosh((( Math.sqrt(x0*x0+z0*z0) )-p)/a)+q;
				double x1 = 0 + dx * n1;
				double z1 = 0 + dz * n1;
				double y1 = a * Math.cosh((( Math.sqrt(x1*x1+z1*z1) )-p)/a)+q;

				tes.startDrawing(GL11.GL_QUADS);
				tes.setColorOpaque_I(col);
				tes.setBrightness(calcBrightness(world, connection.start.posX+x0, connection.start.posY+y0+r, connection.start.posZ+z0));
				tes.addVertex(x0, y0+r, z0);
				tes.setBrightness(calcBrightness(world, connection.start.posX+x1, connection.start.posY+y1+r, connection.start.posZ+z1));
				tes.addVertex(x1, y1+r, z1);
				tes.setBrightness(calcBrightness(world, connection.start.posX+x1, connection.start.posY+y1-r, connection.start.posZ+z1));
				tes.addVertex(x1, y1-r, z1);
				tes.setBrightness(calcBrightness(world, connection.start.posX+x0, connection.start.posY+y0-r, connection.start.posZ+z0));
				tes.addVertex(x0, y0-r, z0);
				tes.draw();
				tes.startDrawing(GL11.GL_QUADS);
				tes.setColorOpaque_I(col);
				tes.setBrightness(calcBrightness(world, connection.start.posX+x0-r*rmodx, connection.start.posY+y0, connection.start.posZ+z0+r*rmodz));
				tes.addVertex(x0-r*rmodx, y0, z0+r*rmodz);
				tes.setBrightness(calcBrightness(world, connection.start.posX+x1-r*rmodx, connection.start.posY+y1, connection.start.posZ+z1+r*rmodz));
				tes.addVertex(x1-r*rmodx, y1, z1+r*rmodz);
				tes.setBrightness(calcBrightness(world, connection.start.posX+x1+r*rmodx, connection.start.posY+y1, connection.start.posZ+z1-r*rmodz));
				tes.addVertex(x1+r*rmodx, y1, z1-r*rmodz);
				tes.setBrightness(calcBrightness(world, connection.start.posX+x0+r*rmodx, connection.start.posY+y0, connection.start.posZ+z0-r*rmodz));
				tes.addVertex(x0+r*rmodx, y0, z0-r*rmodz);
				tes.draw();
			}
		GL11.glEnable(GL11.GL_CULL_FACE);
		//		boolean invert = connection.start.posY>=connection.end.posY;
		//
		//		for(int i=0; i<vertices; ++i)
		//		{
		//			float f12I = (float)i / (float)vertices;
		//			float f12J = (float)(i+1) / (float)vertices;
		//			double yModI = 0;
		//			double yModJ = 0;
		//			if(dy==0)
		//			{
		//				yModI = Math.abs(f12I-.5);
		//				yModI *= yModI*modW;
		//				yModI -= .25*modW;
		//				yModJ = Math.abs(f12J-.5);
		//				yModJ *= yModJ*modW;
		//				yModJ -= .25*modW;
		//			}
		//
		//			double y0 = dy*(f12I*f12I+f12I)*.5-yModI;
		//			double y1 = dy*(f12J*f12J+f12J)*.5-yModJ;
		//			if(!invert)
		//			{
		//				tes.startDrawing(GL11.GL_QUADS);
		//				tes.setColorOpaque_I(col);
		//				tes.addVertex(0+dx*f12I, y0+r, 0+dz*f12I);
		//				tes.addVertex(0+dx*f12J, y1+r, 0+dz*f12J);
		//				tes.addVertex(0+dx*f12J, y1-r, 0+dz*f12J);
		//				tes.addVertex(0+dx*f12I, y0-r, 0+dz*f12I);
		//				tes.draw();
		//
		//				tes.startDrawing(GL11.GL_QUADS);
		//				tes.setColorOpaque_I(col);
		//				tes.addVertex(0+dx*f12I+r, y0, 0+dz*f12I);
		//				tes.addVertex(0+dx*f12J+r, y1, 0+dz*f12J);
		//				tes.addVertex(0+dx*f12J-r, y1, 0+dz*f12J);
		//				tes.addVertex(0+dx*f12I-r, y0, 0+dz*f12I);
		//				tes.draw();
		//			}
		//			else
		//			{
		//				tes.startDrawing(GL11.GL_QUADS);
		//				tes.setColorOpaque_I(col);
		//				tes.addVertex(dx-dx*f12I, dy-y0+r, dz-dz*f12I);
		//				tes.addVertex(dx-dx*f12J, dy-y1+r, dz-dz*f12J);
		//				tes.addVertex(dx-dx*f12J, dy-y1-r, dz-dz*f12J);
		//				tes.addVertex(dx-dx*f12I, dy-y0-r, dz-dz*f12I);
		//				tes.draw();
		//				//				
		//				tes.startDrawing(GL11.GL_QUADS);
		//				tes.setColorOpaque_I(col);
		//				tes.addVertex(dx-dx*f12I+r, dy-y0, dz-dz*f12I);
		//				tes.addVertex(dx-dx*f12J+r, dy-y1, dz-dz*f12J);
		//				tes.addVertex(dx-dx*f12J-r, dy-y1, dz-dz*f12J);
		//				tes.addVertex(dx-dx*f12I-r, dy-y0, dz-dz*f12I);
		//				tes.draw();
		//			}
		//		}

		GL11.glTranslated(-startOffset.xCoord,-startOffset.yCoord,-startOffset.zCoord);
	}
	public static int calcBrightness(IBlockAccess world, double x, double y, double z)
	{
		return world.getLightBrightnessForSkyBlocks((int)Math.round(x), (int)Math.round(y), (int)Math.round(z), 0);
	}


	// GENERAL METHODS
	static HashMap<String, ResourceLocation> resourceMap = new HashMap<String, ResourceLocation>();

	public static Tessellator tes()
	{
		return Tessellator.instance;
	}
	public static Minecraft mc()
	{
		return Minecraft.getMinecraft();
	}
	public static void bindTexture(String path)
	{
		mc().getTextureManager().bindTexture(getResource(path));
	}
	public static ResourceLocation getResource(String path)
	{
		ResourceLocation rl = resourceMap.containsKey(path) ? resourceMap.get(path) : new ResourceLocation(path);
		if(!resourceMap.containsKey(path))
			resourceMap.put(path, rl);
		return rl;
	}
	public static FontRenderer font()
	{
		return mc().fontRenderer;
	}

	public static String formatDouble(double d, String s)
	{
		DecimalFormat df = new DecimalFormat(s);
		System.out.println("d="+d+", df="+s);
		return df.format(d);
	}
	public static String toCamelCase(String s)
	{
		return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
	}

	public static void drawInventoryBlock(Block block, int metadata, RenderBlocks renderer)
	{
		Tessellator tes = tes();
		GL11.glPushMatrix();
		GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tes.startDrawingQuads();
		tes.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
		tes.draw();
		GL11.glPopMatrix();
	}

	public static void drawColouredRect(int x, int y, int w, int h, int colour)
	{
		tes().startDrawingQuads();
		tes().setColorRGBA_I(colour, colour>>24&255);
		tes().addVertex(x, y+h, 0);
		tes().addVertex(x+w, y+h, 0);
		tes().addVertex(x+w, y, 0 );
		tes().addVertex(x, y, 0);
		tes().draw();
	}
	public static void drawGradientRect(int x0, int y0, int x1, int y1, int colour0, int colour1)
	{
		float f = (float)(colour0>>24&255)/255F;
		float f1 = (float)(colour0>>16&255)/255F;
		float f2 = (float)(colour0>>8&255)/255F;
		float f3 = (float)(colour0&255) / 255F;
		float f4 = (float)(colour1>>24&255)/255F;
		float f5 = (float)(colour1>>16&255)/255F;
		float f6 = (float)(colour1>>8&255)/255F;
		float f7 = (float)(colour1&255)/255F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(f1, f2, f3, f);
		tessellator.addVertex((double)x1, (double)y0, 0);
		tessellator.addVertex((double)x0, (double)y0, 0);
		tessellator.setColorRGBA_F(f5, f6, f7, f4);
		tessellator.addVertex((double)x0, (double)y1, 0);
		tessellator.addVertex((double)x1, (double)y1, 0);
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	public static void drawTexturedRect(int x, int y, int w, int h, double... uv)
	{
		tes().startDrawingQuads();
		tes().addVertexWithUV(x, y+h, 0, uv[0], uv[3]);
		tes().addVertexWithUV(x+w, y+h, 0, uv[1], uv[3]);
		tes().addVertexWithUV(x+w, y, 0, uv[1], uv[2]);
		tes().addVertexWithUV(x, y, 0, uv[0], uv[2]);
		tes().draw();
	}
	public static void drawTexturedRect(int x, int y, int w, int h, float picSize, int... uv)
	{
		double[] d_uv = new double[]{uv[0]/picSize,uv[1]/picSize, uv[2]/picSize,uv[3]/picSize};
		drawTexturedRect(x,y,w,h, d_uv);
	}
	public static void drawRepeatedFluidIcon(Fluid fluid, int x, int y, int w, int h)
	{
		bindTexture(TextureMap.locationBlocksTexture.toString());
		IIcon icon = fluid.getIcon();
		int iW = icon.getIconWidth();
		int iH = icon.getIconHeight();
		if(icon!=null && iW>0 && iH>0)
			drawRepeatedIcon(x,y,w,h, iW, iH, icon.getMinU(),icon.getMaxU(), icon.getMinV(),icon.getMaxV());
	}
	public static void drawRepeatedIcon(int x, int y, int w, int h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax)
	{
		int iterMaxW = w/iconWidth;
		int iterMaxH = h/iconHeight;
		int leftoverW = w%iconWidth;
		int leftoverH = h%iconHeight;
		float leftoverWf = leftoverW/(float)iconWidth;
		float leftoverHf = leftoverH/(float)iconHeight;
		float iconUDif = uMax-uMin;
		float iconVDif = vMax-vMin;
		for(int ww=0; ww<iterMaxW; ww++)
		{
			for(int hh=0; hh<iterMaxH; hh++)
				drawTexturedRect(x+ww*iconWidth, y+hh*iconHeight, iconWidth,iconHeight, uMin,uMax,vMin,vMax);
			drawTexturedRect(x+ww*iconWidth, y+iterMaxH*iconHeight, iconWidth,leftoverH, uMin,uMax,vMin,(vMin+iconVDif*leftoverHf));
		}
		if(leftoverW>0)
		{
			for(int hh=0; hh<iterMaxH; hh++)
				drawTexturedRect(x+iterMaxW*iconWidth, y+hh*iconHeight, leftoverW,iconHeight, uMin,(uMin+iconUDif*leftoverWf),vMin,vMax);
			drawTexturedRect(x+iterMaxW*iconWidth, y+iterMaxH*iconHeight, leftoverW,leftoverH, uMin,(uMin+iconUDif*leftoverWf),vMin,(vMin+iconVDif*leftoverHf));
		}
	}


	public static void renderToolTip(ItemStack stack, int x, int y)
	{
		List list = stack.getTooltip(mc().thePlayer, mc().gameSettings.advancedItemTooltips);

		for (int k = 0; k < list.size(); ++k)
			if (k == 0)
				list.set(k, stack.getRarity().rarityColor + (String)list.get(k));
			else
				list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));

		FontRenderer font = stack.getItem().getFontRenderer(stack);
		drawHoveringText(list, x, y, (font == null ? font() : font));
	}

	public static void drawHoveringText(List<String> list, int x, int y, FontRenderer font)
	{
		if (!list.isEmpty())
		{
			boolean uni = ClientUtils.font().getUnicodeFlag();
			ClientUtils.font().setUnicodeFlag(false);

			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;
			Iterator<String> iterator = list.iterator();
			while (iterator.hasNext())
			{
				String s = iterator.next();
				int l = font.getStringWidth(s);
				if (l > k)
					k = l;
			}

			int j2 = x + 12;
			int k2 = y - 12;
			int i1 = 8;

			if (list.size() > 1)
				i1 += 2 + (list.size() - 1) * 10;

			int j1 = -267386864;
			drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
			drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
			drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
			drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
			drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
			int k1 = 1347420415;
			int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
		drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
		drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
		drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
		drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

		for (int i2 = 0; i2 < list.size(); ++i2)
		{
			String s1 = (String)list.get(i2);
			font.drawStringWithShadow(s1, j2, k2, -1);

			if (i2 == 0)
				k2 += 2;

			k2 += 10;
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		ClientUtils.font().setUnicodeFlag(uni);
		}
	}

	public static class BlockLightingInfo
	{
		public int aoBrightnessXYNN;
		public int aoBrightnessYZNN;
		public int aoBrightnessYZNP;
		public int aoBrightnessXYPN;
		public float aoLightValueScratchXYNN;
		public float aoLightValueScratchYZNN;
		public float aoLightValueScratchYZNP;
		public float aoLightValueScratchXYPN;
		public float aoLightValueScratchXYZNNN;
		public int aoBrightnessXYZNNN;
		public float aoLightValueScratchXYZNNP;
		public int aoBrightnessXYZNNP;
		public float aoLightValueScratchXYZPNN;
		public int aoBrightnessXYZPNN;
		public float aoLightValueScratchXYZPNP;
		public int aoBrightnessXYZPNP;
		public int brightnessTopLeft;
		public int brightnessTopRight;
		public int brightnessBottomRight;
		public int brightnessBottomLeft;
		public float colorRedTopLeft;
		public float colorGreenTopLeft;
		public float colorBlueTopLeft;
		public float colorRedBottomLeft;
		public float colorRedBottomRight;
		public float colorRedTopRight;
		public float colorGreenTopRight;
		public float colorBlueTopRight;
		public float colorGreenBottomRight;
		public float colorBlueBottomRight;
		public float colorGreenBottomLeft;
		public float colorBlueBottomLeft;

		public int aoBrightnessXYNP;
		public int aoBrightnessXYPP;
		public int aoBrightnessYZPN;
		public int aoBrightnessYZPP;
		public float aoLightValueScratchXYNP;
		public float aoLightValueScratchXYPP;
		public float aoLightValueScratchYZPN;
		public float aoLightValueScratchYZPP;
		public float aoLightValueScratchXYZNPN;
		public int aoBrightnessXYZNPN;
		public float aoLightValueScratchXYZPPN;
		public int aoBrightnessXYZPPN;
		public float aoLightValueScratchXYZNPP;
		public int aoBrightnessXYZNPP;
		public float aoLightValueScratchXYZPPP;
		public int aoBrightnessXYZPPP;

		public float aoLightValueScratchXZNN;
		public float aoLightValueScratchXZPN;
		public int aoBrightnessXZNN;
		public int aoBrightnessXZPN;

		public float aoLightValueScratchXZNP;
		public float aoLightValueScratchXZPP;
		public int aoBrightnessXZNP;
		public int aoBrightnessXZPP;

		public int getAoBrightness(int par0, int par1, int par2, int par3)
		{
			if (par0 == 0)
				par0 = par3;
			if (par1 == 0)
				par1 = par3;
			if (par2 == 0)
				par2 = par3;
			return par0 + par1 + par2 + par3 >> 2 & 16711935;
		}
		public int mixAoBrightness(int par0, int par1, int par2, int par3, double par4, double par5, double par6, double par7)
		{
			int i1 = (int)((double)(par0 >> 16 & 255) * par4 + (double)(par1 >> 16 & 255) * par5 + (double)(par2 >> 16 & 255) * par6 + (double)(par3 >> 16 & 255) * par7) & 255;
			int j1 = (int)((double)(par0 & 255) * par4 + (double)(par1 & 255) * par5 + (double)(par2 & 255) * par6 + (double)(par3 & 255) * par7) & 255;
			return i1 << 16 | j1;
		}
	}
	public static BlockLightingInfo calculateBlockLighting(int side, IBlockAccess world, Block block, int x, int y, int z, float colR, float colG, float colB)
	{
		boolean flag = false;
		float f3 = 0.0F;
		float f4 = 0.0F;
		float f5 = 0.0F;
		float f6 = 0.0F;
		int l = block.getMixedBrightnessForBlock(world, x, y, z);
		Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(983055);

		boolean flag2;
		boolean flag3;
		boolean flag4;
		boolean flag5;
		int i1;
		float f7;

		BlockLightingInfo lightingInfo = new BlockLightingInfo();

		if(side==0)
		{
			//            if (RenderBlocks.getInstance().renderMinY <= 0.0D)
				//            {
			//                --y;
			//            }

			lightingInfo.aoBrightnessXYNN = block.getMixedBrightnessForBlock(world, x - 1, y, z);
			lightingInfo.aoBrightnessYZNN = block.getMixedBrightnessForBlock(world, x, y, z - 1);
			lightingInfo.aoBrightnessYZNP = block.getMixedBrightnessForBlock(world, x, y, z + 1);
			lightingInfo.aoBrightnessXYPN = block.getMixedBrightnessForBlock(world, x + 1, y, z);
			lightingInfo.aoLightValueScratchXYNN = world.getBlock(x - 1, y, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchYZNN = world.getBlock(x, y, z - 1).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchYZNP = world.getBlock(x, y, z + 1).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXYPN = world.getBlock(x + 1, y, z).getAmbientOcclusionLightValue();
			flag2 = world.getBlock(x + 1, y - 1, z).getCanBlockGrass();
			flag3 = world.getBlock(x - 1, y - 1, z).getCanBlockGrass();
			flag4 = world.getBlock(x, y - 1, z + 1).getCanBlockGrass();
			flag5 = world.getBlock(x, y - 1, z - 1).getCanBlockGrass();

			if (!flag5 && !flag3)
			{
				lightingInfo.aoLightValueScratchXYZNNN = lightingInfo.aoLightValueScratchXYNN;
				lightingInfo.aoBrightnessXYZNNN = lightingInfo.aoBrightnessXYNN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNNN = world.getBlock(x - 1, y, z - 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(world, x - 1, y, z - 1);
			}

			if (!flag4 && !flag3)
			{
				lightingInfo.aoLightValueScratchXYZNNP = lightingInfo.aoLightValueScratchXYNN;
				lightingInfo.aoBrightnessXYZNNP = lightingInfo.aoBrightnessXYNN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNNP = world.getBlock(x - 1, y, z + 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(world, x - 1, y, z + 1);
			}

			if (!flag5 && !flag2)
			{
				lightingInfo.aoLightValueScratchXYZPNN = lightingInfo.aoLightValueScratchXYPN;
				lightingInfo.aoBrightnessXYZPNN = lightingInfo.aoBrightnessXYPN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPNN = world.getBlock(x + 1, y, z - 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(world, x + 1, y, z - 1);
			}

			if (!flag4 && !flag2)
			{
				lightingInfo.aoLightValueScratchXYZPNP = lightingInfo.aoLightValueScratchXYPN;
				lightingInfo.aoBrightnessXYZPNP = lightingInfo.aoBrightnessXYPN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPNP = world.getBlock(x + 1, y, z + 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(world, x + 1, y, z + 1);
			}

			//            if (RenderBlocks.getInstance().renderMinY <= 0.0D)
			//            {
			//                ++y;
			//            }

			i1 = l;

			//            if (RenderBlocks.getInstance().renderMinY <= 0.0D || !world.getBlock(x, y - 1, z).isOpaqueCube())
			//            {
			i1 = block.getMixedBrightnessForBlock(world, x, y - 1, z);
			//            }

			f7 = world.getBlock(x, y - 1, z).getAmbientOcclusionLightValue();
			f3 = (lightingInfo.aoLightValueScratchXYZNNP + lightingInfo.aoLightValueScratchXYNN + lightingInfo.aoLightValueScratchYZNP + f7) / 4.0F;
			f6 = (lightingInfo.aoLightValueScratchYZNP + f7 + lightingInfo.aoLightValueScratchXYZPNP + lightingInfo.aoLightValueScratchXYPN) / 4.0F;
			f5 = (f7 + lightingInfo.aoLightValueScratchYZNN + lightingInfo.aoLightValueScratchXYPN + lightingInfo.aoLightValueScratchXYZPNN) / 4.0F;
			f4 = (lightingInfo.aoLightValueScratchXYNN + lightingInfo.aoLightValueScratchXYZNNN + f7 + lightingInfo.aoLightValueScratchYZNN) / 4.0F;
			lightingInfo.brightnessTopLeft = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYZNNP, lightingInfo.aoBrightnessXYNN, lightingInfo.aoBrightnessYZNP, i1);
			lightingInfo.brightnessTopRight = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessYZNP, lightingInfo.aoBrightnessXYZPNP, lightingInfo.aoBrightnessXYPN, i1);
			lightingInfo.brightnessBottomRight = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessYZNN, lightingInfo.aoBrightnessXYPN, lightingInfo.aoBrightnessXYZPNN, i1);
			lightingInfo.brightnessBottomLeft = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYNN, lightingInfo.aoBrightnessXYZNNN, lightingInfo.aoBrightnessYZNN, i1);

			lightingInfo.colorRedTopLeft = lightingInfo.colorRedBottomLeft = lightingInfo.colorRedBottomRight = lightingInfo.colorRedTopRight = 0.5F;
			lightingInfo.colorGreenTopLeft = lightingInfo.colorGreenBottomLeft = lightingInfo.colorGreenBottomRight = lightingInfo.colorGreenTopRight = 0.5F;
			lightingInfo.colorBlueTopLeft = lightingInfo.colorBlueBottomLeft = lightingInfo.colorBlueBottomRight = lightingInfo.colorBlueTopRight = 0.5F;

			lightingInfo.colorRedTopLeft *= f3;
			lightingInfo.colorGreenTopLeft *= f3;
			lightingInfo.colorBlueTopLeft *= f3;
			lightingInfo.colorRedBottomLeft *= f4;
			lightingInfo.colorGreenBottomLeft *= f4;
			lightingInfo.colorBlueBottomLeft *= f4;
			lightingInfo.colorRedBottomRight *= f5;
			lightingInfo.colorGreenBottomRight *= f5;
			lightingInfo.colorBlueBottomRight *= f5;
			lightingInfo.colorRedTopRight *= f6;
			lightingInfo.colorGreenTopRight *= f6;
			lightingInfo.colorBlueTopRight *= f6;
			flag = true;
		}

		if(side==1)
			//			if(lightingInfo.renderAllFaces || block.shouldSideBeRendered(world, x, y + 1, z, 1))
		{
			//			if (RenderBlocks.getInstance().renderMaxY >= 1.0D)
			//			{
			//				++y;
			//			}

			lightingInfo.aoBrightnessXYNP = block.getMixedBrightnessForBlock(world, x - 1, y, z);
			lightingInfo.aoBrightnessXYPP = block.getMixedBrightnessForBlock(world, x + 1, y, z);
			lightingInfo.aoBrightnessYZPN = block.getMixedBrightnessForBlock(world, x, y, z - 1);
			lightingInfo.aoBrightnessYZPP = block.getMixedBrightnessForBlock(world, x, y, z + 1);
			lightingInfo.aoLightValueScratchXYNP = world.getBlock(x - 1, y, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXYPP = world.getBlock(x + 1, y, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchYZPN = world.getBlock(x, y, z - 1).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchYZPP = world.getBlock(x, y, z + 1).getAmbientOcclusionLightValue();
			flag2 = world.getBlock(x + 1, y + 1, z).getCanBlockGrass();
			flag3 = world.getBlock(x - 1, y + 1, z).getCanBlockGrass();
			flag4 = world.getBlock(x, y + 1, z + 1).getCanBlockGrass();
			flag5 = world.getBlock(x, y + 1, z - 1).getCanBlockGrass();

			if (!flag5 && !flag3)
			{
				lightingInfo.aoLightValueScratchXYZNPN = lightingInfo.aoLightValueScratchXYNP;
				lightingInfo.aoBrightnessXYZNPN = lightingInfo.aoBrightnessXYNP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNPN = world.getBlock(x - 1, y, z - 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(world, x - 1, y, z - 1);
			}

			if (!flag5 && !flag2)
			{
				lightingInfo.aoLightValueScratchXYZPPN = lightingInfo.aoLightValueScratchXYPP;
				lightingInfo.aoBrightnessXYZPPN = lightingInfo.aoBrightnessXYPP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPPN = world.getBlock(x + 1, y, z - 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(world, x + 1, y, z - 1);
			}

			if (!flag4 && !flag3)
			{
				lightingInfo.aoLightValueScratchXYZNPP = lightingInfo.aoLightValueScratchXYNP;
				lightingInfo.aoBrightnessXYZNPP = lightingInfo.aoBrightnessXYNP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNPP = world.getBlock(x - 1, y, z + 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(world, x - 1, y, z + 1);
			}

			if (!flag4 && !flag2)
			{
				lightingInfo.aoLightValueScratchXYZPPP = lightingInfo.aoLightValueScratchXYPP;
				lightingInfo.aoBrightnessXYZPPP = lightingInfo.aoBrightnessXYPP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPPP = world.getBlock(x + 1, y, z + 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(world, x + 1, y, z + 1);
			}

			//			if (RenderBlocks.getInstance().renderMaxY >= 1.0D)
			//			{
			//				--y;
			//			}

			i1 = l;

			//			if (RenderBlocks.getInstance().renderMaxY >= 1.0D || !world.getBlock(x, y + 1, z).isOpaqueCube())
			//			{
			i1 = block.getMixedBrightnessForBlock(world, x, y + 1, z);
			//			}

			f7 = world.getBlock(x, y + 1, z).getAmbientOcclusionLightValue();
			f6 = (lightingInfo.aoLightValueScratchXYZNPP + lightingInfo.aoLightValueScratchXYNP + lightingInfo.aoLightValueScratchYZPP + f7) / 4.0F;
			f3 = (lightingInfo.aoLightValueScratchYZPP + f7 + lightingInfo.aoLightValueScratchXYZPPP + lightingInfo.aoLightValueScratchXYPP) / 4.0F;
			f4 = (f7 + lightingInfo.aoLightValueScratchYZPN + lightingInfo.aoLightValueScratchXYPP + lightingInfo.aoLightValueScratchXYZPPN) / 4.0F;
			f5 = (lightingInfo.aoLightValueScratchXYNP + lightingInfo.aoLightValueScratchXYZNPN + f7 + lightingInfo.aoLightValueScratchYZPN) / 4.0F;
			lightingInfo.brightnessTopRight = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYZNPP, lightingInfo.aoBrightnessXYNP, lightingInfo.aoBrightnessYZPP, i1);
			lightingInfo.brightnessTopLeft = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessYZPP, lightingInfo.aoBrightnessXYZPPP, lightingInfo.aoBrightnessXYPP, i1);
			lightingInfo.brightnessBottomLeft = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessYZPN, lightingInfo.aoBrightnessXYPP, lightingInfo.aoBrightnessXYZPPN, i1);
			lightingInfo.brightnessBottomRight = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYNP, lightingInfo.aoBrightnessXYZNPN, lightingInfo.aoBrightnessYZPN, i1);
			lightingInfo.colorRedTopLeft = lightingInfo.colorRedBottomLeft = lightingInfo.colorRedBottomRight = lightingInfo.colorRedTopRight = colR;
			lightingInfo.colorGreenTopLeft = lightingInfo.colorGreenBottomLeft = lightingInfo.colorGreenBottomRight = lightingInfo.colorGreenTopRight = colG;
			lightingInfo.colorBlueTopLeft = lightingInfo.colorBlueBottomLeft = lightingInfo.colorBlueBottomRight = lightingInfo.colorBlueTopRight = colB;
			lightingInfo.colorRedTopLeft *= f3;
			lightingInfo.colorGreenTopLeft *= f3;
			lightingInfo.colorBlueTopLeft *= f3;
			lightingInfo.colorRedBottomLeft *= f4;
			lightingInfo.colorGreenBottomLeft *= f4;
			lightingInfo.colorBlueBottomLeft *= f4;
			lightingInfo.colorRedBottomRight *= f5;
			lightingInfo.colorGreenBottomRight *= f5;
			lightingInfo.colorBlueBottomRight *= f5;
			lightingInfo.colorRedTopRight *= f6;
			lightingInfo.colorGreenTopRight *= f6;
			lightingInfo.colorBlueTopRight *= f6;
			flag = true;
		}

		float f8;
		float f9;
		float f10;
		float f11;
		int j1;
		int k1;
		int l1;
		int i2;
		IIcon iicon;

		if(side==2)
			//			if (lightingInfo.renderAllFaces || block.shouldSideBeRendered(world, x, y, z - 1, 2))
		{
			//			if (RenderBlocks.getInstance().renderMinZ <= 0.0D)
			//			{
			//				--z;
			//			}

			lightingInfo.aoLightValueScratchXZNN = world.getBlock(x - 1, y, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchYZNN = world.getBlock(x, y - 1, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchYZPN = world.getBlock(x, y + 1, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXZPN = world.getBlock(x + 1, y, z).getAmbientOcclusionLightValue();
			lightingInfo.aoBrightnessXZNN = block.getMixedBrightnessForBlock(world, x - 1, y, z);
			lightingInfo.aoBrightnessYZNN = block.getMixedBrightnessForBlock(world, x, y - 1, z);
			lightingInfo.aoBrightnessYZPN = block.getMixedBrightnessForBlock(world, x, y + 1, z);
			lightingInfo.aoBrightnessXZPN = block.getMixedBrightnessForBlock(world, x + 1, y, z);
			flag2 = world.getBlock(x + 1, y, z - 1).getCanBlockGrass();
			flag3 = world.getBlock(x - 1, y, z - 1).getCanBlockGrass();
			flag4 = world.getBlock(x, y + 1, z - 1).getCanBlockGrass();
			flag5 = world.getBlock(x, y - 1, z - 1).getCanBlockGrass();

			if (!flag3 && !flag5)
			{
				lightingInfo.aoLightValueScratchXYZNNN = lightingInfo.aoLightValueScratchXZNN;
				lightingInfo.aoBrightnessXYZNNN = lightingInfo.aoBrightnessXZNN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNNN = world.getBlock(x - 1, y - 1, z).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(world, x - 1, y - 1, z);
			}

			if (!flag3 && !flag4)
			{
				lightingInfo.aoLightValueScratchXYZNPN = lightingInfo.aoLightValueScratchXZNN;
				lightingInfo.aoBrightnessXYZNPN = lightingInfo.aoBrightnessXZNN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNPN = world.getBlock(x - 1, y + 1, z).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(world, x - 1, y + 1, z);
			}

			if (!flag2 && !flag5)
			{
				lightingInfo.aoLightValueScratchXYZPNN = lightingInfo.aoLightValueScratchXZPN;
				lightingInfo.aoBrightnessXYZPNN = lightingInfo.aoBrightnessXZPN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPNN = world.getBlock(x + 1, y - 1, z).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(world, x + 1, y - 1, z);
			}

			if (!flag2 && !flag4)
			{
				lightingInfo.aoLightValueScratchXYZPPN = lightingInfo.aoLightValueScratchXZPN;
				lightingInfo.aoBrightnessXYZPPN = lightingInfo.aoBrightnessXZPN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPPN = world.getBlock(x + 1, y + 1, z).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(world, x + 1, y + 1, z);
			}

			//			if (RenderBlocks.getInstance().renderMinZ <= 0.0D)
			//			{
			//				++z;
			//			}

			i1 = l;

			//			if (RenderBlocks.getInstance().renderMinZ <= 0.0D || !world.getBlock(x, y, z - 1).isOpaqueCube())
			//			{
			i1 = block.getMixedBrightnessForBlock(world, x, y, z - 1);
			//			}

			f7 = world.getBlock(x, y, z - 1).getAmbientOcclusionLightValue();
			f8 = (lightingInfo.aoLightValueScratchXZNN + lightingInfo.aoLightValueScratchXYZNPN + f7 + lightingInfo.aoLightValueScratchYZPN) / 4.0F;
			f9 = (f7 + lightingInfo.aoLightValueScratchYZPN + lightingInfo.aoLightValueScratchXZPN + lightingInfo.aoLightValueScratchXYZPPN) / 4.0F;
			f10 = (lightingInfo.aoLightValueScratchYZNN + f7 + lightingInfo.aoLightValueScratchXYZPNN + lightingInfo.aoLightValueScratchXZPN) / 4.0F;
			f11 = (lightingInfo.aoLightValueScratchXYZNNN + lightingInfo.aoLightValueScratchXZNN + lightingInfo.aoLightValueScratchYZNN + f7) / 4.0F;
			f3 = (float)((double)f8 * RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMinX) + (double)f9 * RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMinX + (double)f10 * (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMinX + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMinX));
			f4 = (float)((double)f8 * RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMaxX) + (double)f9 * RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMaxX + (double)f10 * (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMaxX + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMaxX));
			f5 = (float)((double)f8 * RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMaxX) + (double)f9 * RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMaxX + (double)f10 * (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMaxX + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMaxX));
			f6 = (float)((double)f8 * RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMinX) + (double)f9 * RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMinX + (double)f10 * (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMinX + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMinX));
			j1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXZNN, lightingInfo.aoBrightnessXYZNPN, lightingInfo.aoBrightnessYZPN, i1);
			k1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessYZPN, lightingInfo.aoBrightnessXZPN, lightingInfo.aoBrightnessXYZPPN, i1);
			l1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessYZNN, lightingInfo.aoBrightnessXYZPNN, lightingInfo.aoBrightnessXZPN, i1);
			i2 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYZNNN, lightingInfo.aoBrightnessXZNN, lightingInfo.aoBrightnessYZNN, i1);
			lightingInfo.brightnessTopLeft = lightingInfo.mixAoBrightness(j1, k1, l1, i2, RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMinX), RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMinX, (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMinX, (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMinX));
			lightingInfo.brightnessBottomLeft = lightingInfo.mixAoBrightness(j1, k1, l1, i2, RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMaxX), RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMaxX, (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMaxX, (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMaxX));
			lightingInfo.brightnessBottomRight = lightingInfo.mixAoBrightness(j1, k1, l1, i2, RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMaxX), RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMaxX, (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMaxX, (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMaxX));
			lightingInfo.brightnessTopRight = lightingInfo.mixAoBrightness(j1, k1, l1, i2, RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMinX), RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMinX, (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMinX, (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMinX));

			lightingInfo.colorRedTopLeft = lightingInfo.colorRedBottomLeft = lightingInfo.colorRedBottomRight = lightingInfo.colorRedTopRight = 0.8F;
			lightingInfo.colorGreenTopLeft = lightingInfo.colorGreenBottomLeft = lightingInfo.colorGreenBottomRight = lightingInfo.colorGreenTopRight = 0.8F;
			lightingInfo.colorBlueTopLeft = lightingInfo.colorBlueBottomLeft = lightingInfo.colorBlueBottomRight = lightingInfo.colorBlueTopRight = 0.8F;

			lightingInfo.colorRedTopLeft *= f3;
			lightingInfo.colorGreenTopLeft *= f3;
			lightingInfo.colorBlueTopLeft *= f3;
			lightingInfo.colorRedBottomLeft *= f4;
			lightingInfo.colorGreenBottomLeft *= f4;
			lightingInfo.colorBlueBottomLeft *= f4;
			lightingInfo.colorRedBottomRight *= f5;
			lightingInfo.colorGreenBottomRight *= f5;
			lightingInfo.colorBlueBottomRight *= f5;
			lightingInfo.colorRedTopRight *= f6;
			lightingInfo.colorGreenTopRight *= f6;
			lightingInfo.colorBlueTopRight *= f6;
			flag = true;
		}

		if(side==3)
			//		if (lightingInfo.renderAllFaces || block.shouldSideBeRendered(world, x, y, z + 1, 3))
		{
			if (RenderBlocks.getInstance().renderMaxZ >= 1.0D)
			{
				++z;
			}

			lightingInfo.aoLightValueScratchXZNP = world.getBlock(x - 1, y, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXZPP = world.getBlock(x + 1, y, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchYZNP = world.getBlock(x, y - 1, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchYZPP = world.getBlock(x, y + 1, z).getAmbientOcclusionLightValue();
			lightingInfo.aoBrightnessXZNP = block.getMixedBrightnessForBlock(world, x - 1, y, z);
			lightingInfo.aoBrightnessXZPP = block.getMixedBrightnessForBlock(world, x + 1, y, z);
			lightingInfo.aoBrightnessYZNP = block.getMixedBrightnessForBlock(world, x, y - 1, z);
			lightingInfo.aoBrightnessYZPP = block.getMixedBrightnessForBlock(world, x, y + 1, z);
			flag2 = world.getBlock(x + 1, y, z + 1).getCanBlockGrass();
			flag3 = world.getBlock(x - 1, y, z + 1).getCanBlockGrass();
			flag4 = world.getBlock(x, y + 1, z + 1).getCanBlockGrass();
			flag5 = world.getBlock(x, y - 1, z + 1).getCanBlockGrass();

			if (!flag3 && !flag5)
			{
				lightingInfo.aoLightValueScratchXYZNNP = lightingInfo.aoLightValueScratchXZNP;
				lightingInfo.aoBrightnessXYZNNP = lightingInfo.aoBrightnessXZNP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNNP = world.getBlock(x - 1, y - 1, z).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(world, x - 1, y - 1, z);
			}

			if (!flag3 && !flag4)
			{
				lightingInfo.aoLightValueScratchXYZNPP = lightingInfo.aoLightValueScratchXZNP;
				lightingInfo.aoBrightnessXYZNPP = lightingInfo.aoBrightnessXZNP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNPP = world.getBlock(x - 1, y + 1, z).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(world, x - 1, y + 1, z);
			}

			if (!flag2 && !flag5)
			{
				lightingInfo.aoLightValueScratchXYZPNP = lightingInfo.aoLightValueScratchXZPP;
				lightingInfo.aoBrightnessXYZPNP = lightingInfo.aoBrightnessXZPP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPNP = world.getBlock(x + 1, y - 1, z).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(world, x + 1, y - 1, z);
			}

			if (!flag2 && !flag4)
			{
				lightingInfo.aoLightValueScratchXYZPPP = lightingInfo.aoLightValueScratchXZPP;
				lightingInfo.aoBrightnessXYZPPP = lightingInfo.aoBrightnessXZPP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPPP = world.getBlock(x + 1, y + 1, z).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(world, x + 1, y + 1, z);
			}

			if (RenderBlocks.getInstance().renderMaxZ >= 1.0D)
			{
				--z;
			}

			i1 = l;

			if (RenderBlocks.getInstance().renderMaxZ >= 1.0D || !world.getBlock(x, y, z + 1).isOpaqueCube())
			{
				i1 = block.getMixedBrightnessForBlock(world, x, y, z + 1);
			}

			f7 = world.getBlock(x, y, z + 1).getAmbientOcclusionLightValue();
			f8 = (lightingInfo.aoLightValueScratchXZNP + lightingInfo.aoLightValueScratchXYZNPP + f7 + lightingInfo.aoLightValueScratchYZPP) / 4.0F;
			f9 = (f7 + lightingInfo.aoLightValueScratchYZPP + lightingInfo.aoLightValueScratchXZPP + lightingInfo.aoLightValueScratchXYZPPP) / 4.0F;
			f10 = (lightingInfo.aoLightValueScratchYZNP + f7 + lightingInfo.aoLightValueScratchXYZPNP + lightingInfo.aoLightValueScratchXZPP) / 4.0F;
			f11 = (lightingInfo.aoLightValueScratchXYZNNP + lightingInfo.aoLightValueScratchXZNP + lightingInfo.aoLightValueScratchYZNP + f7) / 4.0F;
			f3 = (float)((double)f8 * RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMinX) + (double)f9 * RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMinX + (double)f10 * (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMinX + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMinX));
			f4 = (float)((double)f8 * RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMinX) + (double)f9 * RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMinX + (double)f10 * (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMinX + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMinX));
			f5 = (float)((double)f8 * RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMaxX) + (double)f9 * RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMaxX + (double)f10 * (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMaxX + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMaxX));
			f6 = (float)((double)f8 * RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMaxX) + (double)f9 * RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMaxX + (double)f10 * (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMaxX + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMaxX));
			j1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXZNP, lightingInfo.aoBrightnessXYZNPP, lightingInfo.aoBrightnessYZPP, i1);
			k1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessYZPP, lightingInfo.aoBrightnessXZPP, lightingInfo.aoBrightnessXYZPPP, i1);
			l1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessYZNP, lightingInfo.aoBrightnessXYZPNP, lightingInfo.aoBrightnessXZPP, i1);
			i2 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYZNNP, lightingInfo.aoBrightnessXZNP, lightingInfo.aoBrightnessYZNP, i1);
			lightingInfo.brightnessTopLeft = lightingInfo.mixAoBrightness(j1, i2, l1, k1, RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMinX), (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMinX), (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMinX, RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMinX);
			lightingInfo.brightnessBottomLeft = lightingInfo.mixAoBrightness(j1, i2, l1, k1, RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMinX), (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMinX), (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMinX, RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMinX);
			lightingInfo.brightnessBottomRight = lightingInfo.mixAoBrightness(j1, i2, l1, k1, RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMaxX), (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMaxX), (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMaxX, RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMaxX);
			lightingInfo.brightnessTopRight = lightingInfo.mixAoBrightness(j1, i2, l1, k1, RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMaxX), (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMaxX), (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMaxX, RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMaxX);

			lightingInfo.colorRedTopLeft = lightingInfo.colorRedBottomLeft = lightingInfo.colorRedBottomRight = lightingInfo.colorRedTopRight = 0.8F;
			lightingInfo.colorGreenTopLeft = lightingInfo.colorGreenBottomLeft = lightingInfo.colorGreenBottomRight = lightingInfo.colorGreenTopRight = 0.8F;
			lightingInfo.colorBlueTopLeft = lightingInfo.colorBlueBottomLeft = lightingInfo.colorBlueBottomRight = lightingInfo.colorBlueTopRight = 0.8F;

			lightingInfo.colorRedTopLeft *= f3;
			lightingInfo.colorGreenTopLeft *= f3;
			lightingInfo.colorBlueTopLeft *= f3;
			lightingInfo.colorRedBottomLeft *= f4;
			lightingInfo.colorGreenBottomLeft *= f4;
			lightingInfo.colorBlueBottomLeft *= f4;
			lightingInfo.colorRedBottomRight *= f5;
			lightingInfo.colorGreenBottomRight *= f5;
			lightingInfo.colorBlueBottomRight *= f5;
			lightingInfo.colorRedTopRight *= f6;
			lightingInfo.colorGreenTopRight *= f6;
			lightingInfo.colorBlueTopRight *= f6;
			flag = true;
		}

		if(side==4)
			//		if (lightingInfo.renderAllFaces || block.shouldSideBeRendered(world, x - 1, y, z, 4))
		{
			if (RenderBlocks.getInstance().renderMinX <= 0.0D)
			{
				--x;
			}

			lightingInfo.aoLightValueScratchXYNN = world.getBlock(x, y - 1, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXZNN = world.getBlock(x, y, z - 1).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXZNP = world.getBlock(x, y, z + 1).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXYNP = world.getBlock(x, y + 1, z).getAmbientOcclusionLightValue();
			lightingInfo.aoBrightnessXYNN = block.getMixedBrightnessForBlock(world, x, y - 1, z);
			lightingInfo.aoBrightnessXZNN = block.getMixedBrightnessForBlock(world, x, y, z - 1);
			lightingInfo.aoBrightnessXZNP = block.getMixedBrightnessForBlock(world, x, y, z + 1);
			lightingInfo.aoBrightnessXYNP = block.getMixedBrightnessForBlock(world, x, y + 1, z);
			flag2 = world.getBlock(x - 1, y + 1, z).getCanBlockGrass();
			flag3 = world.getBlock(x - 1, y - 1, z).getCanBlockGrass();
			flag4 = world.getBlock(x - 1, y, z - 1).getCanBlockGrass();
			flag5 = world.getBlock(x - 1, y, z + 1).getCanBlockGrass();

			if (!flag4 && !flag3)
			{
				lightingInfo.aoLightValueScratchXYZNNN = lightingInfo.aoLightValueScratchXZNN;
				lightingInfo.aoBrightnessXYZNNN = lightingInfo.aoBrightnessXZNN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNNN = world.getBlock(x, y - 1, z - 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(world, x, y - 1, z - 1);
			}

			if (!flag5 && !flag3)
			{
				lightingInfo.aoLightValueScratchXYZNNP = lightingInfo.aoLightValueScratchXZNP;
				lightingInfo.aoBrightnessXYZNNP = lightingInfo.aoBrightnessXZNP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNNP = world.getBlock(x, y - 1, z + 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(world, x, y - 1, z + 1);
			}

			if (!flag4 && !flag2)
			{
				lightingInfo.aoLightValueScratchXYZNPN = lightingInfo.aoLightValueScratchXZNN;
				lightingInfo.aoBrightnessXYZNPN = lightingInfo.aoBrightnessXZNN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNPN = world.getBlock(x, y + 1, z - 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(world, x, y + 1, z - 1);
			}

			if (!flag5 && !flag2)
			{
				lightingInfo.aoLightValueScratchXYZNPP = lightingInfo.aoLightValueScratchXZNP;
				lightingInfo.aoBrightnessXYZNPP = lightingInfo.aoBrightnessXZNP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZNPP = world.getBlock(x, y + 1, z + 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(world, x, y + 1, z + 1);
			}

			if (RenderBlocks.getInstance().renderMinX <= 0.0D)
			{
				++x;
			}

			i1 = l;

			if (RenderBlocks.getInstance().renderMinX <= 0.0D || !world.getBlock(x - 1, y, z).isOpaqueCube())
			{
				i1 = block.getMixedBrightnessForBlock(world, x - 1, y, z);
			}

			f7 = world.getBlock(x - 1, y, z).getAmbientOcclusionLightValue();
			f8 = (lightingInfo.aoLightValueScratchXYNN + lightingInfo.aoLightValueScratchXYZNNP + f7 + lightingInfo.aoLightValueScratchXZNP) / 4.0F;
			f9 = (f7 + lightingInfo.aoLightValueScratchXZNP + lightingInfo.aoLightValueScratchXYNP + lightingInfo.aoLightValueScratchXYZNPP) / 4.0F;
			f10 = (lightingInfo.aoLightValueScratchXZNN + f7 + lightingInfo.aoLightValueScratchXYZNPN + lightingInfo.aoLightValueScratchXYNP) / 4.0F;
			f11 = (lightingInfo.aoLightValueScratchXYZNNN + lightingInfo.aoLightValueScratchXYNN + lightingInfo.aoLightValueScratchXZNN + f7) / 4.0F;
			f3 = (float)((double)f9 * RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMaxZ + (double)f10 * RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMaxZ) + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMaxZ) + (double)f8 * (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMaxZ);
			f4 = (float)((double)f9 * RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMinZ + (double)f10 * RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMinZ) + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMinZ) + (double)f8 * (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMinZ);
			f5 = (float)((double)f9 * RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMinZ + (double)f10 * RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMinZ) + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMinZ) + (double)f8 * (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMinZ);
			f6 = (float)((double)f9 * RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMaxZ + (double)f10 * RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMaxZ) + (double)f11 * (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMaxZ) + (double)f8 * (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMaxZ);
			j1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYNN, lightingInfo.aoBrightnessXYZNNP, lightingInfo.aoBrightnessXZNP, i1);
			k1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXZNP, lightingInfo.aoBrightnessXYNP, lightingInfo.aoBrightnessXYZNPP, i1);
			l1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXZNN, lightingInfo.aoBrightnessXYZNPN, lightingInfo.aoBrightnessXYNP, i1);
			i2 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYZNNN, lightingInfo.aoBrightnessXYNN, lightingInfo.aoBrightnessXZNN, i1);
			lightingInfo.brightnessTopLeft = lightingInfo.mixAoBrightness(k1, l1, i2, j1, RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMaxZ, RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMaxZ), (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMaxZ), (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMaxZ);
			lightingInfo.brightnessBottomLeft = lightingInfo.mixAoBrightness(k1, l1, i2, j1, RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMinZ, RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMinZ), (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMinZ), (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMinZ);
			lightingInfo.brightnessBottomRight = lightingInfo.mixAoBrightness(k1, l1, i2, j1, RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMinZ, RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMinZ), (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMinZ), (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMinZ);
			lightingInfo.brightnessTopRight = lightingInfo.mixAoBrightness(k1, l1, i2, j1, RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMaxZ, RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMaxZ), (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMaxZ), (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMaxZ);

			lightingInfo.colorRedTopLeft = lightingInfo.colorRedBottomLeft = lightingInfo.colorRedBottomRight = lightingInfo.colorRedTopRight = 0.6F;
			lightingInfo.colorGreenTopLeft = lightingInfo.colorGreenBottomLeft = lightingInfo.colorGreenBottomRight = lightingInfo.colorGreenTopRight = 0.6F;
			lightingInfo.colorBlueTopLeft = lightingInfo.colorBlueBottomLeft = lightingInfo.colorBlueBottomRight = lightingInfo.colorBlueTopRight = 0.6F;

			lightingInfo.colorRedTopLeft *= f3;
			lightingInfo.colorGreenTopLeft *= f3;
			lightingInfo.colorBlueTopLeft *= f3;
			lightingInfo.colorRedBottomLeft *= f4;
			lightingInfo.colorGreenBottomLeft *= f4;
			lightingInfo.colorBlueBottomLeft *= f4;
			lightingInfo.colorRedBottomRight *= f5;
			lightingInfo.colorGreenBottomRight *= f5;
			lightingInfo.colorBlueBottomRight *= f5;
			lightingInfo.colorRedTopRight *= f6;
			lightingInfo.colorGreenTopRight *= f6;
			lightingInfo.colorBlueTopRight *= f6;
			flag = true;
		}

		if(side==5)
			//		if (lightingInfo.renderAllFaces || block.shouldSideBeRendered(world, x + 1, y, z, 5))
		{
			if (RenderBlocks.getInstance().renderMaxX >= 1.0D)
			{
				++x;
			}

			lightingInfo.aoLightValueScratchXYPN = world.getBlock(x, y - 1, z).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXZPN = world.getBlock(x, y, z - 1).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXZPP = world.getBlock(x, y, z + 1).getAmbientOcclusionLightValue();
			lightingInfo.aoLightValueScratchXYPP = world.getBlock(x, y + 1, z).getAmbientOcclusionLightValue();
			lightingInfo.aoBrightnessXYPN = block.getMixedBrightnessForBlock(world, x, y - 1, z);
			lightingInfo.aoBrightnessXZPN = block.getMixedBrightnessForBlock(world, x, y, z - 1);
			lightingInfo.aoBrightnessXZPP = block.getMixedBrightnessForBlock(world, x, y, z + 1);
			lightingInfo.aoBrightnessXYPP = block.getMixedBrightnessForBlock(world, x, y + 1, z);
			flag2 = world.getBlock(x + 1, y + 1, z).getCanBlockGrass();
			flag3 = world.getBlock(x + 1, y - 1, z).getCanBlockGrass();
			flag4 = world.getBlock(x + 1, y, z + 1).getCanBlockGrass();
			flag5 = world.getBlock(x + 1, y, z - 1).getCanBlockGrass();

			if (!flag3 && !flag5)
			{
				lightingInfo.aoLightValueScratchXYZPNN = lightingInfo.aoLightValueScratchXZPN;
				lightingInfo.aoBrightnessXYZPNN = lightingInfo.aoBrightnessXZPN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPNN = world.getBlock(x, y - 1, z - 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(world, x, y - 1, z - 1);
			}

			if (!flag3 && !flag4)
			{
				lightingInfo.aoLightValueScratchXYZPNP = lightingInfo.aoLightValueScratchXZPP;
				lightingInfo.aoBrightnessXYZPNP = lightingInfo.aoBrightnessXZPP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPNP = world.getBlock(x, y - 1, z + 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(world, x, y - 1, z + 1);
			}

			if (!flag2 && !flag5)
			{
				lightingInfo.aoLightValueScratchXYZPPN = lightingInfo.aoLightValueScratchXZPN;
				lightingInfo.aoBrightnessXYZPPN = lightingInfo.aoBrightnessXZPN;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPPN = world.getBlock(x, y + 1, z - 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(world, x, y + 1, z - 1);
			}

			if (!flag2 && !flag4)
			{
				lightingInfo.aoLightValueScratchXYZPPP = lightingInfo.aoLightValueScratchXZPP;
				lightingInfo.aoBrightnessXYZPPP = lightingInfo.aoBrightnessXZPP;
			}
			else
			{
				lightingInfo.aoLightValueScratchXYZPPP = world.getBlock(x, y + 1, z + 1).getAmbientOcclusionLightValue();
				lightingInfo.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(world, x, y + 1, z + 1);
			}

			if (RenderBlocks.getInstance().renderMaxX >= 1.0D)
			{
				--x;
			}

			i1 = l;

			if (RenderBlocks.getInstance().renderMaxX >= 1.0D || !world.getBlock(x + 1, y, z).isOpaqueCube())
			{
				i1 = block.getMixedBrightnessForBlock(world, x + 1, y, z);
			}

			f7 = world.getBlock(x + 1, y, z).getAmbientOcclusionLightValue();
			f8 = (lightingInfo.aoLightValueScratchXYPN + lightingInfo.aoLightValueScratchXYZPNP + f7 + lightingInfo.aoLightValueScratchXZPP) / 4.0F;
			f9 = (lightingInfo.aoLightValueScratchXYZPNN + lightingInfo.aoLightValueScratchXYPN + lightingInfo.aoLightValueScratchXZPN + f7) / 4.0F;
			f10 = (lightingInfo.aoLightValueScratchXZPN + f7 + lightingInfo.aoLightValueScratchXYZPPN + lightingInfo.aoLightValueScratchXYPP) / 4.0F;
			f11 = (f7 + lightingInfo.aoLightValueScratchXZPP + lightingInfo.aoLightValueScratchXYPP + lightingInfo.aoLightValueScratchXYZPPP) / 4.0F;
			f3 = (float)((double)f8 * (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMaxZ + (double)f9 * (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMaxZ) + (double)f10 * RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMaxZ) + (double)f11 * RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMaxZ);
			f4 = (float)((double)f8 * (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMinZ + (double)f9 * (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMinZ) + (double)f10 * RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMinZ) + (double)f11 * RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMinZ);
			f5 = (float)((double)f8 * (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMinZ + (double)f9 * (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMinZ) + (double)f10 * RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMinZ) + (double)f11 * RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMinZ);
			f6 = (float)((double)f8 * (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMaxZ + (double)f9 * (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMaxZ) + (double)f10 * RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMaxZ) + (double)f11 * RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMaxZ);
			j1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYPN, lightingInfo.aoBrightnessXYZPNP, lightingInfo.aoBrightnessXZPP, i1);
			k1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXZPP, lightingInfo.aoBrightnessXYPP, lightingInfo.aoBrightnessXYZPPP, i1);
			l1 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXZPN, lightingInfo.aoBrightnessXYZPPN, lightingInfo.aoBrightnessXYPP, i1);
			i2 = lightingInfo.getAoBrightness(lightingInfo.aoBrightnessXYZPNN, lightingInfo.aoBrightnessXYPN, lightingInfo.aoBrightnessXZPN, i1);
			lightingInfo.brightnessTopLeft = lightingInfo.mixAoBrightness(j1, i2, l1, k1, (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMaxZ, (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMaxZ), RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMaxZ), RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMaxZ);
			lightingInfo.brightnessBottomLeft = lightingInfo.mixAoBrightness(j1, i2, l1, k1, (1.0D - RenderBlocks.getInstance().renderMinY) * RenderBlocks.getInstance().renderMinZ, (1.0D - RenderBlocks.getInstance().renderMinY) * (1.0D - RenderBlocks.getInstance().renderMinZ), RenderBlocks.getInstance().renderMinY * (1.0D - RenderBlocks.getInstance().renderMinZ), RenderBlocks.getInstance().renderMinY * RenderBlocks.getInstance().renderMinZ);
			lightingInfo.brightnessBottomRight = lightingInfo.mixAoBrightness(j1, i2, l1, k1, (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMinZ, (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMinZ), RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMinZ), RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMinZ);
			lightingInfo.brightnessTopRight = lightingInfo.mixAoBrightness(j1, i2, l1, k1, (1.0D - RenderBlocks.getInstance().renderMaxY) * RenderBlocks.getInstance().renderMaxZ, (1.0D - RenderBlocks.getInstance().renderMaxY) * (1.0D - RenderBlocks.getInstance().renderMaxZ), RenderBlocks.getInstance().renderMaxY * (1.0D - RenderBlocks.getInstance().renderMaxZ), RenderBlocks.getInstance().renderMaxY * RenderBlocks.getInstance().renderMaxZ);

			lightingInfo.colorRedTopLeft = lightingInfo.colorRedBottomLeft = lightingInfo.colorRedBottomRight = lightingInfo.colorRedTopRight = 0.6F;
			lightingInfo.colorGreenTopLeft = lightingInfo.colorGreenBottomLeft = lightingInfo.colorGreenBottomRight = lightingInfo.colorGreenTopRight = 0.6F;
			lightingInfo.colorBlueTopLeft = lightingInfo.colorBlueBottomLeft = lightingInfo.colorBlueBottomRight = lightingInfo.colorBlueTopRight = 0.6F;

			lightingInfo.colorRedTopLeft *= f3;
			lightingInfo.colorGreenTopLeft *= f3;
			lightingInfo.colorBlueTopLeft *= f3;
			lightingInfo.colorRedBottomLeft *= f4;
			lightingInfo.colorGreenBottomLeft *= f4;
			lightingInfo.colorBlueBottomLeft *= f4;
			lightingInfo.colorRedBottomRight *= f5;
			lightingInfo.colorGreenBottomRight *= f5;
			lightingInfo.colorBlueBottomRight *= f5;
			lightingInfo.colorRedTopRight *= f6;
			lightingInfo.colorGreenTopRight *= f6;
			lightingInfo.colorBlueTopRight *= f6;
			flag = true;
		}
		return lightingInfo;
	}
}