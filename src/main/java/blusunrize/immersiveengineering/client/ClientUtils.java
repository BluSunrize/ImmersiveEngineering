package blusunrize.immersiveengineering.client;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
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
}