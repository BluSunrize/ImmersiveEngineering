package blusunrize.immersiveengineering.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.obj.Face;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.Vertex;
import net.minecraftforge.client.model.obj.WavefrontObject;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.render.TileRenderIE;
import blusunrize.immersiveengineering.common.util.IESound;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import codechicken.lib.gui.GuiDraw;

public class ClientUtils
{
	// MOD SPECIFIC METHODS
	public static void renderAttachedConnections(TileEntity tile)
	{
		if(tile.getWorldObj()!=null && tile instanceof IImmersiveConnectable)
		{
			List<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(tile.getWorldObj(), Utils.toCC(tile));
			if(outputs!=null)
			{
				Iterator<ImmersiveNetHandler.Connection> itCon = outputs.iterator();
				while(itCon.hasNext())
				{
					ImmersiveNetHandler.Connection con = itCon.next();
					TileEntity tileEnd = tile.getWorldObj().getTileEntity(con.end.posX,con.end.posY,con.end.posZ);
					if(tileEnd instanceof IImmersiveConnectable)
						drawConnection(con, (IImmersiveConnectable)tile, Utils.toIIC(tileEnd, tile.getWorldObj()), con.cableType.getIcon(con));
				}
			}
		}
	}

	public static void drawConnection(ImmersiveNetHandler.Connection connection, IImmersiveConnectable start, IImmersiveConnectable end, IIcon icon)
	{
		if(connection==null || start==null || end==null)
			return;
		int col = connection.cableType.getColour(connection);
		double r = connection.cableType.getRenderDiameter()/2;
		drawConnection(connection, start, end, col,255, r, icon);
	}
	public static void drawConnection(ImmersiveNetHandler.Connection connection, IImmersiveConnectable start, IImmersiveConnectable end, int colour,int alpha, double radius, IIcon icon)
	{
		if(connection==null || start==null || end==null || connection.end==null || connection.start==null)
			return;
		Vec3 startOffset = start.getConnectionOffset(connection);
		Vec3 endOffset = end.getConnectionOffset(connection);
		if(startOffset==null)
			startOffset=Vec3.createVectorHelper(.5,.5,.5);
		if(endOffset==null)
			endOffset=Vec3.createVectorHelper(.5,.5,.5);
		double dx = (connection.end.posX+endOffset.xCoord)-(connection.start.posX+startOffset.xCoord);
		double dy = (connection.end.posY+endOffset.yCoord)-(connection.start.posY+startOffset.yCoord);
		double dz = (connection.end.posZ+endOffset.zCoord)-(connection.start.posZ+startOffset.zCoord);
		double dw = Math.sqrt(dx*dx + dz*dz);
		double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
		World world = ((TileEntity)start).getWorldObj();
		Tessellator tes = tes();

		double rmodx = dz/dw;
		double rmodz = dx/dw;

		Vec3[] vertex = connection.getSubVertices(world);
		Vec3 initPos = Vec3.createVectorHelper(connection.start.posX+startOffset.xCoord, connection.start.posY+startOffset.yCoord, connection.start.posZ+startOffset.zCoord);

		double uMin = icon.getMinU();
		double uMax = icon.getMaxU();
		double vMin = icon.getMinV();
		double vMax = icon.getMaxV();
		double uD = uMax-uMin;
		boolean vertical = connection.end.posX==connection.start.posX && connection.end.posZ==connection.start.posZ;
		boolean b = (dx<0&&dz<=0)||(dz<0&&dx<=0)||(dz<0&&dx>0);
		if(vertical)
		{
			//			double uShift = Math.abs(dy)/ * uD;
			tes.addTranslation((float)initPos.xCoord,(float)initPos.yCoord,(float)initPos.zCoord);
			tes.setColorRGBA_I(colour, alpha);
			tes.setBrightness(calcBrightness(world, connection.start.posX-radius,connection.start.posY,connection.start.posZ));
//			tes.addVertexWithUV(0-radius, 0, 0, b?uMax-uShift:uMin,vMin);
			tes.addVertexWithUV(0-radius, 0, 0, uMin,vMin);
			tes.setBrightness(calcBrightness(world, connection.start.posX-radius,connection.start.posY+dy,connection.start.posZ));
//			tes.addVertexWithUV(dx-radius, dy, dz, b?uMin:uMin+uShift,vMin);
			tes.addVertexWithUV(dx-radius, dy, dz, uMax,vMin);
			tes.setBrightness(calcBrightness(world, connection.start.posX+radius,connection.start.posY+dy,connection.start.posZ));
//			tes.addVertexWithUV(dx+radius, dy, dz, b?uMin:uMin+uShift,vMax);
			tes.addVertexWithUV(dx+radius, dy, dz, uMax,vMax);
			tes.setBrightness(calcBrightness(world, connection.start.posX+radius,connection.start.posY,connection.start.posZ));
//			tes.addVertexWithUV(0+radius, 0, 0, b?uMax-uShift:uMin,vMax);
			tes.addVertexWithUV(0+radius, 0, 0, uMin,vMax);

			tes.setBrightness(calcBrightness(world, connection.start.posX-radius,connection.start.posY+dy,connection.start.posZ));
//			tes.addVertexWithUV(dx-radius, dy, dz, b?uMin:uMin+uShift,vMin);
			tes.addVertexWithUV(dx-radius, dy, dz, uMax,vMin);
			tes.setBrightness(calcBrightness(world, connection.start.posX-radius,connection.start.posY,connection.start.posZ));
//			tes.addVertexWithUV(0-radius, 0, 0, b?uMax-uShift:uMin,vMin);
			tes.addVertexWithUV(0-radius, 0, 0,uMin,vMin);
			tes.setBrightness(calcBrightness(world, connection.start.posX+radius,connection.start.posY,connection.start.posZ));
//			tes.addVertexWithUV(0+radius, 0, 0, b?uMax-uShift:uMin,vMax);
			tes.addVertexWithUV(0+radius, 0, 0, uMin,vMax);
			tes.setBrightness(calcBrightness(world, connection.start.posX+radius,connection.start.posY+dy,connection.start.posZ));
//			tes.addVertexWithUV(dx+radius, dy, dz, b?uMin:uMin+uShift,vMax);
			tes.addVertexWithUV(dx+radius, dy, dz, uMax,vMax);


			tes.setColorRGBA_I(colour, alpha);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY,connection.start.posZ-radius));
//			tes.addVertexWithUV(0, 0, 0-radius, b?uMax-uShift:uMin,vMin);
			tes.addVertexWithUV(0, 0, 0-radius, uMin,vMin);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY+dy,connection.start.posZ-radius));
//			tes.addVertexWithUV(dx, dy, dz-radius, b?uMin:uMin+uShift,vMin);
			tes.addVertexWithUV(dx, dy, dz-radius, uMax,vMin);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY+dy,connection.start.posZ+radius));
//			tes.addVertexWithUV(dx, dy, dz+radius, b?uMin:uMin+uShift,vMax);
			tes.addVertexWithUV(dx, dy, dz+radius, uMax,vMax);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY,connection.start.posZ+radius));
//			tes.addVertexWithUV(0, 0, 0+radius, b?uMax-uShift:uMin,vMax);
			tes.addVertexWithUV(0, 0, 0+radius, uMin,vMax);

			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY+dy,connection.start.posZ-radius));
//			tes.addVertexWithUV(dx, dy, dz-radius, b?uMin:uMin+uShift,vMin);
			tes.addVertexWithUV(dx, dy, dz-radius, uMax,vMin);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY,connection.start.posZ-radius));
//			tes.addVertexWithUV(0, 0, 0-radius, b?uMax-uShift:uMin,vMin);
			tes.addVertexWithUV(0, 0, 0-radius, uMin,vMin);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY,connection.start.posZ+radius));
//			tes.addVertexWithUV(0, 0, 0+radius, b?uMax-uShift:uMin,vMax);
			tes.addVertexWithUV(0, 0, 0+radius, uMin,vMax);
			tes.setBrightness(calcBrightness(world, connection.start.posX,connection.start.posY+dy,connection.start.posZ+radius));
//			tes.addVertexWithUV(dx, dy, dz+radius, b?uMin:uMin+uShift,vMax);
			tes.addVertexWithUV(dx, dy, dz+radius, uMax,vMax);
			tes.addTranslation((float)-initPos.xCoord,(float)-initPos.yCoord,(float)-initPos.zCoord);
		}
		else
		{
			double u0 = uMin;
			double u1 = uMin;
			for(int i=b?(vertex.length-1):0; (b?(i>=0):(i<vertex.length)); i+=(b?-1:1))
			{
				Vec3 v0 = i>0?vertex[i-1]:initPos;
				Vec3 v1 = vertex[i];

//				double u0 = uMin;
//				double u1 = uMax;
				u0 = u1;
				u1 = u0+(v0.distanceTo(v1)/d)*uD;
				if((dx<0&&dz<=0)||(dz<0&&dx<=0)||(dz<0&&dx>0))
				{
					u1 = uMin;
					u0 = uMax;
				}
				tes.setColorRGBA_I(colour, alpha);
				tes.setBrightness(calcBrightness(world, v0.xCoord, v0.yCoord+radius, v0.zCoord));
				tes.addVertexWithUV(v0.xCoord, v0.yCoord+radius, v0.zCoord, u0,vMax);
				tes.setBrightness(calcBrightness(world, v1.xCoord, v1.yCoord+radius, v1.zCoord));
				tes.addVertexWithUV(v1.xCoord, v1.yCoord+radius, v1.zCoord, u1,vMax);
				tes.setBrightness(calcBrightness(world, v1.xCoord, v1.yCoord-radius, v1.zCoord));
				tes.addVertexWithUV(v1.xCoord, v1.yCoord-radius, v1.zCoord, u1,vMin);
				tes.setBrightness(calcBrightness(world, v0.xCoord, v0.yCoord-radius, v0.zCoord));
				tes.addVertexWithUV(v0.xCoord, v0.yCoord-radius, v0.zCoord, u0,vMin);

				tes.setBrightness(calcBrightness(world, v1.xCoord, v1.yCoord+radius, v1.zCoord));
				tes.addVertexWithUV(v1.xCoord, v1.yCoord+radius, v1.zCoord, u1,vMax);
				tes.setBrightness(calcBrightness(world, v0.xCoord, v0.yCoord+radius, v0.zCoord));
				tes.addVertexWithUV(v0.xCoord, v0.yCoord+radius, v0.zCoord, u0,vMax);
				tes.setBrightness(calcBrightness(world, v0.xCoord, v0.yCoord-radius, v0.zCoord));
				tes.addVertexWithUV(v0.xCoord, v0.yCoord-radius, v0.zCoord, u0,vMin);
				tes.setBrightness(calcBrightness(world, v1.xCoord, v1.yCoord-radius, v1.zCoord));
				tes.addVertexWithUV(v1.xCoord, v1.yCoord-radius, v1.zCoord, u1,vMin);

				tes.setColorRGBA_I(colour, alpha);
				tes.setBrightness(calcBrightness(world, v0.xCoord-radius*rmodx, v0.yCoord, v0.zCoord+radius*rmodz));
				tes.addVertexWithUV(v0.xCoord-radius*rmodx, v0.yCoord, v0.zCoord+radius*rmodz, u0,vMax);
				tes.setBrightness(calcBrightness(world, v1.xCoord-radius*rmodx, v1.yCoord, v1.zCoord+radius*rmodz));
				tes.addVertexWithUV(v1.xCoord-radius*rmodx, v1.yCoord, v1.zCoord+radius*rmodz, u1,vMax);
				tes.setBrightness(calcBrightness(world, v1.xCoord+radius*rmodx, v1.yCoord, v1.zCoord-radius*rmodz));
				tes.addVertexWithUV(v1.xCoord+radius*rmodx, v1.yCoord, v1.zCoord-radius*rmodz, u1,vMin);
				tes.setBrightness(calcBrightness(world, v0.xCoord+radius*rmodx, v0.yCoord, v0.zCoord-radius*rmodz));
				tes.addVertexWithUV(v0.xCoord+radius*rmodx, v0.yCoord, v0.zCoord-radius*rmodz, u0,vMin);

				tes.setBrightness(calcBrightness(world, v1.xCoord-radius*rmodx, v1.yCoord, v1.zCoord+radius*rmodz));
				tes.addVertexWithUV(v1.xCoord-radius*rmodx, v1.yCoord, v1.zCoord+radius*rmodz, u1,vMax);
				tes.setBrightness(calcBrightness(world, v0.xCoord-radius*rmodx, v0.yCoord, v0.zCoord+radius*rmodz));
				tes.addVertexWithUV(v0.xCoord-radius*rmodx, v0.yCoord, v0.zCoord+radius*rmodz, u0,vMax);
				tes.setBrightness(calcBrightness(world, v0.xCoord+radius*rmodx, v0.yCoord, v0.zCoord-radius*rmodz));
				tes.addVertexWithUV(v0.xCoord+radius*rmodx, v0.yCoord, v0.zCoord-radius*rmodz, u0,vMin);
				tes.setBrightness(calcBrightness(world, v1.xCoord+radius*rmodx, v1.yCoord, v1.zCoord-radius*rmodz));
				tes.addVertexWithUV(v1.xCoord+radius*rmodx, v1.yCoord, v1.zCoord-radius*rmodz, u1,vMin);
			}
		}
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
	public static void bindAtlas(int i)
	{
		mc().getTextureManager().bindTexture(i==0?TextureMap.locationBlocksTexture:TextureMap.locationItemsTexture);
	}
	public static ResourceLocation getResource(String path)
	{
		ResourceLocation rl = resourceMap.containsKey(path) ? resourceMap.get(path) : new ResourceLocation(path);
		if(!resourceMap.containsKey(path))
			resourceMap.put(path, rl);
		return rl;
	}
	public static WavefrontObject getModel(String path)
	{
		ResourceLocation rl = resourceMap.containsKey(path) ? resourceMap.get(path) : new ResourceLocation(path);
		if(!resourceMap.containsKey(path))
			resourceMap.put(path, rl);
		return (WavefrontObject)AdvancedModelLoader.loadModel(rl);
	}
	public static FontRenderer font()
	{
		return mc().fontRenderer;
	}

	public static String getResourceNameForItemStack(ItemStack stack)
	{
		if(stack!=null)
		{
			IIcon ic = null;
			Block b = Block.getBlockFromItem(stack.getItem());
			if(b!=null&&b!=Blocks.air)
				ic = b.getIcon(2, stack.getItemDamage());
			else
				ic = stack.getIconIndex();
			if(ic!=null)
			{
				String name = ic.getIconName();
				String resource = "";
				String icon = "";
				if(name.indexOf(":")>0)
				{
					String[] split = name.split(":",2);
					resource = split[0]+":";
					icon = split[1];
				}
				else
					icon = name;
				return resource + "textures/" + (stack.getItemSpriteNumber()==0?"blocks":"items") + "/" + icon+ ".png";
			}
		}
		return "";
	}

	public static IESound generatePositionedIESound(String soundName, float volume, float pitch, boolean repeat, int delay, double x, double y, double z)
	{
		IESound sound = new IESound(new ResourceLocation(soundName), volume,pitch, repeat,delay, x,y,z, AttenuationType.LINEAR);
		sound.evaluateVolume();
		ClientUtils.mc().getSoundHandler().playSound(sound);
		return sound;
	}

	public static void handleStaticTileRenderer(TileEntity tile)
	{
		TileEntitySpecialRenderer tesr = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile);
		if(tesr instanceof TileRenderIE)
			((TileRenderIE)tesr).renderStatic(tile, Tessellator.instance, new Matrix4().translate(tile.xCoord, tile.yCoord, tile.zCoord), new Matrix4());
	}

	/**
	 * A big "Thank you!" to AtomicBlom and Rorax for helping me figure this one out =P
	 */
	public static void renderStaticWavefrontModel(TileEntity tile, WavefrontObject model, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, int offsetLighting, boolean invertFaces, String... renderedParts)
	{
		tes.setColorRGBA_F(1F, 1F, 1F, 1F);

		if(tile.getWorldObj()!=null)
		{
			int lb = tile.getWorldObj().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord, tile.zCoord, 0);
			int lb_j = lb % 65536;
			int lb_k = lb / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lb_j / 1.0F, (float)lb_k / 1.0F);
		}
		Vertex vertexCopy = new Vertex(0,0,0);
		Vertex normalCopy = new Vertex(0,0,0);

		for(GroupObject groupObject : model.groupObjects)
		{
			boolean render = false;
			if(renderedParts==null || renderedParts.length<1)
				render = true;
			else
				for(String s : renderedParts)
					if(groupObject.name.equalsIgnoreCase(s))
						render = true;
			if(render)
				for(Face face : groupObject.faces)
				{
					if(face.faceNormal == null)
						face.faceNormal = face.calculateFaceNormal();

					normalCopy.x = face.faceNormal.x;
					normalCopy.y = face.faceNormal.y;
					normalCopy.z = face.faceNormal.z;
					rotationMatrix.apply(normalCopy);
					float biggestNormal = Math.max(Math.abs(normalCopy.y), Math.max(Math.abs(normalCopy.x),Math.abs(normalCopy.z)));
					int side = biggestNormal==Math.abs(normalCopy.y)?(normalCopy.y<0?0:1): biggestNormal==Math.abs(normalCopy.z)?(normalCopy.z<0?2:3): (normalCopy.x<0?4:5);

					HashMap<String,BlockLightingInfo> light = new HashMap<String,BlockLightingInfo>();
					BlockLightingInfo completeLight = null;
					if(offsetLighting==0 && tile.getWorldObj()!=null)
						completeLight = calculateBlockLighting(side, tile.getWorldObj(), tile.getBlockType(), tile.xCoord,tile.yCoord,tile.zCoord, 1,1,1);



					tes.setNormal(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z);
					for(int i=0; i<face.vertices.length; ++i)
					{
						int target = !invertFaces?i:(face.vertices.length-1-i);
						int corner = (int)(target/(float)face.vertices.length*4);
						Vertex vertex = face.vertices[target];
						vertexCopy.x = vertex.x;
						vertexCopy.y = vertex.y;
						vertexCopy.z = vertex.z;
						rotationMatrix.apply(vertexCopy);
						translationMatrix.apply(vertexCopy);

						if(offsetLighting==1 && tile.getWorldObj()!=null)
						{	
							String key = Math.round(tile.xCoord+vertex.x)+";"+Math.round(tile.yCoord+vertex.y)+";"+Math.round(tile.zCoord+vertex.z);
							BlockLightingInfo info = light.get(key);
							if(info==null)
							{
								info = calculateBlockLighting(side, tile.getWorldObj(), tile.getBlockType(), (int)Math.round(tile.xCoord+vertex.x),(int)Math.round(tile.yCoord+vertex.y),(int)Math.round(tile.zCoord+vertex.z), 1,1,1);
								light.put(key, info);
							}
							tes.setBrightness(corner==0?info.brightnessTopLeft: corner==1?info.brightnessBottomLeft: corner==2?info.brightnessBottomRight: info.brightnessTopRight);
							float r = corner==0?info.colorRedTopLeft: corner==1?info.colorRedBottomLeft: corner==2?info.colorRedBottomRight: info.colorRedTopRight;
							float g = corner==0?info.colorGreenTopLeft: corner==1?info.colorGreenBottomLeft: corner==2?info.colorGreenBottomRight: info.colorGreenTopRight;
							float b = corner==0?info.colorBlueTopLeft: corner==1?info.colorBlueBottomLeft: corner==2?info.colorBlueBottomRight: info.colorBlueTopRight;
							tes.setColorOpaque_F(r, g, b);
						}
						else if(offsetLighting==0 && tile.getWorldObj()!=null && completeLight!=null)
						{	
							tes.setBrightness(corner==0?completeLight.brightnessTopLeft: corner==1?completeLight.brightnessBottomLeft: corner==2?completeLight.brightnessBottomRight: completeLight.brightnessTopRight);
							float r = corner==0?completeLight.colorRedTopLeft: corner==1?completeLight.colorRedBottomLeft: corner==2?completeLight.colorRedBottomRight: completeLight.colorRedTopRight;
							float g = corner==0?completeLight.colorGreenTopLeft: corner==1?completeLight.colorGreenBottomLeft: corner==2?completeLight.colorGreenBottomRight: completeLight.colorGreenTopRight;
							float b = corner==0?completeLight.colorBlueTopLeft: corner==1?completeLight.colorBlueBottomLeft: corner==2?completeLight.colorBlueBottomRight: completeLight.colorBlueTopRight;
							tes.setColorOpaque_F(r, g, b);
						}
						else
						{
							tes.setBrightness(0xf000f0);
							tes.setColorOpaque_F(1,1,1);
						}

						if((face.textureCoordinates != null) && (face.textureCoordinates.length > 0))
						{
							TextureCoordinate textureCoordinate = face.textureCoordinates[target];
							tes.addVertexWithUV(vertexCopy.x, vertexCopy.y, vertexCopy.z, textureCoordinate.u, textureCoordinate.v);
						}
						else
						{
							tes.addVertex(vertexCopy.x, vertexCopy.y, vertexCopy.z);
						}
					}
				}
		}
	}
	public static void renderWavefrontModelWithModifications(WavefrontObject model, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, boolean flipTextureU, String... renderedParts)
	{
		Vertex vertexCopy = new Vertex(0,0,0);
		Vertex normalCopy = new Vertex(0,0,0);

		for(GroupObject groupObject : model.groupObjects)
		{
			boolean render = false;
			if(renderedParts==null || renderedParts.length<1)
				render = true;
			else
				for(String s : renderedParts)
					if(groupObject.name.equalsIgnoreCase(s))
						render = true;
			if(render)
				for(Face face : groupObject.faces)
				{
					if(face.faceNormal == null)
						face.faceNormal = face.calculateFaceNormal();

					normalCopy.x = face.faceNormal.x;
					normalCopy.y = face.faceNormal.y;
					normalCopy.z = face.faceNormal.z;
					rotationMatrix.apply(normalCopy);


					tes.setNormal(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z);
					for(int i=0; i<face.vertices.length; ++i)
					{
						Vertex vertex = face.vertices[i];
						vertexCopy.x = vertex.x;
						vertexCopy.y = vertex.y;
						vertexCopy.z = vertex.z;
						rotationMatrix.apply(vertexCopy);
						translationMatrix.apply(vertexCopy);

						if((face.textureCoordinates != null) && (face.textureCoordinates.length > 0))
						{
							TextureCoordinate textureCoordinate = face.textureCoordinates[flipTextureU?(face.textureCoordinates.length-1)-i: i];
							tes.addVertexWithUV(vertexCopy.x, vertexCopy.y, vertexCopy.z, textureCoordinate.u, textureCoordinate.v);
						}
						else
							tes.addVertex(vertexCopy.x, vertexCopy.y, vertexCopy.z);
					}
				}
		}
	}

	public static void renderWavefrontWithIconUVs(WavefrontObject model, IIcon icon, String... parts)
	{
		renderWavefrontWithIconUVs(model, GL11.GL_QUADS, icon, parts);
		renderWavefrontWithIconUVs(model, GL11.GL_TRIANGLES, icon, parts);
	}

	public static void renderWavefrontWithIconUVs(WavefrontObject model, int glDrawingMode, IIcon icon, String... parts)
	{
		List<String> renderParts = Arrays.asList(parts);
		tes().startDrawing(glDrawingMode);
		for(GroupObject go : model.groupObjects)
			if(go.glDrawingMode==glDrawingMode)
			{
				if(renderParts.contains(go.name))
				{
					for(Face face : go.faces)
					{
						float minU = icon.getMinU();
						float sizeU = icon.getMaxU() - minU;
						float minV = icon.getMinV();
						float sizeV = icon.getMaxV() - minV;

						TextureCoordinate[] oldUVs = new TextureCoordinate[face.textureCoordinates.length];
						for(int v=0; v<face.vertices.length; ++v)
						{
							oldUVs[v] = face.textureCoordinates[v]; 
							TextureCoordinate textureCoordinate = face.textureCoordinates[v];
							face.textureCoordinates[v] = new TextureCoordinate(
									minU + sizeU * textureCoordinate.u,
									minV + sizeV * textureCoordinate.v
									);
						}
						face.addFaceForRender(ClientUtils.tes());
						for(int v=0; v<face.vertices.length; ++v)
							face.textureCoordinates[v] = new TextureCoordinate(oldUVs[v].u,oldUVs[v].v);
					}
				}
			}
		tes().draw();
	}

	public static void drawInventoryBlock(Block block, int metadata, RenderBlocks renderer)
	{
		Tessellator tes = tes();
		GL11.glPushMatrix();
		GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tes.startDrawingQuads();
		tes.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
		tes.draw();
		tes.startDrawingQuads();
		tes.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
		tes.draw();
		GL11.glPopMatrix();
	}

	public static void drawColouredRect(int x, int y, int w, int h, int colour)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		tes().startDrawingQuads();
		tes().setColorRGBA_I(colour, colour>>24&255);
		tes().addVertex(x, y+h, 0);
		tes().addVertex(x+w, y+h, 0);
		tes().addVertex(x+w, y, 0 );
		tes().addVertex(x, y, 0);
		tes().draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
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
		if(icon != null)
		{
			int iW = icon.getIconWidth();
			int iH = icon.getIconHeight();
			if(iW>0 && iH>0)
				drawRepeatedIcon(x,y,w,h, iW, iH, icon.getMinU(),icon.getMaxU(), icon.getMinV(),icon.getMaxV());
		}
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
	public static void drawSlot(int x, int y, int w, int h)
	{
		drawSlot(x,y, w,h, 0xff);
	}
	public static void drawSlot(int x, int y, int w, int h, int alpha)
	{
		GuiDraw.drawRect(x+8-w/2  , y+8-h/2-1, w,1, (alpha<<24)+0x373737);
		GuiDraw.drawRect(x+8-w/2-1, y+8-h/2-1, 1,h+1, (alpha<<24)+0x373737);
		GuiDraw.drawRect(x+8-w/2  , y+8-h/2  , w,h, (alpha<<24)+0x8b8b8b);
		GuiDraw.drawRect(x+8-w/2  , y+8+h/2  , w+1,1, (alpha<<24)+0xffffff);
		GuiDraw.drawRect(x+8+w/2  , y+8-h/2  , 1,h, (alpha<<24)+0xffffff);
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
			int l1 = ((k1 & 16711422) >> 1 | k1 & -16777216);
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

	public static void handleGuiTank(FluidTank tank, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, String originalTexture, ArrayList<String> tooltip)
	{
		if(tooltip==null)
		{
			if(tank.getFluid()!=null && tank.getFluid().getFluid()!=null)
			{
				int fluidHeight = (int)(h*(tank.getFluid().amount/(float)tank.getCapacity()));
				drawRepeatedFluidIcon(tank.getFluid().getFluid(), x,y+h-fluidHeight, w, fluidHeight);
				bindTexture(originalTexture);
			}
			int xOff = (w-oW)/2;
			int yOff = (h-oH)/2;
			drawTexturedRect(x+xOff,y+yOff,oW,oH, 256f, oX,oX+oW,oY,oY+oH);
		}
		else
		{
			if(mX>x&&mX<x+w && mY>y&&mY<y+h)
			{
				if(tank.getFluid()!=null && tank.getFluid().getFluid()!=null)
					tooltip.add(tank.getFluid().getLocalizedName());
				else
					tooltip.add(StatCollector.translateToLocal("gui.ImmersiveEngineering.empty"));
				tooltip.add(tank.getFluidAmount()+"/"+tank.getCapacity()+"mB");
			}
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
		float f3 = 0.0F;
		float f4 = 0.0F;
		float f5 = 0.0F;
		float f6 = 0.0F;
		int l = block.getMixedBrightnessForBlock(world, x, y, z);

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

			if (RenderBlocks.getInstance().renderMinY <= 0.0D)
			{
				++y;
			}

			i1 = l;

			if (RenderBlocks.getInstance().renderMinY <= 0.0D || !world.getBlock(x, y - 1, z).isOpaqueCube())
			{
				i1 = block.getMixedBrightnessForBlock(world, x, y - 1, z);
			}

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

			if (RenderBlocks.getInstance().renderMaxY >= 1.0D)
			{
				--y;
			}

			i1 = l;

			if (RenderBlocks.getInstance().renderMaxY >= 1.0D || !world.getBlock(x, y + 1, z).isOpaqueCube())
			{
				i1 = block.getMixedBrightnessForBlock(world, x, y + 1, z);
			}

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
		}

		float f8;
		float f9;
		float f10;
		float f11;
		int j1;
		int k1;
		int l1;
		int i2;

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

			if (RenderBlocks.getInstance().renderMinZ <= 0.0D)
			{
				++z;
			}

			i1 = l;

			if (RenderBlocks.getInstance().renderMinZ <= 0.0D || !world.getBlock(x, y, z - 1).isOpaqueCube())
			{
				i1 = block.getMixedBrightnessForBlock(world, x, y, z - 1);
			}

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
		}
		return lightingInfo;
	}

	public static boolean drawWorldBlock(IBlockAccess world, Block block, int x, int y, int z, double[][] uv)
	{
		Tessellator tes = tes();
		boolean flag = false;
		BlockLightingInfo info;
		// SIDE 0
		if(block.shouldSideBeRendered(world, x, y, z, 0))
		{
			info = calculateBlockLighting(0, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+0, z+1, uv[0][0], uv[0][3]);
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+0, z+0, uv[0][0], uv[0][2]);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+0, z+0, uv[0][1], uv[0][2]);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1, y+0, z+1, uv[0][1], uv[0][3]);
			flag=true;
		}
		// SIDE 1
		if(block.shouldSideBeRendered(world, x, y, z, 1))
		{
			info = calculateBlockLighting(1, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+1, y+1, z+1, uv[1][1], uv[1][3]);
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+1, z+0, uv[1][1], uv[1][2]);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+0, y+1, z+0, uv[1][0], uv[1][2]);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0, y+1, z+1, uv[1][0], uv[1][3]);
			flag=true;
		}
		// SIDE 2
		if(block.shouldSideBeRendered(world, x, y, z, 2))
		{
			info = calculateBlockLighting(2, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+1, z+0, uv[2][0], uv[2][3]);
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+1, z+0, uv[2][1], uv[2][3]);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+0, z+0, uv[2][1], uv[2][2]);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0, y+0, z+0, uv[2][0], uv[2][2]);
			flag=true;
		}
		// SIDE 3
		if(block.shouldSideBeRendered(world, x, y, z, 3))
		{
			info = calculateBlockLighting(3, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+1, z+1, uv[3][0], uv[3][3]);
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+0, z+1, uv[3][0], uv[3][2]);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+0, z+1, uv[3][1], uv[3][2]);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1, y+1, z+1, uv[3][1], uv[3][3]);
			flag=true;
		}
		// SIDE 4
		if(block.shouldSideBeRendered(world, x, y, z, 4))
		{
			info = calculateBlockLighting(4, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+0, y+1, z+1, uv[4][1], uv[4][3]);
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+0, y+1, z+0, uv[4][0], uv[4][3]);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+0, y+0, z+0, uv[4][0], uv[4][2]);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+0, y+0, z+1, uv[4][1], uv[4][2]);
			flag=true;
		}
		// SIDE 5
		if(block.shouldSideBeRendered(world, x, y, z, 5))
		{
			info = calculateBlockLighting(5, world, block, x,y,z, 1,1,1);
			tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
			tes.setBrightness(info.brightnessTopLeft);
			tes.addVertexWithUV(x+1, y+0, z+1, uv[5][1], uv[5][2]);
			tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
			tes.setBrightness(info.brightnessBottomLeft);
			tes.addVertexWithUV(x+1, y+0, z+0, uv[5][0], uv[5][2]);
			tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
			tes.setBrightness(info.brightnessBottomRight);
			tes.addVertexWithUV(x+1, y+1, z+0, uv[5][0], uv[5][3]);
			tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
			tes.setBrightness(info.brightnessTopRight);
			tes.addVertexWithUV(x+1, y+1, z+1, uv[5][1], uv[5][3]);
			flag=true;
		}
		return flag;
	}

	/**
	 * @param vs 000,001,100,101, 010,011,110,111 
	 */
	public static boolean drawWorldSubBlock(RenderBlocks renderer, IBlockAccess world, Block block, int x, int y, int z, Vec3[] vs)
	{
		Tessellator tes = tes();
		boolean flag = false;
		BlockLightingInfo info;
		IIcon icon;
		// SIDE 0
		if(block.shouldSideBeRendered(world, x, y, z, 0))
		{
			icon = renderer.hasOverrideBlockTexture()?renderer.overrideBlockTexture: block.getIcon(world,x,y,z,0);
			info = calculateBlockLighting(0, world, block, x,y,z, 1,1,1);

			double d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[0].xCoord * 16)));
			double d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[3].xCoord * 16)));
			double d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[0].zCoord * 16)));
			double d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[3].zCoord * 16)));
			double d7 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[2].xCoord * 16)));
			double d8 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[1].xCoord * 16)));
			double d9 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[2].zCoord * 16)));
			double d10 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[1].zCoord * 16)));

			if(renderer.uvRotateBottom == 2)
			{
				d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[0].zCoord * 16)));
				d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[3].xCoord * 16)));
				d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[3].zCoord * 16)));
				d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[0].xCoord * 16)));
				d9 = d5;
				d10 = d6;
				d7 = d3;
				d8 = d4;
				d5 = d6;
				d6 = d9;
			}
			else if(renderer.uvRotateBottom == 1)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[3].zCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[0].xCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[0].zCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[3].xCoord * 16)));
				d7 = d4;
				d8 = d3;
				d3 = d4;
				d4 = d8;
				d9 = d6;
				d10 = d5;
			}
			else if(renderer.uvRotateBottom == 3)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[0].xCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[3].xCoord * 16)));
				d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[0].zCoord * 16)));
				d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[3].zCoord * 16)));
				d7 = d4;
				d8 = d3;
				d9 = d5;
				d10 = d6;
			}

			if(renderer.enableAO)
			{
				tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
				tes.setBrightness(info.brightnessTopLeft);
				tes.addVertexWithUV(x+vs[1].xCoord, y+vs[1].yCoord, z+vs[1].zCoord, d8,d10);
				tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
				tes.setBrightness(info.brightnessBottomLeft);
				tes.addVertexWithUV(x+vs[0].xCoord, y+vs[0].yCoord, z+vs[0].zCoord, d3,d5);
				tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
				tes.setBrightness(info.brightnessBottomRight);
				tes.addVertexWithUV(x+vs[2].xCoord, y+vs[2].yCoord, z+vs[2].zCoord, d7,d9);
				tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
				tes.setBrightness(info.brightnessTopRight);
				tes.addVertexWithUV(x+vs[3].xCoord, y+vs[3].yCoord, z+vs[3].zCoord, d4,d6);
			}
			else
			{
				tes.setColorOpaque_I(0xffffff);
				tes.addVertexWithUV(x+vs[1].xCoord, y+vs[1].yCoord, z+vs[1].zCoord, d8,d10);
				tes.addVertexWithUV(x+vs[0].xCoord, y+vs[0].yCoord, z+vs[0].zCoord, d3,d5);
				tes.addVertexWithUV(x+vs[2].xCoord, y+vs[2].yCoord, z+vs[2].zCoord, d7,d9);
				tes.addVertexWithUV(x+vs[3].xCoord, y+vs[3].yCoord, z+vs[3].zCoord, d4,d6);
			}
			flag=true;
		}
		// SIDE 1
		if(block.shouldSideBeRendered(world, x, y, z, 1))
		{
			icon = renderer.hasOverrideBlockTexture()?renderer.overrideBlockTexture: block.getIcon(world,x,y,z,1);
			info = calculateBlockLighting(1, world, block, x,y,z, 1,1,1);
			tes.setBrightness(983055);

			double d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[4].xCoord * 16)));
			double d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[7].xCoord * 16)));
			double d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[4].zCoord * 16)));
			double d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[7].zCoord * 16)));
			double d7 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[6].xCoord * 16)));
			double d8 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[5].xCoord * 16)));
			double d9 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[6].zCoord * 16)));
			double d10 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[5].zCoord * 16)));

			if(renderer.uvRotateTop == 1)
			{
				d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[4].zCoord * 16)));
				d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[7].xCoord * 16)));
				d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[7].zCoord * 16)));
				d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[4].xCoord * 16)));
				d9 = d5;
				d10 = d6;
				d7 = d3;
				d8 = d4;
				d5 = d6;
				d6 = d9;
			}
			else if(renderer.uvRotateTop == 2)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[7].zCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[4].xCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[4].zCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[7].xCoord * 16)));
				d7 = d4;
				d8 = d3;
				d3 = d4;
				d4 = d8;
				d9 = d6;
				d10 = d5;
			}
			else if(renderer.uvRotateTop == 3)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[4].xCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[7].xCoord * 16)));
				d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[4].zCoord * 16)));
				d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[7].zCoord * 16)));
				d7 = d4;
				d8 = d3;
				d9 = d5;
				d10 = d6;
			}

			if(renderer.enableAO)
			{
				tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
				tes.setBrightness(info.brightnessTopLeft);
				tes.addVertexWithUV(x+vs[7].xCoord, y+vs[7].yCoord, z+vs[7].zCoord, d4, d6);
				tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
				tes.setBrightness(info.brightnessBottomLeft);
				tes.addVertexWithUV(x+vs[6].xCoord, y+vs[6].yCoord, z+vs[6].zCoord, d7, d9);
				tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
				tes.setBrightness(info.brightnessBottomRight);
				tes.addVertexWithUV(x+vs[4].xCoord, y+vs[4].yCoord, z+vs[4].zCoord, d3, d5);
				tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
				tes.setBrightness(info.brightnessTopRight);
				tes.addVertexWithUV(x+vs[5].xCoord, y+vs[5].yCoord, z+vs[5].zCoord, d8, d10);
			}
			else
			{
				tes.setColorOpaque_I(0xffffff);
				tes.addVertexWithUV(x+vs[7].xCoord, y+vs[7].yCoord, z+vs[7].zCoord, d4, d6);
				tes.addVertexWithUV(x+vs[6].xCoord, y+vs[6].yCoord, z+vs[6].zCoord, d7, d9);
				tes.addVertexWithUV(x+vs[4].xCoord, y+vs[4].yCoord, z+vs[4].zCoord, d3, d5);
				tes.addVertexWithUV(x+vs[5].xCoord, y+vs[5].yCoord, z+vs[5].zCoord, d8, d10);
			}
			flag=true;
		}
		// SIDE 2
		if(block.shouldSideBeRendered(world, x, y, z, 2))
		{
			icon = renderer.hasOverrideBlockTexture()?renderer.overrideBlockTexture: block.getIcon(world,x,y,z,2);
			info = calculateBlockLighting(2, world, block, x,y,z, 1,1,1);
			tes.setBrightness(983055);

			double d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[0].xCoord * 16)));
			double d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[6].xCoord * 16)));
			double d5 = icon.getInterpolatedV(16.0D - Math.min(16,Math.max(0, vs[6].yCoord * 16)));
			double d6 = icon.getInterpolatedV(16.0D - Math.min(16,Math.max(0, vs[0].yCoord * 16)));
			double d7 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[2].xCoord * 16)));
			double d8 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[4].xCoord * 16)));
			double d9 = icon.getInterpolatedV(16.0D - Math.min(16,Math.max(0, vs[4].yCoord * 16)));
			double d10 = icon.getInterpolatedV(16.0D - Math.min(16,Math.max(0, vs[2].yCoord * 16)));

			if(renderer.uvRotateEast == 2)
			{
				d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[0].yCoord * 16)));
				d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[6].yCoord * 16)));
				d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[6].xCoord * 16)));
				d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[0].xCoord * 16)));
				d9 = d5;
				d10 = d6;
				d7 = d3;
				d8 = d4;
				d5 = d6;
				d6 = d9;
			}
			else if(renderer.uvRotateEast == 1)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[6].yCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[0].yCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[0].xCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[6].xCoord * 16)));
				d7 = d4;
				d8 = d3;
				d3 = d4;
				d4 = d8;
				d9 = d6;
				d10 = d5;
			}
			else if(renderer.uvRotateEast == 3)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[6].xCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[0].xCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[0].yCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[6].yCoord * 16)));
				d7 = d4;
				d8 = d3;
				d9 = d5;
				d10 = d6;
			}

			if(renderer.enableAO)
			{
				tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
				tes.setBrightness(info.brightnessTopLeft);
				tes.addVertexWithUV(x+vs[4].xCoord, y+vs[4].yCoord, z+vs[4].zCoord, d7, d9);
				tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
				tes.setBrightness(info.brightnessBottomLeft);
				tes.addVertexWithUV(x+vs[6].xCoord, y+vs[6].yCoord, z+vs[6].zCoord, d3, d5);
				tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
				tes.setBrightness(info.brightnessBottomRight);
				tes.addVertexWithUV(x+vs[2].xCoord, y+vs[2].yCoord, z+vs[2].zCoord, d8, d10);
				tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
				tes.setBrightness(info.brightnessTopRight);
				tes.addVertexWithUV(x+vs[0].xCoord, y+vs[0].yCoord, z+vs[0].zCoord, d4, d6);
			}
			else
			{
				tes.setColorOpaque_I(0xffffff);
				tes.addVertexWithUV(x+vs[4].xCoord, y+vs[4].yCoord, z+vs[4].zCoord, d7, d9);
				tes.addVertexWithUV(x+vs[6].xCoord, y+vs[6].yCoord, z+vs[6].zCoord, d3, d5);
				tes.addVertexWithUV(x+vs[2].xCoord, y+vs[2].yCoord, z+vs[2].zCoord, d8, d10);
				tes.addVertexWithUV(x+vs[0].xCoord, y+vs[0].yCoord, z+vs[0].zCoord, d4, d6);
			}

			flag=true;
		}
		// SIDE 3
		if(block.shouldSideBeRendered(world, x, y, z, 3))
		{
			icon = renderer.hasOverrideBlockTexture()?renderer.overrideBlockTexture: block.getIcon(world,x,y,z,3);
			info = calculateBlockLighting(3, world, block, x,y,z, 1,1,1);
			tes.setBrightness(983055);

			double d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[5].xCoord * 16)));
			double d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[3].xCoord * 16)));
			double d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[5].yCoord * 16)));
			double d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[3].yCoord * 16)));
			double d7 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[7].xCoord * 16)));
			double d8 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[1].xCoord * 16)));
			double d9 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[7].yCoord * 16)));
			double d10 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[1].yCoord * 16)));

			if(renderer.uvRotateWest == 1)
			{
				d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[3].yCoord * 16)));
				d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[5].xCoord * 16)));
				d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[5].yCoord * 16)));
				d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[3].xCoord * 16)));
				d9 = d5;
				d10 = d6;
				d7 = d3;
				d8 = d4;
				d5 = d6;
				d6 = d9;
			}
			else if(renderer.uvRotateWest == 2)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[5].yCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[5].xCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[3].yCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[3].xCoord * 16)));
				d7 = d4;
				d8 = d3;
				d3 = d4;
				d4 = d8;
				d9 = d6;
				d10 = d5;
			}
			else if(renderer.uvRotateWest == 3)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[5].xCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[3].xCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[5].yCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[3].yCoord * 16)));
				d7 = d4;
				d8 = d3;
				d9 = d5;
				d10 = d6;
			}

			if(renderer.enableAO)
			{
				tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
				tes.setBrightness(info.brightnessTopLeft);
				tes.addVertexWithUV(x+vs[5].xCoord, y+vs[5].yCoord, z+vs[5].zCoord, d3, d5);
				tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
				tes.setBrightness(info.brightnessBottomLeft);
				tes.addVertexWithUV(x+vs[1].xCoord, y+vs[1].yCoord, z+vs[1].zCoord, d8, d10);
				tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
				tes.setBrightness(info.brightnessBottomRight);
				tes.addVertexWithUV(x+vs[3].xCoord, y+vs[3].yCoord, z+vs[3].zCoord, d4, d6);
				tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
				tes.setBrightness(info.brightnessTopRight);
				tes.addVertexWithUV(x+vs[7].xCoord, y+vs[7].yCoord, z+vs[7].zCoord, d7, d9);
			}
			else
			{
				tes.setColorOpaque_I(0xffffff);
				tes.addVertexWithUV(x+vs[5].xCoord, y+vs[5].yCoord, z+vs[5].zCoord, d3, d5);
				tes.addVertexWithUV(x+vs[1].xCoord, y+vs[1].yCoord, z+vs[1].zCoord, d8, d10);
				tes.addVertexWithUV(x+vs[3].xCoord, y+vs[3].yCoord, z+vs[3].zCoord, d4, d6);
				tes.addVertexWithUV(x+vs[7].xCoord, y+vs[7].yCoord, z+vs[7].zCoord, d7, d9);
			}
			flag=true;
		}
		// SIDE 4
		if(block.shouldSideBeRendered(world, x, y, z, 4))
		{
			icon = renderer.hasOverrideBlockTexture()?renderer.overrideBlockTexture: block.getIcon(world,x,y,z,4);
			info = calculateBlockLighting(4, world, block, x,y,z, 1,1,1);
			tes.setBrightness(983055);

			double d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[4].zCoord * 16)));
			double d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[1].zCoord * 16)));
			double d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[4].yCoord * 16)));
			double d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[1].yCoord * 16)));
			double d7 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[5].zCoord * 16)));
			double d8 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[0].zCoord * 16)));
			double d9 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[5].yCoord * 16)));
			double d10 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[0].yCoord * 16)));

			if(renderer.uvRotateNorth == 1)
			{
				d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[1].yCoord * 16)));
				d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[1].zCoord * 16)));
				d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[4].yCoord * 16)));
				d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[4].zCoord * 16)));
				d9 = d5;
				d10 = d6;
				d7 = d3;
				d8 = d4;
				d5 = d6;
				d6 = d9;
			}
			else if(renderer.uvRotateNorth == 2)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[4].yCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[4].zCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[1].yCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[1].zCoord * 16)));
				d7 = d4;
				d8 = d3;
				d3 = d4;
				d4 = d8;
				d9 = d6;
				d10 = d5;
			}
			else if(renderer.uvRotateNorth == 3)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[4].zCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[1].zCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[4].yCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[1].yCoord * 16)));
				d7 = d4;
				d8 = d3;
				d9 = d5;
				d10 = d6;
			}

			if(renderer.enableAO)
			{
				tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
				tes.setBrightness(info.brightnessTopLeft);
				tes.addVertexWithUV(x+vs[5].xCoord, y+vs[5].yCoord, z+vs[5].zCoord, d7, d9);
				tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
				tes.setBrightness(info.brightnessBottomLeft);
				tes.addVertexWithUV(x+vs[4].xCoord, y+vs[4].yCoord, z+vs[4].zCoord, d3, d5);
				tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
				tes.setBrightness(info.brightnessBottomRight);
				tes.addVertexWithUV(x+vs[0].xCoord, y+vs[0].yCoord, z+vs[0].zCoord, d8, d10);
				tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
				tes.setBrightness(info.brightnessTopRight);
				tes.addVertexWithUV(x+vs[1].xCoord, y+vs[1].yCoord, z+vs[1].zCoord, d4, d6);
			}
			else
			{
				tes.setColorOpaque_I(0xffffff);
				tes.addVertexWithUV(x+vs[5].xCoord, y+vs[5].yCoord, z+vs[5].zCoord, d7, d9);
				tes.addVertexWithUV(x+vs[4].xCoord, y+vs[4].yCoord, z+vs[4].zCoord, d3, d5);
				tes.addVertexWithUV(x+vs[0].xCoord, y+vs[0].yCoord, z+vs[0].zCoord, d8, d10);
				tes.addVertexWithUV(x+vs[1].xCoord, y+vs[1].yCoord, z+vs[1].zCoord, d4, d6);
			}
			flag=true;
		}
		// SIDE 5
		if(block.shouldSideBeRendered(world, x, y, z, 5))
		{
			icon = renderer.hasOverrideBlockTexture()?renderer.overrideBlockTexture: block.getIcon(world,x,y,z,5);
			info = calculateBlockLighting(5, world, block, x,y,z, 1,1,1);
			tes.setBrightness(983055);

			double d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[7].zCoord * 16)));
			double d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[2].zCoord * 16)));
			double d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[7].yCoord * 16)));
			double d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[2].yCoord * 16)));
			double d7 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[6].zCoord * 16)));
			double d8 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[3].zCoord * 16)));
			double d9 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[6].yCoord * 16)));
			double d10 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[3].yCoord * 16)));

			if(renderer.uvRotateSouth == 2)
			{
				d3 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[2].yCoord * 16)));
				d5 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[7].zCoord * 16)));
				d4 = icon.getInterpolatedU(Math.min(16,Math.max(0, vs[7].yCoord * 16)));
				d6 = icon.getInterpolatedV(16 - Math.min(16,Math.max(0, vs[2].zCoord * 16)));
				d9 = d5;
				d10 = d6;
				d7 = d3;
				d8 = d4;
				d5 = d6;
				d6 = d9;
			}
			else if(renderer.uvRotateSouth == 1)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[7].yCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[2].zCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[2].yCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[7].zCoord * 16)));
				d7 = d4;
				d8 = d3;
				d3 = d4;
				d4 = d8;
				d9 = d6;
				d10 = d5;
			}
			else if(renderer.uvRotateSouth == 3)
			{
				d3 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[7].zCoord * 16)));
				d4 = icon.getInterpolatedU(16 - Math.min(16,Math.max(0, vs[2].zCoord * 16)));
				d5 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[7].yCoord * 16)));
				d6 = icon.getInterpolatedV(Math.min(16,Math.max(0, vs[2].yCoord * 16)));
				d7 = d4;
				d8 = d3;
				d9 = d5;
				d10 = d6;
			}
			if(renderer.enableAO)
			{
				tes.setColorOpaque_F(info.colorRedTopLeft, info.colorGreenTopLeft, info.colorBlueTopLeft);
				tes.setBrightness(info.brightnessTopLeft);
				tes.addVertexWithUV(x+vs[3].xCoord, y+vs[3].yCoord, z+vs[3].zCoord, d8, d10);
				tes.setColorOpaque_F(info.colorRedBottomLeft, info.colorGreenBottomLeft, info.colorBlueBottomLeft);
				tes.setBrightness(info.brightnessBottomLeft);
				tes.addVertexWithUV(x+vs[2].xCoord, y+vs[2].yCoord, z+vs[2].zCoord, d4, d6);
				tes.setColorOpaque_F(info.colorRedBottomRight, info.colorGreenBottomRight, info.colorBlueBottomRight);
				tes.setBrightness(info.brightnessBottomRight);
				tes.addVertexWithUV(x+vs[6].xCoord, y+vs[6].yCoord, z+vs[6].zCoord, d7, d9);
				tes.setColorOpaque_F(info.colorRedTopRight, info.colorGreenTopRight, info.colorBlueTopRight);
				tes.setBrightness(info.brightnessTopRight);
				tes.addVertexWithUV(x+vs[7].xCoord, y+vs[7].yCoord, z+vs[7].zCoord, d3, d5);
			}
			else
			{
				tes.setColorOpaque_I(0xffffff);
				tes.addVertexWithUV(x+vs[3].xCoord, y+vs[3].yCoord, z+vs[3].zCoord, d8, d10);
				tes.addVertexWithUV(x+vs[2].xCoord, y+vs[2].yCoord, z+vs[2].zCoord, d4, d6);
				tes.addVertexWithUV(x+vs[6].xCoord, y+vs[6].yCoord, z+vs[6].zCoord, d7, d9);
				tes.addVertexWithUV(x+vs[7].xCoord, y+vs[7].yCoord, z+vs[7].zCoord, d3, d5);
			}
			flag=true;
		}
		return flag;
	}
}