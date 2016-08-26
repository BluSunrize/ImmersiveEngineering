package blusunrize.immersiveengineering.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Quat4d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.models.SmartLightingQuad;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;

public class ClientUtils
{
	public static final AxisAlignedBB standardBlockAABB = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	static HashMap<String, ResourceLocation> resourceMap = new HashMap<String, ResourceLocation>();
	public static TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
	//
	//	// MOD SPECIFIC METHODS
	//	public static void renderAttachedConnections(TileEntity tile)
	//	{
	//		if(tile.getWorldObj()!=null && tile instanceof IImmersiveConnectable)
	//		{
	//			Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(tile.getWorldObj(), Utils.toCC(tile));
	//			if(outputs!=null)
	//			{
	//				Iterator<ImmersiveNetHandler.Connection> itCon = outputs.iterator();
	//				while(itCon.hasNext())
	//				{
	//					ImmersiveNetHandler.Connection con = itCon.next();
	//					TileEntity tileEnd = tile.getWorldObj().getTileEntity(con.end.posX,con.end.posY,con.end.posZ);
	//					if(tileEnd instanceof IImmersiveConnectable)
	//						drawConnection(con, (IImmersiveConnectable)tile, Utils.toIIC(tileEnd, tile.getWorldObj()), con.cableType.getIcon(con));
	//				}
	//			}
	//		}
	//	}
	//
	public static void tessellateConnection(ImmersiveNetHandler.Connection connection, IImmersiveConnectable start, IImmersiveConnectable end, TextureAtlasSprite sprite)
	{
		if(connection==null || start==null || end==null)
			return;
		int col = connection.cableType.getColour(connection);
		double r = connection.cableType.getRenderDiameter()/2;
		int[] rgba = new int[]{col>>16&255, col>>8&255, col&255, 255};
		tessellateConnection(connection, start, end, rgba, r, sprite);
	}
	public static void tessellateConnection(ImmersiveNetHandler.Connection connection, IImmersiveConnectable start, IImmersiveConnectable end, int[] rgba, double radius, TextureAtlasSprite sprite)
	{
		if(connection==null || start==null || end==null || connection.end==null || connection.start==null)
			return;
		Vec3 startOffset = start.getConnectionOffset(connection);
		Vec3 endOffset = end.getConnectionOffset(connection);
		if(startOffset==null)
			startOffset=new Vec3(.5,.5,.5);
		if(endOffset==null)
			endOffset=new Vec3(.5,.5,.5);
		double dx = (connection.end.getX()+endOffset.xCoord)-(connection.start.getX()+startOffset.xCoord);
		double dy = (connection.end.getY()+endOffset.yCoord)-(connection.start.getY()+startOffset.yCoord);
		double dz = (connection.end.getZ()+endOffset.zCoord)-(connection.start.getZ()+startOffset.zCoord);
		double dw = Math.sqrt(dx*dx + dz*dz);
		double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
		World world = ((TileEntity)start).getWorld();
		Tessellator tes = tes();

		double rmodx = dz/dw;
		double rmodz = dx/dw;

		Vec3[] vertex = connection.getSubVertices(world);
		//		Vec3 initPos = new Vec3(connection.start.getX()+startOffset.xCoord, connection.start.getY()+startOffset.yCoord, connection.start.getZ()+startOffset.zCoord);
		Vec3 initPos = new Vec3(startOffset.xCoord, startOffset.yCoord, startOffset.zCoord);

		double uMin = sprite.getMinU();
		double uMax = sprite.getMaxU();
		double vMin = sprite.getMinV();
		double vMax = sprite.getMaxV();
		double uD = uMax-uMin;
		boolean vertical = connection.end.getX()==connection.start.getX() && connection.end.getZ()==connection.start.getZ();
		boolean b = (dx<0&&dz<=0)||(dz<0&&dx<=0)||(dz<0&&dx>0);


		WorldRenderer worldrenderer = tes.getWorldRenderer();
		//		worldrenderer.pos(x, y+h, 0).tex(uv[0], uv[3]).endVertex();
		//		worldrenderer.pos(x+w, y+h, 0).tex(uv[1], uv[3]).endVertex();
		//		worldrenderer.pos(x+w, y, 0).tex(uv[1], uv[2]).endVertex();
		//		worldrenderer.pos(x, y, 0).tex(uv[0], uv[2]).endVertex();
		if(vertical)
		{
			//			double uShift = Math.abs(dy)/ * uD;
			//			worldrenderer.pos(x, y, z)
			worldrenderer.setTranslation(initPos.xCoord,initPos.yCoord,initPos.zCoord);

			//			tes.addVertexWithUV(0-radius, 0, 0, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0-radius,0,0).tex(uMin,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx-radius, dy, dz, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx-radius, dy, dz).tex(uMax,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx+radius, dy, dz, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx+radius, dy, dz).tex(uMax,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0+radius, 0, 0, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0+radius, 0, 0).tex(uMin,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();

			//			tes.addVertexWithUV(dx-radius, dy, dz, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx-radius, dy, dz).tex(uMax,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0-radius, 0, 0, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0-radius, 0, 0).tex(uMin,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0+radius, 0, 0, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0+radius, 0, 0).tex(uMin,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx+radius, dy, dz, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx+radius, dy, dz).tex(uMax,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();


			//			tes.addVertexWithUV(0, 0, 0-radius, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0, 0, 0-radius).tex(uMin,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz-radius, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx, dy, dz-radius).tex(uMax,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz+radius, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx, dy, dz+radius).tex(uMax,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0+radius, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0, 0, 0+radius).tex(uMin,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();

			//			tes.addVertexWithUV(dx, dy, dz-radius, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx, dy, dz-radius).tex(uMax,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0-radius, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0, 0, 0-radius).tex(uMin,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0+radius, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0, 0, 0+radius).tex(uMin,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz+radius, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx, dy, dz+radius).tex(uMax,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
			worldrenderer.setTranslation(0, 0, 0);
		}
		else
		{
			double u0 = uMin;
			double u1 = uMin;
			for(int i=b?(vertex.length-1):0; (b?(i>=0):(i<vertex.length)); i+=(b?-1:1))
			{
				Vec3 v0 = i>0?vertex[i-1].subtract(connection.start.getX(), connection.start.getY(), connection.start.getZ()):initPos;
				Vec3 v1 = vertex[i].subtract(connection.start.getX(), connection.start.getY(), connection.start.getZ());

				//				double u0 = uMin;
				//				double u1 = uMax;
				u0 = u1;
				u1 = u0+(v0.distanceTo(v1)/d)*uD;
				if((dx<0&&dz<=0)||(dz<0&&dx<=0)||(dz<0&&dx>0))
				{
					u1 = uMin;
					u0 = uMax;
				}
				worldrenderer.pos(v0.xCoord, v0.yCoord+radius, v0.zCoord).tex(u0,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord, v1.yCoord+radius, v1.zCoord).tex(u1,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord, v1.yCoord-radius, v1.zCoord).tex(u1,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord, v0.yCoord-radius, v0.zCoord).tex(u0,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v1.xCoord, v1.yCoord+radius, v1.zCoord).tex(u1,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord, v0.yCoord+radius, v0.zCoord).tex(u0,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord, v0.yCoord-radius, v0.zCoord).tex(u0,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord, v1.yCoord-radius, v1.zCoord).tex(u1,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v0.xCoord-radius*rmodx, v0.yCoord, v0.zCoord+radius*rmodz).tex(u0,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord-radius*rmodx, v1.yCoord, v1.zCoord+radius*rmodz).tex(u1,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord+radius*rmodx, v1.yCoord, v1.zCoord-radius*rmodz).tex(u1,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord+radius*rmodx, v0.yCoord, v0.zCoord-radius*rmodz).tex(u0,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v1.xCoord-radius*rmodx, v1.yCoord, v1.zCoord+radius*rmodz).tex(u1,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord-radius*rmodx, v0.yCoord, v0.zCoord+radius*rmodz).tex(u0,vMax).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord+radius*rmodx, v0.yCoord, v0.zCoord-radius*rmodz).tex(u0,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord+radius*rmodx, v1.yCoord, v1.zCoord-radius*rmodz).tex(u1,vMin).color(rgba[0],rgba[1],rgba[2], rgba[3]).endVertex();

			}
		}
		//		tes.setColorRGBA_I(0xffffff, 0xff);
	}
	//
	//	public static int calcBrightness(IBlockAccess world, double x, double y, double z)
	//	{
	//		return world.getLightBrightnessForSkyBlocks((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z), 0);
	//	}
	//
	//
	//	public static void tessellateBox(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, IIcon icon)
	//	{
	//		tes().addVertexWithUV(xMin,yMin,zMax, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(zMax*16));
	//		tes().addVertexWithUV(xMin,yMin,zMin, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(zMin*16));
	//		tes().addVertexWithUV(xMax,yMin,zMin, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(zMin*16));
	//		tes().addVertexWithUV(xMax,yMin,zMax, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(zMax*16));
	//
	//		tes().addVertexWithUV(xMin,yMax,zMin, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(zMin*16));
	//		tes().addVertexWithUV(xMin,yMax,zMax, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(zMax*16));
	//		tes().addVertexWithUV(xMax,yMax,zMax, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(zMax*16));
	//		tes().addVertexWithUV(xMax,yMax,zMin, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(zMin*16));
	//
	//		tes().addVertexWithUV(xMax,yMin,zMin, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMin,yMin,zMin, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMin,yMax,zMin, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(yMin*16));
	//		tes().addVertexWithUV(xMax,yMax,zMin, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(yMin*16));
	//
	//		tes().addVertexWithUV(xMin,yMin,zMax, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMax,yMin,zMax, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMax,yMax,zMax, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(yMin*16));
	//		tes().addVertexWithUV(xMin,yMax,zMax, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(yMin*16));
	//
	//		tes().addVertexWithUV(xMin,yMin,zMin, icon.getInterpolatedU(zMin*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMin,yMin,zMax, icon.getInterpolatedU(zMax*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMin,yMax,zMax, icon.getInterpolatedU(zMax*16),icon.getInterpolatedV(yMin*16));
	//		tes().addVertexWithUV(xMin,yMax,zMin, icon.getInterpolatedU(zMin*16),icon.getInterpolatedV(yMin*16));
	//
	//		tes().addVertexWithUV(xMax,yMin,zMax, icon.getInterpolatedU(zMax*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMax,yMin,zMin, icon.getInterpolatedU(zMin*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMax,yMax,zMin, icon.getInterpolatedU(zMin*16),icon.getInterpolatedV(yMin*16));
	//		tes().addVertexWithUV(xMax,yMax,zMax, icon.getInterpolatedU(zMax*16),icon.getInterpolatedV(yMin*16));
	//	}
	//

	public static Tessellator tes()
	{
		return Tessellator.getInstance();
	}
	public static Minecraft mc()
	{
		return Minecraft.getMinecraft();
	}
	public static void bindTexture(String path)
	{
		mc().getTextureManager().bindTexture(getResource(path));
	}
	public static void bindAtlas()
	{
		mc().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
	}
	public static ResourceLocation getResource(String path)
	{
		ResourceLocation rl = resourceMap.containsKey(path) ? resourceMap.get(path) : new ResourceLocation(path);
		if(!resourceMap.containsKey(path))
			resourceMap.put(path, rl);
		return rl;
	}
	public static TextureAtlasSprite getSprite(ResourceLocation rl)
	{
		return mc().getTextureMapBlocks().getAtlasSprite(rl.toString());
	}
	//	public static WavefrontObject getModel(String path)
	//	{
	//		ResourceLocation rl = resourceMap.containsKey(path) ? resourceMap.get(path) : new ResourceLocation(path);
	//		if(!resourceMap.containsKey(path))
	//			resourceMap.put(path, rl);
	//		return (WavefrontObject)AdvancedModelLoader.loadModel(rl);
	//	}
	public static FontRenderer font()
	{
		return mc().fontRendererObj;
	}
	public static Timer timer()
	{
		return mc().timer;
	}

	public static enum TimestampFormat
	{
		D,
		H,
		M,
		S,
		MS,
		HMS,
		HM,
		DHMS,
		DHM,
		DH;
		static TimestampFormat[] coreValues = {TimestampFormat.D,TimestampFormat.H,TimestampFormat.M,TimestampFormat.S}; 

		public boolean containsFormat(TimestampFormat format)
		{
			return this.toString().contains(format.toString());
		}
		public long getTickCut()
		{
			return this==D?1728000L: this==H?72000L: this==M?1200L: this==S?20L: 1;
		}
		public String getLocalKey()
		{
			return this==D?"day": this==H?"hour": this==M?"minute": this==S?"second": "";
		}
	}
	public static String fomatTimestamp(long timestamp, TimestampFormat format)
	{
		String s = "";
		for(TimestampFormat core : TimestampFormat.coreValues)
			if(format.containsFormat(core) && timestamp>=core.getTickCut())
			{
				s += StatCollector.translateToLocalFormatted(Lib.DESC_INFO+core.getLocalKey(), Long.toString(timestamp/core.getTickCut()));
				timestamp %= core.getTickCut();
			}
		return s;
	}

	//	public static String getResourceNameForItemStack(ItemStack stack)
	//	{
	//		if(stack!=null)
	//		{
	//			IIcon ic = null;
	//			Block b = Block.getBlockFromItem(stack.getItem());
	//			if(b!=null&&b!=Blocks.air)
	//				ic = b.getIcon(2, stack.getItemDamage());
	//			else
	//				ic = stack.getIconIndex();
	//			if(ic!=null)
	//			{
	//				String name = ic.getIconName();
	//				String resource = "";
	//				String icon = "";
	//				if(name.indexOf(":")>0)
	//				{
	//					String[] split = name.split(":",2);
	//					resource = split[0]+":";
	//					icon = split[1];
	//				}
	//				else
	//					icon = name;
	//				return resource + "textures/" + (stack.getItemSpriteNumber()==0?"blocks":"items") + "/" + icon+ ".png";
	//			}
	//		}
	//		return "";
	//	}

	static int[] chatColours = {
			0x000000,//BLACK
			0x0000AA,//DARK_BLUE
			0x00AA00,//DARK_GREEN
			0x00AAAA,//DARK_AQUA
			0xAA0000,//DARK_RED
			0xAA00AA,//DARK_PURPLE
			0xFFAA00,//GOLD
			0xAAAAAA,//GRAY
			0x555555,//DARK_GRAY
			0x5555FF,//BLUE
			0x55FF55,//GREEN
			0x55FFFF,//AQUA
			0xFF5555,//RED
			0xFF55FF,//LIGHT_PURPLE
			0xFFFF55,//YELLOW
			0xFFFFFF//WHITE
	};
	public static int getFormattingColour(EnumChatFormatting rarityColor)
	{
		return rarityColor.ordinal()<16?chatColours[rarityColor.ordinal()]:0;
	}

	public static int getDarkenedTextColour(int colour)
	{
		int r = (colour>>16&255)/4;
		int g = (colour>>8&255)/4;
		int b = (colour&255)/4;
		return r<<16 | g<< 8 | b;
	}

	public static IETileSound generatePositionedIESound(String soundName, float volume, float pitch, boolean repeat, int delay, BlockPos pos)
	{
		IETileSound sound = new IETileSound(new ResourceLocation(soundName), volume,pitch, repeat,delay, pos, AttenuationType.LINEAR);
		sound.evaluateVolume();
		ClientUtils.mc().getSoundHandler().playSound(sound);
		return sound;
	}

	public static ModelRenderer[] copyModelRenderers(ModelBase model, ModelRenderer... oldRenderers)
	{
		ModelRenderer[] newRenderers = new ModelRenderer[oldRenderers.length];
		for(int i=0; i<newRenderers.length; i++)
			if(oldRenderers[i]!=null)
			{
				newRenderers[i] = new ModelRenderer(model, oldRenderers[i].boxName);
				int toX = oldRenderers[i].textureOffsetX;
				int toY = oldRenderers[i].textureOffsetY;
				newRenderers[i].setTextureOffset(toX,toY);
				newRenderers[i].mirror = oldRenderers[i].mirror;
				ArrayList<ModelBox> newCubes = new ArrayList<ModelBox>();
				for(ModelBox cube :  (List<ModelBox>)oldRenderers[i].cubeList)
					newCubes.add(new ModelBox(newRenderers[i],toX,toY, cube.posX1,cube.posY1,cube.posZ1, (int)(cube.posX2-cube.posX1),(int)(cube.posY2-cube.posY1),(int)(cube.posZ2-cube.posZ1), 0));
				newRenderers[i].cubeList = newCubes;
				newRenderers[i].setRotationPoint(oldRenderers[i].rotationPointX,oldRenderers[i].rotationPointY,oldRenderers[i].rotationPointZ);
				newRenderers[i].rotateAngleX = oldRenderers[i].rotateAngleX;
				newRenderers[i].rotateAngleY = oldRenderers[i].rotateAngleY;
				newRenderers[i].rotateAngleZ = oldRenderers[i].rotateAngleZ;
				newRenderers[i].offsetX = oldRenderers[i].offsetX;
				newRenderers[i].offsetY = oldRenderers[i].offsetY;
				newRenderers[i].offsetZ = oldRenderers[i].offsetZ;
			}
		return newRenderers;
	}

	//	public static void handleStaticTileRenderer(TileEntity tile)
	//	{
	//		handleStaticTileRenderer(tile,true);
	//	}
	//	public static void handleStaticTileRenderer(TileEntity tile, boolean translate)
	//	{
	//		TileEntitySpecialRenderer tesr = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile);
	//		if(tesr instanceof TileRenderIE)
	//		{
	//			Matrix4 matrixT = new Matrix4();
	//			if(translate)
	//				matrixT.translate(tile.xCoord, tile.yCoord, tile.zCoord);
	//			((TileRenderIE)tesr).renderStatic(tile, Tessellator.instance, matrixT, new Matrix4());
	//		}
	//	}
	//
	//	public static void renderStaticWavefrontModel(TileEntity tile, WavefrontObject model, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, int offsetLighting, boolean invertFaces, String... renderedParts) {
	//		renderStaticWavefrontModel(tile, model, tes, translationMatrix, rotationMatrix, offsetLighting, invertFaces, 1, 1, 1, renderedParts);
	//	}
	//
	//	/**
	//	 * A big "Thank you!" to AtomicBlom and Rorax for helping me figure this one out =P
	//	 */
	//	public static void renderStaticWavefrontModel(TileEntity tile, WavefrontObject model, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, int offsetLighting, boolean invertFaces, float colR, float colG, float colB, String... renderedParts)
	//	{
	//		renderStaticWavefrontModel(tile.getWorldObj(),tile.xCoord,tile.yCoord,tile.zCoord, model, tes, translationMatrix, rotationMatrix, offsetLighting, invertFaces, colR,colG,colB, renderedParts);
	//	}
	//	public static void renderStaticWavefrontModel(IBlockAccess world, int x, int y, int z, WavefrontObject model, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, int offsetLighting, boolean invertFaces, float colR, float colG, float colB, String... renderedParts)
	//	{
	//		if(world!=null)
	//		{
	//			int lb = world.getLightBrightnessForSkyBlocks(x, y, z, 0);
	//			int lb_j = lb % 65536;
	//			int lb_k = lb / 65536;
	//			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lb_j / 1.0F, (float)lb_k / 1.0F);
	//		}
	//		Vertex vertexCopy = new Vertex(0,0,0);
	//		Vertex normalCopy = new Vertex(0,0,0);
	//
	//		for(GroupObject groupObject : model.groupObjects)
	//		{
	//			boolean render = false;
	//			if(renderedParts==null || renderedParts.length<1)
	//				render = true;
	//			else
	//				for(String s : renderedParts)
	//					if(groupObject.name.equalsIgnoreCase(s))
	//						render = true;
	//			if(render)
	//				for(Face face : groupObject.faces)
	//				{
	//					if(face.faceNormal == null)
	//						face.faceNormal = face.calculateFaceNormal();
	//
	//					normalCopy.x = face.faceNormal.x;
	//					normalCopy.y = face.faceNormal.y;
	//					normalCopy.z = face.faceNormal.z;
	//					rotationMatrix.apply(normalCopy);
	//					float biggestNormal = Math.max(Math.abs(normalCopy.y), Math.max(Math.abs(normalCopy.x),Math.abs(normalCopy.z)));
	//					int side = biggestNormal==Math.abs(normalCopy.y)?(normalCopy.y<0?0:1): biggestNormal==Math.abs(normalCopy.z)?(normalCopy.z<0?2:3): (normalCopy.x<0?4:5);
	//
	//					BlockLightingInfo faceLight = null;
	//					if(offsetLighting==0 && world!=null)
	//						faceLight = calculateBlockLighting(side, world, world.getBlock(x,y,z), x,y,z, colR,colG,colB, standardBlockAABB);
	//					else if(offsetLighting==1 && world!=null)
	//					{
	//						double faceMinX = face.vertices[0].x;
	//						double faceMinY = face.vertices[0].y;
	//						double faceMinZ = face.vertices[0].z;
	//						double faceMaxX = face.vertices[0].x;
	//						double faceMaxY = face.vertices[0].y;
	//						double faceMaxZ = face.vertices[0].z;
	//						for(int i=1; i<face.vertices.length; ++i)
	//						{
	//							faceMinX = Math.min(faceMinX, face.vertices[i].x);
	//							faceMinY = Math.min(faceMinY, face.vertices[i].y);
	//							faceMinZ = Math.min(faceMinZ, face.vertices[i].z);
	//							faceMaxX = Math.max(faceMaxX, face.vertices[i].x);
	//							faceMaxY = Math.max(faceMaxY, face.vertices[i].y);
	//							faceMaxZ = Math.max(faceMaxZ, face.vertices[i].z);
	//						}
	//						faceLight = calculateBlockLighting(side, world, world.getBlock(x, y, z), x,y,z, colR,colG,colB, AxisAlignedBB.getBoundingBox(faceMinX,faceMinY,faceMinZ, faceMaxX,faceMaxY,faceMaxZ));
	//					}
	//
	//					tes.setNormal(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z);
	//
	//					for(int i=0; i<face.vertices.length; ++i)
	//					{
	//						int target = !invertFaces?i:(face.vertices.length-1-i);
	//						int corner = (int)(target/(float)face.vertices.length*4);
	//						Vertex vertex = face.vertices[target];
	//						vertexCopy.x = vertex.x;
	//						vertexCopy.y = vertex.y;
	//						vertexCopy.z = vertex.z;
	//						rotationMatrix.apply(vertexCopy);
	//						translationMatrix.apply(vertexCopy);
	//						if(faceLight!=null)
	//						{
	//							tes.setBrightness(corner==0?faceLight.brightnessTopLeft: corner==1?faceLight.brightnessBottomLeft: corner==2?faceLight.brightnessBottomRight: faceLight.brightnessTopRight);
	//							float r = corner==0?faceLight.colorRedTopLeft: corner==1?faceLight.colorRedBottomLeft: corner==2?faceLight.colorRedBottomRight: faceLight.colorRedTopRight;
	//							float g = corner==0?faceLight.colorGreenTopLeft: corner==1?faceLight.colorGreenBottomLeft: corner==2?faceLight.colorGreenBottomRight: faceLight.colorGreenTopRight;
	//							float b = corner==0?faceLight.colorBlueTopLeft: corner==1?faceLight.colorBlueBottomLeft: corner==2?faceLight.colorBlueBottomRight: faceLight.colorBlueTopRight;
	//							tes.setColorOpaque_F(r, g, b);
	//						}
	//						else if(world!=null)
	//						{
	//							tes.setBrightness(0xb000b0);
	//							tes.setColorOpaque_F(colR,colG,colB);
	//						}
	//
	//						if(face.textureCoordinates!=null && face.textureCoordinates.length>0)
	//						{
	//							TextureCoordinate textureCoordinate = face.textureCoordinates[target];
	//							tes.addVertexWithUV(vertexCopy.x, vertexCopy.y, vertexCopy.z, textureCoordinate.u, textureCoordinate.v);
	//						}
	//						else
	//						{
	//							tes.addVertex(vertexCopy.x, vertexCopy.y, vertexCopy.z);
	//						}
	//					}
	//				}
	//		}
	//	}
	//	public static void renderStaticWavefrontModelWithIcon(TileEntity tile, WavefrontObject model, IIcon icon, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, int offsetLighting, boolean invertFaces, float colR, float colG, float colB, String... renderedParts)
	//	{
	//		renderStaticWavefrontModelWithIcon(tile.getWorldObj(),tile.xCoord,tile.yCoord,tile.zCoord, model, icon, tes, translationMatrix, rotationMatrix, offsetLighting, invertFaces, colR,colG,colB, renderedParts);
	//	}
	//	public static void renderStaticWavefrontModelWithIcon(IBlockAccess world, int x, int y, int z, WavefrontObject model, IIcon icon, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, int offsetLighting, boolean invertFaces, float colR, float colG, float colB, String... renderedParts)
	//	{
	//		if(icon==null)
	//			return;
	//		if(world!=null)
	//		{
	//			int lb = world.getLightBrightnessForSkyBlocks(x, y, z, 0);
	//			int lb_j = lb % 65536;
	//			int lb_k = lb / 65536;
	//			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lb_j / 1.0F, (float)lb_k / 1.0F);
	//		}
	//		Vertex normalCopy = new Vertex(0,0,0);
	//
	//		float minU = icon.getInterpolatedU(0);
	//		float sizeU = icon.getInterpolatedU(16) - minU;
	//		float minV = icon.getInterpolatedV(0);
	//		float sizeV = icon.getInterpolatedV(16) - minV;
	//		float baseOffsetU = (16f/icon.getIconWidth())*.0005F;
	//		float baseOffsetV = (16f/icon.getIconHeight())*.0005F;
	//
	//		for(GroupObject groupObject : model.groupObjects)
	//		{
	//			boolean render = false;
	//			if(renderedParts==null || renderedParts.length<1)
	//				render = true;
	//			else
	//				for(String s : renderedParts)
	//					if(groupObject.name.equalsIgnoreCase(s))
	//						render = true;
	//			if(render)
	//				for(Face face : groupObject.faces)
	//				{
	//					if(face.faceNormal == null)
	//						face.faceNormal = face.calculateFaceNormal();
	//
	//					normalCopy.x = face.faceNormal.x;
	//					normalCopy.y = face.faceNormal.y;
	//					normalCopy.z = face.faceNormal.z;
	//					rotationMatrix.apply(normalCopy);
	//					float biggestNormal = Math.max(Math.abs(normalCopy.y), Math.max(Math.abs(normalCopy.x),Math.abs(normalCopy.z)));
	//					int side = biggestNormal==Math.abs(normalCopy.y)?(normalCopy.y<0?0:1): biggestNormal==Math.abs(normalCopy.z)?(normalCopy.z<0?2:3): (normalCopy.x<0?4:5);
	//
	//					BlockLightingInfo faceLight = null;
	//					if(offsetLighting==0 && world!=null)
	//						faceLight = calculateBlockLighting(side, world, world.getBlock(x,y,z), x,y,z, colR,colG,colB, standardBlockAABB);
	//					else if(offsetLighting==1 && world!=null)
	//					{
	//						double faceMinX = face.vertices[0].x;
	//						double faceMinY = face.vertices[0].y;
	//						double faceMinZ = face.vertices[0].z;
	//						double faceMaxX = face.vertices[0].x;
	//						double faceMaxY = face.vertices[0].y;
	//						double faceMaxZ = face.vertices[0].z;
	//						for(int i=1; i<face.vertices.length; ++i)
	//						{
	//							faceMinX = Math.min(faceMinX, face.vertices[i].x);
	//							faceMinY = Math.min(faceMinY, face.vertices[i].y);
	//							faceMinZ = Math.min(faceMinZ, face.vertices[i].z);
	//							faceMaxX = Math.max(faceMaxX, face.vertices[i].x);
	//							faceMaxY = Math.max(faceMaxY, face.vertices[i].y);
	//							faceMaxZ = Math.max(faceMaxZ, face.vertices[i].z);
	//						}
	//						faceLight = calculateBlockLighting(side, world, world.getBlock(x, y, z), x,y,z, colR,colG,colB, AxisAlignedBB.getBoundingBox(faceMinX,faceMinY,faceMinZ, faceMaxX,faceMaxY,faceMaxZ));
	//					}
	//
	//					tes.setNormal(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z);
	//
	//					float averageU = 0F;
	//					float averageV = 0F;
	//					if(face.textureCoordinates!=null && face.textureCoordinates.length>0)
	//					{
	//						for(int i=0; i<face.textureCoordinates.length; ++i)
	//						{
	//							averageU += face.textureCoordinates[i].u;
	//							averageV += face.textureCoordinates[i].v;
	//						}
	//						averageU = averageU / face.textureCoordinates.length;
	//						averageV = averageV / face.textureCoordinates.length;
	//					}
	//
	//					TextureCoordinate[] oldUVs = new TextureCoordinate[face.textureCoordinates.length];
	//					for(int v=0; v<face.vertices.length; ++v)
	//					{
	//						float offsetU, offsetV;
	//						offsetU = baseOffsetU;
	//						offsetV = baseOffsetV;
	//						if (face.textureCoordinates[v].u > averageU)
	//							offsetU = -offsetU;
	//						if (face.textureCoordinates[v].v > averageV)
	//							offsetV = -offsetV;
	//
	//						oldUVs[v] = face.textureCoordinates[v];
	//						TextureCoordinate textureCoordinate = face.textureCoordinates[v];
	//						face.textureCoordinates[v] = new TextureCoordinate(
	//								minU + sizeU * (textureCoordinate.u+offsetU),
	//								minV + sizeV * (textureCoordinate.v+offsetV)
	//								);
	//					}
	//					addFaceToWorldRender(face, tes, faceLight, translationMatrix, rotationMatrix, invertFaces, colR, colG, colB);
	//					for(int v=0; v<face.vertices.length; ++v)
	//						face.textureCoordinates[v] = new TextureCoordinate(oldUVs[v].u,oldUVs[v].v);
	//				}
	//		}
	//	}
	//	public static void renderWavefrontModelWithModifications(WavefrontObject model, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, boolean flipTextureU, String... renderedParts)
	//	{
	//		Vertex vertexCopy = new Vertex(0,0,0);
	//		Vertex normalCopy = new Vertex(0,0,0);
	//
	//		for(GroupObject groupObject : model.groupObjects)
	//		{
	//			boolean render = false;
	//			if(renderedParts==null || renderedParts.length<1)
	//				render = true;
	//			else
	//				for(String s : renderedParts)
	//					if(groupObject.name.equalsIgnoreCase(s))
	//						render = true;
	//			if(render)
	//				for(Face face : groupObject.faces)
	//				{
	//					if(face.faceNormal == null)
	//						face.faceNormal = face.calculateFaceNormal();
	//
	//					normalCopy.x = face.faceNormal.x;
	//					normalCopy.y = face.faceNormal.y;
	//					normalCopy.z = face.faceNormal.z;
	//					rotationMatrix.apply(normalCopy);
	//
	//
	//					tes.setNormal(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z);
	//					for(int i=0; i<face.vertices.length; ++i)
	//					{
	//						Vertex vertex = face.vertices[i];
	//						vertexCopy.x = vertex.x;
	//						vertexCopy.y = vertex.y;
	//						vertexCopy.z = vertex.z;
	//						rotationMatrix.apply(vertexCopy);
	//						translationMatrix.apply(vertexCopy);
	//
	//						if((face.textureCoordinates != null) && (face.textureCoordinates.length > 0))
	//						{
	//							TextureCoordinate textureCoordinate = face.textureCoordinates[flipTextureU?(face.textureCoordinates.length-1)-i: i];
	//							tes.addVertexWithUV(vertexCopy.x, vertexCopy.y, vertexCopy.z, textureCoordinate.u, textureCoordinate.v);
	//						}
	//						else
	//							tes.addVertex(vertexCopy.x, vertexCopy.y, vertexCopy.z);
	//					}
	//				}
	//		}
	//	}
	//
	//	public static void renderWavefrontWithIconUVs(WavefrontObject model, IIcon icon, String... parts)
	//	{
	//		renderWavefrontWithIconUVs(model, GL11.GL_QUADS, icon, parts);
	//		renderWavefrontWithIconUVs(model, GL11.GL_TRIANGLES, icon, parts);
	//	}
	//
	//	public static void renderWavefrontWithIconUVs(WavefrontObject model, int glDrawingMode, IIcon icon, String... parts)
	//	{
	//		if(icon==null)
	//			return;
	//		List<String> renderParts = Arrays.asList(parts);
	//		tes().startDrawing(glDrawingMode);
	//		for(GroupObject go : model.groupObjects)
	//			if(go.glDrawingMode==glDrawingMode)
	//				if(renderParts.contains(go.name))
	//					tessellateWavefrontGroupObjectWithIconUVs(go, icon);
	//		tes().draw();
	//	}
	//	public static void tessellateWavefrontGroupObjectWithIconUVs(GroupObject object, IIcon icon)
	//	{
	//		if(icon==null)
	//			return;
	//
	//		float minU = icon.getInterpolatedU(0);
	//		float sizeU = icon.getInterpolatedU(16) - minU;
	//		float minV = icon.getInterpolatedV(0);
	//		float sizeV = icon.getInterpolatedV(16) - minV;
	//		float baseOffsetU = (16f/icon.getIconWidth())*.0005F;
	//		float baseOffsetV = (16f/icon.getIconHeight())*.0005F;
	//		for(Face face : object.faces)
	//		{
	//			float averageU = 0F;
	//			float averageV = 0F;
	//			if(face.textureCoordinates!=null && face.textureCoordinates.length>0)
	//			{
	//				for(int i=0; i<face.textureCoordinates.length; ++i)
	//				{
	//					averageU += face.textureCoordinates[i].u;
	//					averageV += face.textureCoordinates[i].v;
	//				}
	//				averageU = averageU / face.textureCoordinates.length;
	//				averageV = averageV / face.textureCoordinates.length;
	//			}
	//
	//			TextureCoordinate[] oldUVs = new TextureCoordinate[face.textureCoordinates.length];
	//			for(int v=0; v<face.vertices.length; ++v)
	//			{
	//				float offsetU, offsetV;
	//				offsetU = baseOffsetU;
	//				offsetV = baseOffsetV;
	//				if (face.textureCoordinates[v].u > averageU)
	//					offsetU = -offsetU;
	//				if (face.textureCoordinates[v].v > averageV)
	//					offsetV = -offsetV;
	//
	//				oldUVs[v] = face.textureCoordinates[v];
	//				TextureCoordinate textureCoordinate = face.textureCoordinates[v];
	//				face.textureCoordinates[v] = new TextureCoordinate(
	//						minU + sizeU * (textureCoordinate.u+offsetU),
	//						minV + sizeV * (textureCoordinate.v+offsetV)
	//						);
	//			}
	//			face.addFaceForRender(ClientUtils.tes(),0);
	//			for(int v=0; v<face.vertices.length; ++v)
	//				face.textureCoordinates[v] = new TextureCoordinate(oldUVs[v].u,oldUVs[v].v);
	//		}
	//	}
	//
	//	public static void addFaceToWorldRender(Face face, Tessellator tes, BlockLightingInfo light, Matrix4 translationMatrix, Matrix4 rotationMatrix, boolean invert, float colR, float colG, float colB)
	//	{
	//		Vertex vertexCopy = new Vertex(0,0,0);
	//		for(int i=0; i<face.vertices.length; ++i)
	//		{
	//			int target = !invert?i:(face.vertices.length-1-i);
	//			int corner = (int)(target/(float)face.vertices.length*4);
	//			Vertex vertex = face.vertices[target];
	//			vertexCopy.x = vertex.x;
	//			vertexCopy.y = vertex.y;
	//			vertexCopy.z = vertex.z;
	//			rotationMatrix.apply(vertexCopy);
	//			translationMatrix.apply(vertexCopy);
	//			if(light!=null)
	//			{
	//				tes.setBrightness(corner==0?light.brightnessTopLeft: corner==1?light.brightnessBottomLeft: corner==2?light.brightnessBottomRight: light.brightnessTopRight);
	//				float r = corner==0?light.colorRedTopLeft: corner==1?light.colorRedBottomLeft: corner==2?light.colorRedBottomRight: light.colorRedTopRight;
	//				float g = corner==0?light.colorGreenTopLeft: corner==1?light.colorGreenBottomLeft: corner==2?light.colorGreenBottomRight: light.colorGreenTopRight;
	//				float b = corner==0?light.colorBlueTopLeft: corner==1?light.colorBlueBottomLeft: corner==2?light.colorBlueBottomRight: light.colorBlueTopRight;
	//				tes.setColorOpaque_F(r, g, b);
	//			}
	//			else
	//			{
	//				tes.setBrightness(0xb000b0);
	//				tes.setColorOpaque_F(colR,colG,colB);
	//			}
	//
	//			if(face.textureCoordinates!=null && face.textureCoordinates.length>0)
	//			{
	//				TextureCoordinate textureCoordinate = face.textureCoordinates[target];
	//				tes.addVertexWithUV(vertexCopy.x, vertexCopy.y, vertexCopy.z, textureCoordinate.u, textureCoordinate.v);
	//			}
	//			else
	//			{
	//				tes.addVertex(vertexCopy.x, vertexCopy.y, vertexCopy.z);
	//			}
	//		}
	//	}
	//
	//
	//	public static void renderItemIn2D(IIcon icon, double[] uv, int width, int height, float depth)
	//	{
	//		double uMin = icon.getInterpolatedU(uv[0]*16);
	//		double uMax = icon.getInterpolatedU(uv[1]*16);
	//		double vMin = icon.getInterpolatedV(uv[2]*16);
	//		double vMax = icon.getInterpolatedV(uv[3]*16);
	//
	//		double w = width/16d/2;
	//		double h = height/16d;
	//		tes().startDrawingQuads();
	//		tes().setNormal(0.0F, 0.0F, 1.0F);
	//		tes().addVertexWithUV(-w, 0, 0.0D, uMin, vMax);
	//		tes().addVertexWithUV( w, 0, 0.0D, uMax, vMax);
	//		tes().addVertexWithUV( w, h, 0.0D, uMax, vMin);
	//		tes().addVertexWithUV(-w, h, 0.0D, uMin, vMin);
	//		tes().draw();
	//		tes().startDrawingQuads();
	//		tes().setNormal(0.0F, 0.0F, -1.0F);
	//		tes().addVertexWithUV(-w, h, (0.0F - depth), uMin, vMin);
	//		tes().addVertexWithUV( w, h, (0.0F - depth), uMax, vMin);
	//		tes().addVertexWithUV( w, 0, (0.0F - depth), uMax, vMax);
	//		tes().addVertexWithUV(-w, 0, (0.0F - depth), uMin, vMax);
	//		tes().draw();
	//		double f5 = 0.5F * (uMin - uMax) / (float)width;
	//		double f6 = 0.5F * (vMax - vMin) / (float)height;
	//		int k;
	//		double f7;
	//		double f8;
	//		double f9;
	//
	//		tes().startDrawingQuads();
	//		tes().setNormal(0.0F, 1.0F, 0.0F);
	//		for(k=0; k<width; k++)
	//		{
	//			f7 = k/(double)width;
	//			f8 = uMin + (uMax - uMin) * f7 - f5;
	//			f9 = k/(double)icon.getIconWidth();
	//			tes().addVertexWithUV(-w+f9, 0, -depth, f8, vMax);
	//			tes().addVertexWithUV(-w+f9, 0, 0, f8, vMax);
	//			tes().addVertexWithUV(-w+f9, h, 0, f8, vMin);
	//			tes().addVertexWithUV(-w+f9, h, -depth, f8, vMin);
	//		}
	//		tes().draw();
	//
	//		tes().startDrawingQuads();
	//		tes().setNormal(1.0F, 0.0F, 0.0F);
	//		for(k=0; k<width; k++)
	//		{
	//			f7 = k/(double)width;
	//			f8 = uMin + (uMax - uMin) * f7 - f5;
	//			f9 = (k+1)/(double)icon.getIconWidth();
	//			tes().addVertexWithUV(-w+f9, h, -depth, f8, vMin);
	//			tes().addVertexWithUV(-w+f9, h, 0, f8, vMin);
	//			tes().addVertexWithUV(-w+f9, 0, 0, f8, vMax);
	//			tes().addVertexWithUV(-w+f9, 0, -depth, f8, vMax);
	//		}
	//		tes().draw();
	//
	//		tes().startDrawingQuads();
	//		tes().setNormal(0.0F, 1.0F, 0.0F);
	//		for (k = 0; k < height; ++k)
	//		{
	//			f7 = k / (double)height;
	//			f8 = vMax + (vMin - vMax) * f7 - f6;
	//			f9 = (k+1)/(double)icon.getIconHeight();
	//			tes().addVertexWithUV(-w, f9, 0, uMin, f8);
	//			tes().addVertexWithUV( w, f9, 0, uMax, f8);
	//			tes().addVertexWithUV( w, f9, -depth, uMax, f8);
	//			tes().addVertexWithUV(-w, f9,- depth, uMin, f8);
	//		}
	//		tes().draw();
	//
	//		tes().startDrawingQuads();
	//		tes().setNormal(0.0F, -1.0F, 0.0F);
	//		for (k = 0; k < height; ++k)
	//		{
	//			f7 = k / (double)height;
	//			f8 = vMax + (vMin - vMax) * f7 - f6;
	//			f9 = k/(double)icon.getIconHeight();
	//			tes().addVertexWithUV( w, f9, 0, uMax, f8);
	//			tes().addVertexWithUV(-w, f9, 0, uMin, f8);
	//			tes().addVertexWithUV(-w, f9, -depth, uMin, f8);
	//			tes().addVertexWithUV( w, f9, -depth, uMax, f8);
	//		}
	//		tes().draw();
	//	}
	//
	//	public static void drawInventoryBlock(Block block, int metadata, RenderBlocks renderer)
	//	{
	//		Tessellator tes = tes();
	//		GL11.glPushMatrix();
	//		GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
	//		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
	//		tes.startDrawingQuads();
	//		tes.setNormal(0.0F, -1.0F, 0.0F);
	//		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
	//		tes.draw();
	//		tes.startDrawingQuads();
	//		tes.setNormal(0.0F, 1.0F, 0.0F);
	//		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
	//		tes.draw();
	//		tes.startDrawingQuads();
	//		tes.setNormal(0.0F, 0.0F, -1.0F);
	//		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
	//		tes.draw();
	//		tes.startDrawingQuads();
	//		tes.setNormal(0.0F, 0.0F, 1.0F);
	//		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
	//		tes.draw();
	//		tes.startDrawingQuads();
	//		tes.setNormal(-1.0F, 0.0F, 0.0F);
	//		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
	//		tes.draw();
	//		tes.startDrawingQuads();
	//		tes.setNormal(1.0F, 0.0F, 0.0F);
	//		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.overrideBlockTexture!=null?renderer.overrideBlockTexture: renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
	//		tes.draw();
	//		GL11.glPopMatrix();
	//	}

	//Cheers boni =P
	public static void drawBlockDamageTexture(Tessellator tessellatorIn, WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, World world, List<BlockPos> blocks)
	{
		double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
		double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
		double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
		TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
		int progress = (int)(Minecraft.getMinecraft().playerController.curBlockDamageMP*10f) - 1; // 0-10
		if(progress < 0)
			return;
		renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		//preRenderDamagedBlocks BEGIN
		GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		GlStateManager.doPolygonOffset(-3.0F, -3.0F);
		GlStateManager.enablePolygonOffset();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
		//preRenderDamagedBlocks END
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRendererIn.setTranslation(-d0, -d1, -d2);
		//		worldRendererIn.markDirty();
		for(BlockPos blockpos : blocks) {
			double d3 = (double)blockpos.getX() - d0;
			double d4 = (double)blockpos.getY() - d1;
			double d5 = (double)blockpos.getZ() - d2;
			Block block = world.getBlockState(blockpos).getBlock();
			TileEntity te = world.getTileEntity(blockpos);
			boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest
					|| block instanceof BlockSign || block instanceof BlockSkull;
			if (!hasBreak) hasBreak = te != null && te.canRenderBreaking();
			if (!hasBreak)
			{
				IBlockState iblockstate = world.getBlockState(blockpos);
				if (iblockstate.getBlock().getMaterial() != Material.air)
				{
					TextureAtlasSprite textureatlassprite = destroyBlockIcons[progress];
					BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
					blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, world);
				}
			}
		}
		tessellatorIn.draw();
		worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
		// postRenderDamagedBlocks BEGIN
		GlStateManager.disableAlpha();
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.disablePolygonOffset();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
		// postRenderDamagedBlocks END
	}

	public static void drawColouredRect(int x, int y, int w, int h, int colour)
	{
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(x, y+h, 0).color(colour>>16&255, colour>>8&255, colour&255, colour>>24&255).endVertex();
		worldrenderer.pos(x+w, y+h, 0).color(colour>>16&255, colour>>8&255, colour&255, colour>>24&255).endVertex();
		worldrenderer.pos(x+w, y, 0).color(colour>>16&255, colour>>8&255, colour&255, colour>>24&255).endVertex();
		worldrenderer.pos(x, y, 0).color(colour>>16&255, colour>>8&255, colour&255, colour>>24&255).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}
	public static void drawGradientRect(int x0, int y0, int x1, int y1, int colour0, int colour1)
	{
		float f = (float)(colour0 >> 24 & 255) / 255.0F;
		float f1 = (float)(colour0 >> 16 & 255) / 255.0F;
		float f2 = (float)(colour0 >> 8 & 255) / 255.0F;
		float f3 = (float)(colour0 & 255) / 255.0F;
		float f4 = (float)(colour1 >> 24 & 255) / 255.0F;
		float f5 = (float)(colour1 >> 16 & 255) / 255.0F;
		float f6 = (float)(colour1 >> 8 & 255) / 255.0F;
		float f7 = (float)(colour1 & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(x1, y0, 0).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(x0, y0, 0).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(x0, y1, 0).color(f5, f6, f7, f4).endVertex();
		worldrenderer.pos(x1, y1, 0).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}
	public static void drawTexturedRect(float x, float y, float w, float h, double... uv)
	{
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y+h, 0).tex(uv[0], uv[3]).endVertex();
		worldrenderer.pos(x+w, y+h, 0).tex(uv[1], uv[3]).endVertex();
		worldrenderer.pos(x+w, y, 0).tex(uv[1], uv[2]).endVertex();
		worldrenderer.pos(x, y, 0).tex(uv[0], uv[2]).endVertex();
		tessellator.draw();
	}
	public static void drawTexturedRect(int x, int y, int w, int h, float picSize, int... uv)
	{
		double[] d_uv = new double[]{uv[0]/picSize,uv[1]/picSize, uv[2]/picSize,uv[3]/picSize};
		drawTexturedRect(x,y,w,h, d_uv);
	}
	public static void drawRepeatedFluidSprite(Fluid fluid, float x, float y, float w, float h)
	{
		bindTexture(TextureMap.locationBlocksTexture.toString());
		TextureAtlasSprite sprite = mc().getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
		if(sprite != null)
		{
			int iW = sprite.getIconWidth();
			int iH = sprite.getIconHeight();
			if(iW>0 && iH>0)
				drawRepeatedSprite(x,y,w,h, iW, iH, sprite.getMinU(),sprite.getMaxU(), sprite.getMinV(),sprite.getMaxV());
		}
	}
	public static void drawRepeatedSprite(float x, float y, float w, float h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax)
	{
		int iterMaxW = (int)(w/iconWidth);
		int iterMaxH = (int)(h/iconHeight);
		float leftoverW = w%iconWidth;
		float leftoverH = h%iconHeight;
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
	//	public static void drawSlot(int x, int y, int w, int h)
	//	{
	//		drawSlot(x,y, w,h, 0xff);
	//	}
	//	public static void drawSlot(int x, int y, int w, int h, int alpha)
	//	{
	//		GuiDraw.drawRect(x+8-w/2  , y+8-h/2-1, w,1, (alpha<<24)+0x373737);
	//		GuiDraw.drawRect(x+8-w/2-1, y+8-h/2-1, 1,h+1, (alpha<<24)+0x373737);
	//		GuiDraw.drawRect(x+8-w/2  , y+8-h/2  , w,h, (alpha<<24)+0x8b8b8b);
	//		GuiDraw.drawRect(x+8-w/2  , y+8+h/2  , w+1,1, (alpha<<24)+0xffffff);
	//		GuiDraw.drawRect(x+8+w/2  , y+8-h/2  , 1,h, (alpha<<24)+0xffffff);
	//	}


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
		drawHoveringText(list, x,y, font, -1,-1);
	}
	public static void drawHoveringText(List<String> list, int x, int y, FontRenderer font, int xSize, int ySize)
	{
		if (!list.isEmpty())
		{
			boolean uni = ClientUtils.font().getUnicodeFlag();
			ClientUtils.font().setUnicodeFlag(false);

			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int k = 0;
			Iterator<String> iterator = list.iterator();
			while(iterator.hasNext())
			{
				String s = iterator.next();
				int l = font.getStringWidth(s);
				if(l > k)
					k = l;
			}

			int j2 = x + 12;
			int k2 = y - 12;
			int i1 = 8;

			boolean shift = false;
			if(xSize>0 && j2 + k > xSize)
			{
				j2 -= 28 + k;
				shift = true;
			}
			if(ySize>0 && k2 + i1 + 6 > ySize)
			{
				k2 = ySize - i1 - 6;
				shift = true;
			}
			if(!shift && mc().currentScreen!=null)
			{
				if (j2 + k > mc().currentScreen.width)
					j2 -= 28 + k;
				if (k2 + i1 + 6 > mc().currentScreen.height)
					k2 = mc().currentScreen.height - i1 - 6;
			}

			if (list.size() > 1)
				i1 += 2 + (list.size() - 1) * 10;
			//            this.zLevel = 300.0F;
			//            this.itemRender.zLevel = 300.0F;
			//            int l = -267386864;
			//            this.drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, l, l);
			//            this.drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, l, l);
			//            this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, l, l);
			//            this.drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, l, l);
			//            this.drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, l, l);
			//            int i1 = 1347420415;
			//            int j1 = (i1 & 16711422) >> 1 | i1 & -16777216;
			//            this.drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, i1, j1);
			//            this.drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, i1, j1);
			//            this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, i1, i1);
			//            this.drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, j1, j1);
			GL11.glTranslated(0, 0, 300);
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
			GL11.glTranslated(0, 0,-300);

			for (int i2 = 0; i2 < list.size(); ++i2)
			{
				String s1 = (String)list.get(i2);
				font.drawStringWithShadow(s1, j2, k2, -1);

				if (i2 == 0)
					k2 += 2;

				k2 += 10;
			}

			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();

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
				drawRepeatedFluidSprite(tank.getFluid().getFluid(), x,y+h-fluidHeight, w, fluidHeight);
				bindTexture(originalTexture);
			}
			int xOff = (w-oW)/2;
			int yOff = (h-oH)/2;
			drawTexturedRect(x+xOff,y+yOff,oW,oH, 256f, oX,oX+oW,oY,oY+oH);
		}
		else
		{
			if(mX>=x&&mX<x+w && mY>=y&&mY<y+h)
			{
				if(tank.getFluid()!=null && tank.getFluid().getFluid()!=null)
					tooltip.add(tank.getFluid().getLocalizedName());
				else
					tooltip.add(StatCollector.translateToLocal("gui.ImmersiveEngineering.empty"));
				tooltip.add(tank.getFluidAmount()+"/"+tank.getCapacity()+"mB");
			}
		}
	}

	public static Quat4d degreeToQuaterion(double x, double y, double z)
	{
		x = Math.toRadians(x);
		y = Math.toRadians(y);
		z = Math.toRadians(z);
		Quat4d qYaw = new Quat4d(0,Math.sin(y/2),0, Math.cos(y/2));
		Quat4d qPitch = new Quat4d(Math.sin(x/2),0,0, Math.cos(x/2));
		Quat4d qRoll = new Quat4d(0,0,Math.sin(z/2), Math.cos(z/2));

		Quat4d quat = qYaw;
		quat.mul(qRoll);
		quat.mul(qPitch);
		return quat;
	}

	public static List<BakedQuad>[] convertConnectionFromBlockstate(IExtendedBlockState s, TextureAtlasSprite t)
	{
		List<BakedQuad>[] ret = new List[2];
		ret[0] = new ArrayList<>();
		ret[1] = new ArrayList<>();
		Set<Connection> conns = s.getValue(IEProperties.CONNECTIONS);
		if (conns == null)
			return ret;
		Vector3f dir = new Vector3f();
		Vector3f cross = new Vector3f();
		Vector3f tmp = new Vector3f();

		BlockPos pos = null;
		Vector3f up = new Vector3f(0, 1, 0);
		for (Connection conn : conns)
		{
			if (pos==null)
				pos = conn.start;
			Vec3[] f = conn.catenaryVertices;
			if (f.length < 1)
				continue;
			int color = conn.cableType.getColour(conn);
			Vector3f start = new Vector3f(conn.start.getX(), conn.start.getY(), conn.start.getZ());
			float radius = (float) (conn.cableType.getRenderDiameter() / 2);
			List<Integer> crossings = new ArrayList<>();
			for (int i = 1;i<f.length;i++)
				if (crossesChunkBoundary(f[i], f[i-1]))
					crossings.add(i);
			int index = crossings.size()/2;
			boolean greater = conn.start.compareTo(conn.end)>0;
			if (crossings.size()%2==0&&greater)
				index--;
			int max = (crossings.size()>0?
					(crossings.get(index)+(greater?1:2)):
						(greater?f.length+1:0));
			for (int i = 1; i < max&&i<f.length; i++)
			{
				boolean fading = i==max-1;
				List<BakedQuad> currList = ret[fading?1:0];
				int j = i - 1;
				Vector3f here = new Vector3f((float) f[i].xCoord+(fading?.0001F:0), (float) f[i].yCoord+(fading?.0001F:0), (float) f[i].zCoord+(fading?.0001F:0));
				Vector3f.sub(here, start, here);
				Vector3f there = new Vector3f((float) f[j].xCoord+(fading?.0001F:0), (float) f[j].yCoord+(fading?.0001F:0), (float) f[j].zCoord+(fading?.0001F:0));
				Vector3f.sub(there, start, there);
				boolean vertical = here.x == there.x && here.z == there.z;
				int[] data;
				int[] dataInv;
				if (!vertical)
				{
					Vector3f.sub(here, there, dir);
					Vector3f.cross(up, dir, cross);
					cross.scale(radius / cross.length());
				}
				else
					cross.set(radius, 0, 0);
				data = new int[28];
				dataInv = new int[28];
				Vector3f.add(here, cross, tmp);
				storeVertexData(data, 0, tmp, t, 0, 0, color, 0);
				storeVertexData(dataInv, 3, tmp, t, 0, 0, color, 0);
				Vector3f.sub(here, cross, tmp);
				storeVertexData(data, 1, tmp, t, 0, 16, color, 0);
				storeVertexData(dataInv, 2, tmp, t, 0, 16, color, 0);
				Vector3f.sub(there, cross, tmp);
				storeVertexData(data, 2, tmp, t, 16, 16, color, 255);
				storeVertexData(dataInv, 1, tmp, t, 16, 16, color, 255);
				Vector3f.add(there, cross, tmp);
				storeVertexData(data, 3, tmp, t, 16, 0, color, 255);
				storeVertexData(dataInv, 0, tmp, t, 16, 0, color, 255);
				currList.add(new SmartLightingQuad(data, -1, EnumFacing.DOWN, pos, false));
				currList.add(new SmartLightingQuad(dataInv, -1, EnumFacing.UP, pos, false));

				data = new int[28];
				dataInv = new int[28];
				if (!vertical)
				{
					Vector3f.cross(cross, dir, cross);
					cross.scale(radius / cross.length());
				}
				else
					cross.set(0, 0, radius);
				Vector3f.add(here, cross, tmp);
				storeVertexData(data, 0, tmp, t, 0, 0, color, 0);
				storeVertexData(dataInv, 3, tmp, t, 0, 0, color, 0);
				Vector3f.sub(here, cross, tmp);
				storeVertexData(data, 1, tmp, t, 0, 16, color, 0);
				storeVertexData(dataInv, 2, tmp, t, 0, 16, color, 0);
				Vector3f.sub(there, cross, tmp);
				storeVertexData(data, 2, tmp, t, 16, 16, color, 255);
				storeVertexData(dataInv, 1, tmp, t, 16, 16, color, 255);
				Vector3f.add(there, cross, tmp);
				storeVertexData(data, 3, tmp, t, 16, 0, color, 255);
				storeVertexData(dataInv, 0, tmp, t, 16, 0, color, 255);
				currList.add(new SmartLightingQuad(data, -1, EnumFacing.WEST, pos, false));
				currList.add(new SmartLightingQuad(dataInv, -1, EnumFacing.EAST, pos, false));

			}
		}
		return ret;
	}

	public static boolean crossesChunkBoundary(Vec3 start, Vec3 end)
	{
		if (((int)Math.floor(start.xCoord/16))!=((int)Math.floor(end.xCoord/16)))
			return true;
		if (((int)Math.floor(start.yCoord/16))!=((int)Math.floor(end.yCoord/16)))
			return true;
		if (((int)Math.floor(start.zCoord/16))!=((int)Math.floor(end.zCoord/16)))
			return true;
		return false;
	}

	private static void storeVertexData(int[] faceData, int storeIndex, Vector3f position, TextureAtlasSprite t, int u,
			int v, int color, int alpha)
	{
		int i = storeIndex * 7;
		faceData[i] = Float.floatToRawIntBits(position.x);
		faceData[i + 1] = Float.floatToRawIntBits(position.y);
		faceData[i + 2] = Float.floatToRawIntBits(position.z);
		faceData[i + 3] = invertRgb(color, alpha);
		faceData[i + 4] = Float.floatToRawIntBits(t.getInterpolatedU(u));
		faceData[i + 5] = Float.floatToRawIntBits(t.getInterpolatedV(v));
	}

	private static int invertRgb(int in, int alpha)
	{
		int ret = in & (255 << 8);
		ret += (in >> 16) & 255;
		ret += (in & 255) << 16;
		ret += alpha<<24;
		return ret;
	}

	public static void renderBox(WorldRenderer wr, double x0, double y0, double z0, double x1, double y1, double z1, double u0, double v0, double u1, double v1)
	{
		wr.pos(x0, y0, z1).tex(u0, v0).endVertex();
		wr.pos(x1, y0, z1).tex(u1, v0).endVertex();
		wr.pos(x1, y1, z1).tex(u1, v1).endVertex();
		wr.pos(x0, y1, z1).tex(u0, v1).endVertex();

		wr.pos(x0, y1, z0).tex(u0, v0).endVertex();
		wr.pos(x1, y1, z0).tex(u1, v0).endVertex();
		wr.pos(x1, y0, z0).tex(u1, v1).endVertex();
		wr.pos(x0, y0, z0).tex(u0, v1).endVertex();


		wr.pos(x0, y0, z0).tex(u0, v0).endVertex();
		wr.pos(x1, y0, z0).tex(u1, v0).endVertex();
		wr.pos(x1, y0, z1).tex(u1, v1).endVertex();
		wr.pos(x0, y0, z1).tex(u0, v1).endVertex();

		wr.pos(x0, y1, z1).tex(u0, v0).endVertex();
		wr.pos(x1, y1, z1).tex(u1, v0).endVertex();
		wr.pos(x1, y1, z0).tex(u1, v1).endVertex();
		wr.pos(x0, y1, z0).tex(u0, v1).endVertex();


		wr.pos(x0, y0, z0).tex(u0, v0).endVertex();
		wr.pos(x0, y0, z1).tex(u1, v0).endVertex();
		wr.pos(x0, y1, z1).tex(u1, v1).endVertex();
		wr.pos(x0, y1, z0).tex(u0, v1).endVertex();

		wr.pos(x1, y1, z0).tex(u0, v0).endVertex();
		wr.pos(x1, y1, z1).tex(u1, v0).endVertex();
		wr.pos(x1, y0, z1).tex(u1, v1).endVertex();
		wr.pos(x1, y0, z0).tex(u0, v1).endVertex();
	}
	public static int intFromRgb(float[] rgb)
	{
		int ret = (int)(255*rgb[0]);
		ret = (ret<<8)+(int)(255*rgb[1]);
		ret = (ret<<8)+(int)(255*rgb[2]);
		return ret;
	}

}