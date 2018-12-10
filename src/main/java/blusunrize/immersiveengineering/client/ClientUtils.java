/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.models.SmartLightingQuad;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.items.ItemChemthrower;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.items.ItemRailgun;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.IEFluid;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Timer;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.Normal;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.vecmath.Quat4d;
import java.util.*;
import java.util.function.Function;

public class ClientUtils
{
	public static final AxisAlignedBB standardBlockAABB = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	static HashMap<String, ResourceLocation> resourceMap = new HashMap<String, ResourceLocation>();
	public static TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];

	//
	//	// MOD SPECIFIC METHODS
	//	public static void renderAttachedConnections(TileEntity tile)
	//	{
	//		if(tile.getworld()!=null && tile instanceof IImmersiveConnectable)
	//		{
	//			Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(tile.getworld(), Utils.toCC(tile));
	//			if(outputs!=null)
	//			{
	//				Iterator<ImmersiveNetHandler.Connection> itCon = outputs.iterator();
	//				while(itCon.hasNext())
	//				{
	//					ImmersiveNetHandler.Connection con = itCon.next();
	//					TileEntity tileEnd = tile.getworld().getTileEntity(con.end.posX,con.end.posY,con.end.posZ);
	//					if(tileEnd instanceof IImmersiveConnectable)
	//						drawConnection(con, (IImmersiveConnectable)tile, Utils.toIIC(tileEnd, tile.getworld()), con.cableType.getIcon(con));
	//				}
	//			}
	//		}
	//	}
	//
	public static void tessellateConnection(Connection connection, IImmersiveConnectable start, IImmersiveConnectable end, TextureAtlasSprite sprite)
	{
		if(connection==null||start==null||end==null)
			return;
		int col = connection.cableType.getColour(connection);
		double r = connection.cableType.getRenderDiameter()/2;
		int[] rgba = new int[]{col >> 16&255, col >> 8&255, col&255, 255};
		tessellateConnection(connection, start, end, rgba, r, sprite);
	}

	public static void tessellateConnection(Connection connection, IImmersiveConnectable start, IImmersiveConnectable end, int[] rgba, double radius, TextureAtlasSprite sprite)
	{
		if(connection==null||start==null||end==null||connection.end==null||connection.start==null)
			return;
		Vec3d startOffset = start.getConnectionOffset(connection);
		Vec3d endOffset = end.getConnectionOffset(connection);
		if(startOffset==null)
			startOffset = new Vec3d(.5, .5, .5);
		if(endOffset==null)
			endOffset = new Vec3d(.5, .5, .5);
		double dx = (connection.end.getX()+endOffset.x)-(connection.start.getX()+startOffset.x);
		double dy = (connection.end.getY()+endOffset.y)-(connection.start.getY()+startOffset.y);
		double dz = (connection.end.getZ()+endOffset.z)-(connection.start.getZ()+startOffset.z);
		double dw = Math.sqrt(dx*dx+dz*dz);
		double d = Math.sqrt(dx*dx+dy*dy+dz*dz);
		World world = ((TileEntity)start).getWorld();
		Tessellator tes = tes();

		double rmodx = dz/dw;
		double rmodz = dx/dw;

		Vec3d[] vertex = connection.getSubVertices(world);
		//		Vec3 initPos = new Vec3(connection.start.getX()+startOffset.xCoord, connection.start.getY()+startOffset.yCoord, connection.start.getZ()+startOffset.zCoord);
		Vec3d initPos = new Vec3d(startOffset.x, startOffset.y, startOffset.z);

		double uMin = sprite.getMinU();
		double uMax = sprite.getMaxU();
		double vMin = sprite.getMinV();
		double vMax = sprite.getMaxV();
		double uD = uMax-uMin;
		boolean vertical = connection.end.getX()==connection.start.getX()&&connection.end.getZ()==connection.start.getZ();
		boolean b = (dx < 0&&dz <= 0)||(dz < 0&&dx <= 0)||(dz < 0&&dx > 0);


		BufferBuilder worldrenderer = tes.getBuffer();
		//		worldrenderer.pos(x, y+h, 0).tex(uv[0], uv[3]).endVertex();
		//		worldrenderer.pos(x+w, y+h, 0).tex(uv[1], uv[3]).endVertex();
		//		worldrenderer.pos(x+w, y, 0).tex(uv[1], uv[2]).endVertex();
		//		worldrenderer.pos(x, y, 0).tex(uv[0], uv[2]).endVertex();
		if(vertical)
		{
			//			double uShift = Math.abs(dy)/ * uD;
			//			worldrenderer.pos(x, y, z)
			worldrenderer.setTranslation(initPos.x, initPos.y, initPos.z);

			//			tes.addVertexWithUV(0-radius, 0, 0, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0-radius, 0, 0).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx-radius, dy, dz, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx-radius, dy, dz).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx+radius, dy, dz, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx+radius, dy, dz).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0+radius, 0, 0, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0+radius, 0, 0).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

			//			tes.addVertexWithUV(dx-radius, dy, dz, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx-radius, dy, dz).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0-radius, 0, 0, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0-radius, 0, 0).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0+radius, 0, 0, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0+radius, 0, 0).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx+radius, dy, dz, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx+radius, dy, dz).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();


			//			tes.addVertexWithUV(0, 0, 0-radius, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0, 0, 0-radius).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz-radius, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx, dy, dz-radius).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz+radius, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx, dy, dz+radius).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0+radius, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0, 0, 0+radius).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

			//			tes.addVertexWithUV(dx, dy, dz-radius, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx, dy, dz-radius).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0-radius, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0, 0, 0-radius).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0+radius, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0, 0, 0+radius).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz+radius, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx, dy, dz+radius).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			worldrenderer.setTranslation(0, 0, 0);
		}
		else
		{
			double u0 = uMin;
			double u1 = uMin;
			for(int i = b?(vertex.length-1): 0; (b?(i >= 0): (i < vertex.length)); i += (b?-1: 1))
			{
				Vec3d v0 = i > 0?vertex[i-1].subtract(connection.start.getX(), connection.start.getY(), connection.start.getZ()): initPos;
				Vec3d v1 = vertex[i].subtract(connection.start.getX(), connection.start.getY(), connection.start.getZ());

				//				double u0 = uMin;
				//				double u1 = uMax;
				u0 = u1;
				u1 = u0+(v0.distanceTo(v1)/d)*uD;
				if((dx < 0&&dz <= 0)||(dz < 0&&dx <= 0)||(dz < 0&&dx > 0))
				{
					u1 = uMin;
					u0 = uMax;
				}
				worldrenderer.pos(v0.x, v0.y+radius, v0.z).tex(u0, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.x, v1.y+radius, v1.z).tex(u1, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.x, v1.y-radius, v1.z).tex(u1, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.x, v0.y-radius, v0.z).tex(u0, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v1.x, v1.y+radius, v1.z).tex(u1, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.x, v0.y+radius, v0.z).tex(u0, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.x, v0.y-radius, v0.z).tex(u0, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.x, v1.y-radius, v1.z).tex(u1, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v0.x-radius*rmodx, v0.y, v0.z+radius*rmodz).tex(u0, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.x-radius*rmodx, v1.y, v1.z+radius*rmodz).tex(u1, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.x+radius*rmodx, v1.y, v1.z-radius*rmodz).tex(u1, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.x+radius*rmodx, v0.y, v0.z-radius*rmodz).tex(u0, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v1.x-radius*rmodx, v1.y, v1.z+radius*rmodz).tex(u1, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.x-radius*rmodx, v0.y, v0.z+radius*rmodz).tex(u0, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.x+radius*rmodx, v0.y, v0.z-radius*rmodz).tex(u0, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.x+radius*rmodx, v1.y, v1.z-radius*rmodz).tex(u1, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

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
		mc().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	public static ResourceLocation getResource(String path)
	{
		ResourceLocation rl = resourceMap.containsKey(path)?resourceMap.get(path): new ResourceLocation(path);
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
		return mc().fontRenderer;
	}

	public static Timer timer()
	{
		return mc().timer;
	}

	public enum TimestampFormat
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
		static TimestampFormat[] coreValues = {TimestampFormat.D, TimestampFormat.H, TimestampFormat.M, TimestampFormat.S};

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
			if(format.containsFormat(core)&&timestamp >= core.getTickCut())
			{
				s += I18n.format(Lib.DESC_INFO+core.getLocalKey(), Long.toString(timestamp/core.getTickCut()));
				timestamp %= core.getTickCut();
			}
		if(s.isEmpty())
			for(int i = TimestampFormat.coreValues.length-1; i >= 0; i--)
				if(format.containsFormat(TimestampFormat.coreValues[i]))
				{
					s = I18n.format(Lib.DESC_INFO+TimestampFormat.coreValues[i].getLocalKey(), 0);
					break;
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

	public static int getFormattingColour(TextFormatting color)
	{
		return color.ordinal() < 16?chatColours[color.ordinal()]: 0;
	}

	public static int getDarkenedTextColour(int colour)
	{
		int r = (colour >> 16&255)/4;
		int g = (colour >> 8&255)/4;
		int b = (colour&255)/4;
		return r<<16|g<<8|b;
	}

	public static IETileSound generatePositionedIESound(SoundEvent soundEvent, float volume, float pitch, boolean repeat, int delay, BlockPos pos)
	{
		IETileSound sound = new IETileSound(soundEvent, volume, pitch, repeat, delay, pos, AttenuationType.LINEAR, SoundCategory.BLOCKS);
//		sound.evaluateVolume();
		ClientUtils.mc().getSoundHandler().playSound(sound);
		return sound;
	}

	public static ModelRenderer[] copyModelRenderers(ModelBase model, ModelRenderer... oldRenderers)
	{
		ModelRenderer[] newRenderers = new ModelRenderer[oldRenderers.length];
		for(int i = 0; i < newRenderers.length; i++)
			if(oldRenderers[i]!=null)
			{
				newRenderers[i] = new ModelRenderer(model, oldRenderers[i].boxName);
				int toX = oldRenderers[i].textureOffsetX;
				int toY = oldRenderers[i].textureOffsetY;
				newRenderers[i].setTextureOffset(toX, toY);
				newRenderers[i].mirror = oldRenderers[i].mirror;
				ArrayList<ModelBox> newCubes = new ArrayList<ModelBox>();
				for(ModelBox cube : oldRenderers[i].cubeList)
					newCubes.add(new ModelBox(newRenderers[i], toX, toY, cube.posX1, cube.posY1, cube.posZ1, (int)(cube.posX2-cube.posX1), (int)(cube.posY2-cube.posY1), (int)(cube.posZ2-cube.posZ1), 0));
				newRenderers[i].cubeList = newCubes;
				newRenderers[i].setRotationPoint(oldRenderers[i].rotationPointX, oldRenderers[i].rotationPointY, oldRenderers[i].rotationPointZ);
				newRenderers[i].rotateAngleX = oldRenderers[i].rotateAngleX;
				newRenderers[i].rotateAngleY = oldRenderers[i].rotateAngleY;
				newRenderers[i].rotateAngleZ = oldRenderers[i].rotateAngleZ;
				newRenderers[i].offsetX = oldRenderers[i].offsetX;
				newRenderers[i].offsetY = oldRenderers[i].offsetY;
				newRenderers[i].offsetZ = oldRenderers[i].offsetZ;
			}
		return newRenderers;
	}

	public static void handleBipedRotations(ModelBiped model, Entity entity)
	{
		if(!Config.IEConfig.fancyItemHolding)
			return;

		if(entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)entity;
			for(EnumHand hand : EnumHand.values())
			{
				ItemStack heldItem = player.getHeldItem(hand);
				if(!heldItem.isEmpty())
				{
					boolean right = (hand==EnumHand.MAIN_HAND)==(player.getPrimaryHand()==EnumHandSide.RIGHT);
					if(heldItem.getItem() instanceof ItemRevolver)
					{
						if(right)
						{
							model.bipedRightArm.rotateAngleX = -1.39626f+model.bipedHead.rotateAngleX;
							model.bipedRightArm.rotateAngleY = -.08726f+model.bipedHead.rotateAngleY;
						}
						else
						{
							model.bipedLeftArm.rotateAngleX = -1.39626f+model.bipedHead.rotateAngleX;
							model.bipedLeftArm.rotateAngleY = .08726f+model.bipedHead.rotateAngleY;
						}
					}
					else if(heldItem.getItem() instanceof ItemDrill||heldItem.getItem() instanceof ItemChemthrower)
					{
						if(right)
						{
							model.bipedLeftArm.rotateAngleX = -.87266f;
							model.bipedLeftArm.rotateAngleY = .52360f;
						}
						else
						{
							model.bipedRightArm.rotateAngleX = -.87266f;
							model.bipedRightArm.rotateAngleY = -0.52360f;
						}
					}
					else if(heldItem.getItem() instanceof ItemRailgun)
					{
						if(right)
							model.bipedRightArm.rotateAngleX = -.87266f;
						else
							model.bipedLeftArm.rotateAngleX = -.87266f;
					}

				}
			}
		}
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
	//		renderStaticWavefrontModel(tile.getworld(),tile.xCoord,tile.yCoord,tile.zCoord, model, tes, translationMatrix, rotationMatrix, offsetLighting, invertFaces, colR,colG,colB, renderedParts);
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
	//		renderStaticWavefrontModelWithIcon(tile.getworld(),tile.xCoord,tile.yCoord,tile.zCoord, model, icon, tes, translationMatrix, rotationMatrix, offsetLighting, invertFaces, colR,colG,colB, renderedParts);
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
	//		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
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
	//		GlStateManager.popMatrix();
	//	}

	//Cheers boni =P
	public static void drawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, World world, Collection<BlockPos> blocks)
	{
		double d0 = entityIn.lastTickPosX+(entityIn.posX-entityIn.lastTickPosX)*(double)partialTicks;
		double d1 = entityIn.lastTickPosY+(entityIn.posY-entityIn.lastTickPosY)*(double)partialTicks;
		double d2 = entityIn.lastTickPosZ+(entityIn.posZ-entityIn.lastTickPosZ)*(double)partialTicks;
		TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
		int progress = (int)(Minecraft.getMinecraft().playerController.curBlockDamageMP*10f)-1; // 0-10
		if(progress < 0)
			return;
		renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
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
		for(BlockPos blockpos : blocks)
		{
			double d3 = (double)blockpos.getX()-d0;
			double d4 = (double)blockpos.getY()-d1;
			double d5 = (double)blockpos.getZ()-d2;
			Block block = world.getBlockState(blockpos).getBlock();
			TileEntity te = world.getTileEntity(blockpos);
			boolean hasBreak = block instanceof BlockChest||block instanceof BlockEnderChest
					||block instanceof BlockSign||block instanceof BlockSkull;
			if(!hasBreak) hasBreak = te!=null&&te.canRenderBreaking();
			if(!hasBreak)
			{
				IBlockState iblockstate = world.getBlockState(blockpos);
				if(iblockstate.getMaterial()!=Material.AIR)
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
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(x, y+h, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.pos(x+w, y+h, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.pos(x+w, y, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.pos(x, y, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void drawGradientRect(int x0, int y0, int x1, int y1, int colour0, int colour1)
	{
		float f = (float)(colour0 >> 24&255)/255.0F;
		float f1 = (float)(colour0 >> 16&255)/255.0F;
		float f2 = (float)(colour0 >> 8&255)/255.0F;
		float f3 = (float)(colour0&255)/255.0F;
		float f4 = (float)(colour1 >> 24&255)/255.0F;
		float f5 = (float)(colour1 >> 16&255)/255.0F;
		float f6 = (float)(colour1 >> 8&255)/255.0F;
		float f7 = (float)(colour1&255)/255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
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
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y+h, 0).tex(uv[0], uv[3]).endVertex();
		worldrenderer.pos(x+w, y+h, 0).tex(uv[1], uv[3]).endVertex();
		worldrenderer.pos(x+w, y, 0).tex(uv[1], uv[2]).endVertex();
		worldrenderer.pos(x, y, 0).tex(uv[0], uv[2]).endVertex();
		tessellator.draw();
	}

	public static void drawTexturedRect(int x, int y, int w, int h, float picSize, int... uv)
	{
		double[] d_uv = new double[]{uv[0]/picSize, uv[1]/picSize, uv[2]/picSize, uv[3]/picSize};
		drawTexturedRect(x, y, w, h, d_uv);
	}

	public static void drawRepeatedFluidSprite(FluidStack fluid, float x, float y, float w, float h)
	{
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE.toString());
		TextureAtlasSprite sprite = mc().getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill(fluid).toString());
		if(sprite!=null)
		{
			int col = fluid.getFluid().getColor(fluid);
			GlStateManager.color((col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1);
			int iW = sprite.getIconWidth();
			int iH = sprite.getIconHeight();
			if(iW > 0&&iH > 0)
				drawRepeatedSprite(x, y, w, h, iW, iH, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
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
		for(int ww = 0; ww < iterMaxW; ww++)
		{
			for(int hh = 0; hh < iterMaxH; hh++)
				drawTexturedRect(x+ww*iconWidth, y+hh*iconHeight, iconWidth, iconHeight, uMin, uMax, vMin, vMax);
			drawTexturedRect(x+ww*iconWidth, y+iterMaxH*iconHeight, iconWidth, leftoverH, uMin, uMax, vMin, (vMin+iconVDif*leftoverHf));
		}
		if(leftoverW > 0)
		{
			for(int hh = 0; hh < iterMaxH; hh++)
				drawTexturedRect(x+iterMaxW*iconWidth, y+hh*iconHeight, leftoverW, iconHeight, uMin, (uMin+iconUDif*leftoverWf), vMin, vMax);
			drawTexturedRect(x+iterMaxW*iconWidth, y+iterMaxH*iconHeight, leftoverW, leftoverH, uMin, (uMin+iconUDif*leftoverWf), vMin, (vMin+iconVDif*leftoverHf));
		}
	}

	public static void drawSlot(int x, int y, int w, int h)
	{
		drawSlot(x, y, w, h, 0xff);
	}

	public static void drawSlot(int x, int y, int w, int h, int alpha)
	{
		drawColouredRect(x+8-w/2, y+8-h/2-1, w, 1, (alpha<<24)+0x373737);
		drawColouredRect(x+8-w/2-1, y+8-h/2-1, 1, h+1, (alpha<<24)+0x373737);
		drawColouredRect(x+8-w/2, y+8-h/2, w, h, (alpha<<24)+0x8b8b8b);
		drawColouredRect(x+8-w/2, y+8+h/2, w+1, 1, (alpha<<24)+0xffffff);
		drawColouredRect(x+8+w/2, y+8-h/2, 1, h, (alpha<<24)+0xffffff);
	}

	public static void drawDarkSlot(int x, int y, int w, int h)
	{
		drawColouredRect(x+8-w/2, y+8-h/2-1, w, 1, 0x77222222);
		drawColouredRect(x+8-w/2-1, y+8-h/2-1, 1, h+1, 0x77222222);
		drawColouredRect(x+8-w/2, y+8-h/2, w, h, 0x77111111);
		drawColouredRect(x+8-w/2, y+8+h/2, w+1, 1, 0x77999999);
		drawColouredRect(x+8+w/2, y+8-h/2, 1, h, 0x77999999);
	}

	public static void renderToolTip(ItemStack stack, int x, int y)
	{
		List list = stack.getTooltip(mc().player, mc().gameSettings.advancedItemTooltips?TooltipFlags.ADVANCED: TooltipFlags.NORMAL);

		for(int k = 0; k < list.size(); ++k)
			if(k==0)
				list.set(k, stack.getRarity().color+(String)list.get(k));
			else
				list.set(k, TextFormatting.GRAY+(String)list.get(k));

		FontRenderer font = stack.getItem().getFontRenderer(stack);
		drawHoveringText(list, x, y, (font==null?font(): font));
	}

	public static void drawHoveringText(List<String> list, int x, int y, FontRenderer font)
	{
		drawHoveringText(list, x, y, font, -1, -1);
	}

	public static void drawHoveringText(List<String> list, int x, int y, FontRenderer font, int xSize, int ySize)
	{
		if(!list.isEmpty())
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

			int j2 = x+12;
			int k2 = y-12;
			int i1 = 8;

			boolean shift = false;
			if(xSize > 0&&j2+k > xSize)
			{
				j2 -= 28+k;
				shift = true;
			}
			if(ySize > 0&&k2+i1+6 > ySize)
			{
				k2 = ySize-i1-6;
				shift = true;
			}
			if(!shift&&mc().currentScreen!=null)
			{
				if(j2+k > mc().currentScreen.width)
					j2 -= 28+k;
				if(k2+i1+6 > mc().currentScreen.height)
					k2 = mc().currentScreen.height-i1-6;
			}

			if(list.size() > 1)
				i1 += 2+(list.size()-1)*10;
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
			GlStateManager.translate(0, 0, 300);
			int j1 = -267386864;
			drawGradientRect(j2-3, k2-4, j2+k+3, k2-3, j1, j1);
			drawGradientRect(j2-3, k2+i1+3, j2+k+3, k2+i1+4, j1, j1);
			drawGradientRect(j2-3, k2-3, j2+k+3, k2+i1+3, j1, j1);
			drawGradientRect(j2-4, k2-3, j2-3, k2+i1+3, j1, j1);
			drawGradientRect(j2+k+3, k2-3, j2+k+4, k2+i1+3, j1, j1);
			int k1 = 1347420415;
			int l1 = ((k1&16711422) >> 1|k1&-16777216);
			drawGradientRect(j2-3, k2-3+1, j2-3+1, k2+i1+3-1, k1, l1);
			drawGradientRect(j2+k+2, k2-3+1, j2+k+3, k2+i1+3-1, k1, l1);
			drawGradientRect(j2-3, k2-3, j2+k+3, k2-3+1, k1, k1);
			drawGradientRect(j2-3, k2+i1+2, j2+k+3, k2+i1+3, l1, l1);
			GlStateManager.translate(0, 0, -300);

			for(int i2 = 0; i2 < list.size(); ++i2)
			{
				String s1 = list.get(i2);
				font.drawStringWithShadow(s1, j2, k2, -1);

				if(i2==0)
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

	public static void handleGuiTank(IFluidTank tank, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, String originalTexture, ArrayList<String> tooltip)
	{
		handleGuiTank(tank.getFluid(), tank.getCapacity(), x, y, w, h, oX, oY, oW, oH, mX, mY, originalTexture, tooltip);
	}

	public static void handleGuiTank(FluidStack fluid, int capacity, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, String originalTexture, ArrayList<String> tooltip)
	{
		if(tooltip==null)
		{
			if(fluid!=null&&fluid.getFluid()!=null)
			{
				int fluidHeight = (int)(h*(fluid.amount/(float)capacity));
				drawRepeatedFluidSprite(fluid, x, y+h-fluidHeight, w, fluidHeight);
				bindTexture(originalTexture);
				GlStateManager.color(1, 1, 1, 1);
			}
			int xOff = (w-oW)/2;
			int yOff = (h-oH)/2;
			drawTexturedRect(x+xOff, y+yOff, oW, oH, 256f, oX, oX+oW, oY, oY+oH);
		}
		else
		{
			if(mX >= x&&mX < x+w&&mY >= y&&mY < y+h)
				addFluidTooltip(fluid, tooltip, capacity);
		}
	}

	public static void addFluidTooltip(FluidStack fluid, List<String> tooltip, int tankCapacity)
	{
		if(fluid!=null&&fluid.getFluid()!=null)
			tooltip.add(fluid.getFluid().getRarity(fluid).color+fluid.getLocalizedName());
		else
			tooltip.add(I18n.format("gui.immersiveengineering.empty"));
		if(fluid!=null&&fluid.getFluid() instanceof IEFluid)
			((IEFluid)fluid.getFluid()).addTooltipInfo(fluid, null, tooltip);

		if(mc().gameSettings.advancedItemTooltips&&fluid!=null)
			if(!GuiScreen.isShiftKeyDown())
				tooltip.add(I18n.format(Lib.DESC_INFO+"holdShiftForInfo"));
			else
			{
				tooltip.add(TextFormatting.DARK_GRAY+"Fluid Registry: "+FluidRegistry.getFluidName(fluid));
				tooltip.add(TextFormatting.DARK_GRAY+"Density: "+fluid.getFluid().getDensity(fluid));
				tooltip.add(TextFormatting.DARK_GRAY+"Temperature: "+fluid.getFluid().getTemperature(fluid));
				tooltip.add(TextFormatting.DARK_GRAY+"Viscosity: "+fluid.getFluid().getViscosity(fluid));
				tooltip.add(TextFormatting.DARK_GRAY+"NBT Data: "+fluid.tag);
			}

		if(tankCapacity > 0)
			tooltip.add(TextFormatting.GRAY.toString()+(fluid!=null?fluid.amount: 0)+"/"+tankCapacity+"mB");
		else
			tooltip.add(TextFormatting.GRAY.toString()+(fluid!=null?fluid.amount: 0)+"mB");
	}

	public static Quat4d degreeToQuaterion(double x, double y, double z)
	{
		x = Math.toRadians(x);
		y = Math.toRadians(y);
		z = Math.toRadians(z);
		Quat4d qYaw = new Quat4d(0, Math.sin(y/2), 0, Math.cos(y/2));
		Quat4d qPitch = new Quat4d(Math.sin(x/2), 0, 0, Math.cos(x/2));
		Quat4d qRoll = new Quat4d(0, 0, Math.sin(z/2), Math.cos(z/2));

		Quat4d quat = qYaw;
		quat.mul(qRoll);
		quat.mul(qPitch);
		return quat;
	}

	private static final Vector3f fadingOffset = new Vector3f(.0001F, .0001F, .0001F);
	private static float[] alphaFirst2Fading = {0, 0, 1, 1};
	private static float[] alphaNoFading = {1, 1, 1, 1};

	public static List<BakedQuad>[] convertConnectionFromBlockstate(IExtendedBlockState s, TextureAtlasSprite t)
	{
		List<BakedQuad>[] ret = new List[2];
		ret[0] = new ArrayList<>();
		ret[1] = new ArrayList<>();
		Set<Connection> conns = s.getValue(IEProperties.CONNECTIONS);
		if(conns==null)
			return ret;
		Vector3f dir = new Vector3f();
		Vector3f cross = new Vector3f();

		Vector3f up = new Vector3f(0, 1, 0);
		BlockPos pos = null;
		for(Connection conn : conns)
		{
			if(pos==null)
				pos = conn.start;
			Vec3d[] f = conn.catenaryVertices;
			if(f==null||f.length < 1)
				continue;
			int color = conn.cableType.getColour(conn);
			float[] rgb = {(color >> 16&255)/255f, (color >> 8&255)/255f, (color&255)/255f, (color >> 24&255)/255f};
			if(rgb[3]==0)
				rgb[3] = 1;
			float radius = (float)(conn.cableType.getRenderDiameter()/2);
			List<Integer> crossings = new ArrayList<>();
			for(int i = 1; i < f.length; i++)
				if(crossesChunkBoundary(f[i], f[i-1], conn.start))
					crossings.add(i);
			int index = crossings.size()/2;
			boolean greater = conn.start.compareTo(conn.end) > 0;
			if(crossings.size()%2==0&&greater)
				index--;
			int max = (crossings.size() > 0?
					(crossings.get(index)+(greater?1: 2)):
					(greater?f.length+1: 0));
			for(int i = 1; i < max&&i < f.length; i++)
			{
				boolean fading = i==max-1;
				List<BakedQuad> curr = ret[fading?1: 0];
				int j = i-1;
				Vector3f here = new Vector3f((float)f[i].x, (float)f[i].y, (float)f[i].z);
				Vector3f there = new Vector3f((float)f[j].x, (float)f[j].y, (float)f[j].z);
				if(fading)
				{
					Vector3f.add(here, fadingOffset, here);
					Vector3f.add(there, fadingOffset, there);
				}
				boolean vertical = here.x==there.x&&here.z==there.z;
				if(!vertical)
				{
					Vector3f.sub(here, there, dir);
					Vector3f.cross(up, dir, cross);
					cross.scale(radius/cross.length());
				}
				else
					cross.set(radius, 0, 0);
				Vector3f[] vertices = {Vector3f.add(here, cross, null),
						Vector3f.sub(here, cross, null),
						Vector3f.sub(there, cross, null),
						Vector3f.add(there, cross, null)};
				curr.add(createSmartLightingBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.DOWN, t, rgb, false, fading?alphaFirst2Fading: alphaNoFading, pos));
				curr.add(createSmartLightingBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.UP, t, rgb, true, fading?alphaFirst2Fading: alphaNoFading, pos));

				if(!vertical)
				{
					Vector3f.cross(cross, dir, cross);
					cross.scale(radius/cross.length());
				}
				else
					cross.set(0, 0, radius);
				vertices = new Vector3f[]{Vector3f.add(here, cross, null),
						Vector3f.sub(here, cross, null),
						Vector3f.sub(there, cross, null),
						Vector3f.add(there, cross, null)};
				curr.add(createSmartLightingBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.WEST, t, rgb, false, fading?alphaFirst2Fading: alphaNoFading, pos));
				curr.add(createSmartLightingBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.EAST, t, rgb, true, fading?alphaFirst2Fading: alphaNoFading, pos));
			}
		}
		return ret;
	}

	private static void storeVertexData(int[] faceData, int storeIndex, Vector3f position, TextureAtlasSprite t, int u,
										int v, int color)
	{
		int i = storeIndex*7;
		faceData[i] = Float.floatToRawIntBits(position.x);
		faceData[i+1] = Float.floatToRawIntBits(position.y);
		faceData[i+2] = Float.floatToRawIntBits(position.z);
		faceData[i+3] = invertRgb(color);
		faceData[i+4] = Float.floatToRawIntBits(t.getInterpolatedU(u));
		faceData[i+5] = Float.floatToRawIntBits(t.getInterpolatedV(v));
	}

	public static Vector3f[] applyMatrixToVertices(Matrix4 matrix, Vector3f... vertices)
	{
		if(matrix==null)
			return vertices;
		Vector3f[] ret = new Vector3f[vertices.length];
		for(int i = 0; i < ret.length; i++)
			ret[i] = matrix.apply(vertices[i]);
		return ret;
	}

	public static Set<BakedQuad> createBakedBox(Vector3f from, Vector3f to, Matrix4 matrix, Function<EnumFacing, TextureAtlasSprite> textureGetter, float[] colour)
	{
		return createBakedBox(from, to, matrix, EnumFacing.NORTH, textureGetter, colour);
	}

	public static Set<BakedQuad> createBakedBox(Vector3f from, Vector3f to, Matrix4 matrix, EnumFacing facing, Function<EnumFacing, TextureAtlasSprite> textureGetter, float[] colour)
	{
		return createBakedBox(from, to, matrix, facing, vertices -> vertices, textureGetter, colour);
	}

	@Nonnull
	public static Set<BakedQuad> createBakedBox(Vector3f from, Vector3f to, Matrix4 matrix, EnumFacing facing, Function<Vector3f[], Vector3f[]> vertexTransformer, Function<EnumFacing, TextureAtlasSprite> textureGetter, float[] colour)
	{
		HashSet<BakedQuad> quads = new HashSet<>();
		if(vertexTransformer==null)
			vertexTransformer = v -> v;

		Vector3f[] vertices = {new Vector3f(from.x, from.y, from.z), new Vector3f(from.x, from.y, to.z), new Vector3f(to.x, from.y, to.z), new Vector3f(to.x, from.y, from.z)};
		TextureAtlasSprite sprite = textureGetter.apply(EnumFacing.DOWN);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.DOWN, facing), sprite, new double[]{from.x*16, 16-from.z*16, to.x*16, 16-to.z*16}, colour, true));

		for(Vector3f v : vertices)
			v.setY(to.y);
		sprite = textureGetter.apply(EnumFacing.UP);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), sprite, new double[]{from.x*16, from.z*16, to.x*16, to.z*16}, colour, false));

		vertices = new Vector3f[]{new Vector3f(to.x, to.y, from.z), new Vector3f(to.x, from.y, from.z), new Vector3f(from.x, from.y, from.z), new Vector3f(from.x, to.y, from.z)};
		sprite = textureGetter.apply(EnumFacing.NORTH);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.NORTH, facing), sprite, new double[]{from.x*16, 16-to.y*16, to.x*16, 16-from.y*16}, colour, false));

		for(Vector3f v : vertices)
			v.setZ(to.z);
		sprite = textureGetter.apply(EnumFacing.SOUTH);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.SOUTH, facing), sprite, new double[]{to.x*16, 16-to.y*16, from.x*16, 16-from.y*16}, colour, true));

		vertices = new Vector3f[]{new Vector3f(from.x, to.y, to.z), new Vector3f(from.x, from.y, to.z), new Vector3f(from.x, from.y, from.z), new Vector3f(from.x, to.y, from.z)};
		sprite = textureGetter.apply(EnumFacing.WEST);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.WEST, facing), sprite, new double[]{to.z*16, 16-to.y*16, from.z*16, 16-from.y*16}, colour, true));

		for(Vector3f v : vertices)
			v.setX(to.x);
		sprite = textureGetter.apply(EnumFacing.EAST);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(EnumFacing.EAST, facing), sprite, new double[]{16-to.z*16, 16-to.y*16, 16-from.z*16, 16-from.y*16}, colour, false));

		return quads;
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vector3f[] vertices, EnumFacing facing, TextureAtlasSprite sprite, float[] colour, boolean invert, float[] alpha)
	{
		return createBakedQuad(format, vertices, facing, sprite, new double[]{0, 0, 16, 16}, colour, invert, alpha);
	}

	public static BakedQuad createSmartLightingBakedQuad(VertexFormat format, Vector3f[] vertices, EnumFacing facing, TextureAtlasSprite sprite, float[] colour, boolean invert, float[] alpha, BlockPos base)
	{
		return createBakedQuad(format, vertices, facing, sprite, new double[]{0, 0, 16, 16}, colour, invert, alpha, true, base);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vector3f[] vertices, EnumFacing facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert)
	{
		return createBakedQuad(format, vertices, facing, sprite, uvs, colour, invert, alphaNoFading);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vector3f[] vertices, EnumFacing facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert, float[] alpha)
	{
		return createBakedQuad(format, vertices, facing, sprite, uvs, colour, invert, alpha, false, null);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vector3f[] vertices, EnumFacing facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert, float[] alpha, boolean smartLighting, BlockPos basePos)
	{
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setQuadOrientation(facing);
		builder.setTexture(sprite);
		Normal faceNormal = new Normal(facing.getDirectionVec().getX(), facing.getDirectionVec().getY(), facing.getDirectionVec().getZ());
		int vId = invert?3: 0;
		int u = vId > 1?2: 0;
		putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, alpha[vId]);
		vId = invert?2: 1;
		u = vId > 1?2: 0;
		putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, alpha[vId]);
		vId = invert?1: 2;
		u = vId > 1?2: 0;
		putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, alpha[vId]);
		vId = invert?0: 3;
		u = vId > 1?2: 0;
		putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, alpha[vId]);
		BakedQuad tmp = builder.build();
		return smartLighting?new SmartLightingQuad(tmp.getVertexData(), -1, facing, sprite, format, basePos): tmp;
	}

	public static void putVertexData(VertexFormat format, UnpackedBakedQuad.Builder builder, Vector3f pos, Normal faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha)
	{
		for(int e = 0; e < format.getElementCount(); e++)
			switch(format.getElement(e).getUsage())
			{
				case POSITION:
					builder.put(e, pos.getX(), pos.getY(), pos.getZ(), 0);
					break;
				case COLOR:
					float d = 1;//LightUtil.diffuseLight(faceNormal.x, faceNormal.y, faceNormal.z);
					builder.put(e, d*colour[0], d*colour[1], d*colour[2], 1*colour[3]*alpha);
					break;
				case UV:
					if(sprite==null)//Double Safety. I have no idea how it even happens, but it somehow did .-.
						sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
					builder.put(e,
							sprite.getInterpolatedU(u),
							sprite.getInterpolatedV((v)),
							0, 1);
					break;
				case NORMAL:
					builder.put(e, faceNormal.x, faceNormal.y, faceNormal.z, 0);
					break;
				default:
					builder.put(e);
			}
	}

	public static boolean crossesChunkBoundary(Vec3d start, Vec3d end, BlockPos offset)
	{
		if(crossesChunkBorderSingleDim(start.x, end.x, offset.getX()))
			return true;
		if(crossesChunkBorderSingleDim(start.y, end.y, offset.getY()))
			return true;
		return crossesChunkBorderSingleDim(start.z, end.z, offset.getZ());
	}

	private static boolean crossesChunkBorderSingleDim(double a, double b, int offset)
	{
		return ((int)Math.floor(a+offset)) >> 4!=((int)Math.floor(b+offset)) >> 4;
	}

	public static void renderQuads(Collection<BakedQuad> quads, float brightness, float red, float green, float blue)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder BufferBuilder = tessellator.getBuffer();
		for(BakedQuad bakedquad : quads)
		{
			BufferBuilder.begin(7, DefaultVertexFormats.ITEM);
			BufferBuilder.addVertexData(bakedquad.getVertexData());
			if(bakedquad.hasTintIndex())
				BufferBuilder.putColorRGB_F4(red*brightness, green*brightness, blue*brightness);
			else
				BufferBuilder.putColorRGB_F4(brightness, brightness, brightness);
			Vec3i vec3i = bakedquad.getFace().getDirectionVec();
			BufferBuilder.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
			tessellator.draw();
		}
	}

	public static ResourceLocation getSideTexture(@Nonnull ItemStack stack, EnumFacing side)
	{
		IBakedModel model = mc().getRenderItem().getItemModelWithOverrides(stack, null, null);
		List<BakedQuad> quads = model.getQuads(null, side, 0);
		if(quads==null||quads.isEmpty())//no quads for the specified side D:
			quads = model.getQuads(null, null, 0);
		if(quads==null||quads.isEmpty())//no quads at all D:
			return null;
		return new ResourceLocation(quads.get(0).getSprite().getIconName());
	}

	public static ResourceLocation getSideTexture(@Nonnull IBlockState state, EnumFacing side)
	{
		IBakedModel model = mc().getBlockRendererDispatcher().getModelForState(state);
		List<BakedQuad> quads = model.getQuads(state, side, 0);
		if(quads==null||quads.isEmpty())//no quads for the specified side D:
			quads = model.getQuads(state, null, 0);
		if(quads==null||quads.isEmpty())//no quads at all D:
			return null;
		return new ResourceLocation(quads.get(0).getSprite().getIconName());
	}

	private static int invertRgb(int in)
	{
		int ret = in&(255<<8);
		ret += (in >> 16)&255;
		ret += (in&255)<<16;
		return ret;
	}

	public static int pulseRGBAlpha(int rgb, int tickrate, float min, float max)
	{
		float f_alpha = mc().player.ticksExisted%(tickrate*2)/(float)tickrate;
		if(f_alpha > 1)
			f_alpha = 2-f_alpha;
		return changeRGBAlpha(rgb, MathHelper.clamp(f_alpha, min, max));

	}

	public static int changeRGBAlpha(int rgb, float alpha)
	{
		return (rgb&0x00ffffff)|((int)(alpha*255)<<24);
	}

	public static void renderBox(BufferBuilder wr, double x0, double y0, double z0, double x1, double y1, double z1)
	{
		wr.pos(x0, y0, z1).endVertex();
		wr.pos(x1, y0, z1).endVertex();
		wr.pos(x1, y1, z1).endVertex();
		wr.pos(x0, y1, z1).endVertex();

		wr.pos(x0, y1, z0).endVertex();
		wr.pos(x1, y1, z0).endVertex();
		wr.pos(x1, y0, z0).endVertex();
		wr.pos(x0, y0, z0).endVertex();

		wr.pos(x0, y0, z0).endVertex();
		wr.pos(x1, y0, z0).endVertex();
		wr.pos(x1, y0, z1).endVertex();
		wr.pos(x0, y0, z1).endVertex();

		wr.pos(x0, y1, z1).endVertex();
		wr.pos(x1, y1, z1).endVertex();
		wr.pos(x1, y1, z0).endVertex();
		wr.pos(x0, y1, z0).endVertex();

		wr.pos(x0, y0, z0).endVertex();
		wr.pos(x0, y0, z1).endVertex();
		wr.pos(x0, y1, z1).endVertex();
		wr.pos(x0, y1, z0).endVertex();

		wr.pos(x1, y1, z0).endVertex();
		wr.pos(x1, y1, z1).endVertex();
		wr.pos(x1, y0, z1).endVertex();
		wr.pos(x1, y0, z0).endVertex();
	}

	public static void renderTexturedBox(BufferBuilder wr, double x0, double y0, double z0, double x1, double y1, double z1, TextureAtlasSprite tex, boolean yForV)
	{
		float minU = tex.getInterpolatedU(x0*16);
		float maxU = tex.getInterpolatedU(x1*16);
		float minV = tex.getInterpolatedV((yForV?y1: z0)*16);
		float maxV = tex.getInterpolatedV((yForV?y0: z1)*16);
		renderTexturedBox(wr, x0, y0, z0, x1, y1, z1, minU, minV, maxU, maxV);
	}

	public static void renderTexturedBox(BufferBuilder wr, double x0, double y0, double z0, double x1, double y1, double z1, double u0, double v0, double u1, double v1)
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
	// variables for fancy TESR models, external to reduce allocations

	// The coordinates for each vertex of a quad
	private static final float[][] quadCoords = new float[4][3];
	// one diagonal of a quad. Used to calculate the normal of that quad
	private static final Vector3f side1 = new Vector3f();
	// and the other one
	private static final Vector3f side2 = new Vector3f();
	// and the normal of that quad
	private static final Vector3f normal = new Vector3f();
	// the brighnesses of the surrounding blocks. the first dimension indicates block (1) vs sky (0) light
	// These are used to create different light direction vectors depending on the direction of a quads normal vector.
	private static final int[][] neighbourBrightness = new int[2][6];
	// The light vectors created from neighbourBrightness aren't "normalized" (to length 255), the length needs to be divided by this factor to normalize it.
	// The indices are generated as follows: a 1 bit indicates a positive facing normal, a 0 a negative one. 1=x, 2=y, 4=z
	private static final float[][] normalizationFactors = new float[2][8];

	/**
	 * Renders the given quads. Uses the local and neighbour brightnesses to calculate lighting
	 *
	 * @param quads     the quads to render
	 * @param renderer  the BufferBuilder to render to
	 * @param world     the world the model is in. Will be used to obtain lighting information
	 * @param pos       the position that this model is in. Use the position the the quads are actually in, not the rendering block
	 * @param useCached Whether to use cached information for world local data. Set to true if the previous call to this method was in the same tick and for the same world+pos
	 */
	public static void renderModelTESRFancy(List<BakedQuad> quads, BufferBuilder renderer, World world, BlockPos pos, boolean useCached)
	{//TODO include matrix transformations?, cache normals?
		if(Config.IEConfig.disableFancyTESR)
			renderModelTESRFast(quads, renderer, world, pos);
		else
		{
			if(!useCached)
			{
				// Calculate surrounding brighness and split into block and sky light
				for(EnumFacing f : EnumFacing.VALUES)
				{
					int val = world.getCombinedLight(pos.offset(f), 0);
					neighbourBrightness[0][f.getIndex()] = (val >> 16)&255;
					neighbourBrightness[1][f.getIndex()] = val&255;
				}
				// calculate the different correction factors for all 8 possible light vectors
				for(int type = 0; type < 2; type++)
					for(int i = 0; i < 8; i++)
					{
						float sSquared = 0;
						if((i&1)!=0)
							sSquared += scaledSquared(neighbourBrightness[type][5], 255F);
						else
							sSquared += scaledSquared(neighbourBrightness[type][4], 255F);
						if((i&2)!=0)
							sSquared += scaledSquared(neighbourBrightness[type][1], 255F);
						else
							sSquared += scaledSquared(neighbourBrightness[type][0], 255F);
						if((i&4)!=0)
							sSquared += scaledSquared(neighbourBrightness[type][3], 255F);
						else
							sSquared += scaledSquared(neighbourBrightness[type][2], 255F);
						normalizationFactors[type][i] = (float)Math.sqrt(sSquared);
					}
			}
			int localBrightness = world.getCombinedLight(pos, 0);
			for(BakedQuad quad : quads)
			{
				int[] vData = quad.getVertexData();
				VertexFormat format = quad.getFormat();
				int size = format.getIntegerSize();
				int uv = format.getUvOffsetById(0)/4;
				// extract position info from the quad
				for(int i = 0; i < 4; i++)
				{
					quadCoords[i][0] = Float.intBitsToFloat(vData[size*i]);
					quadCoords[i][1] = Float.intBitsToFloat(vData[size*i+1]);
					quadCoords[i][2] = Float.intBitsToFloat(vData[size*i+2]);
				}
				//generate the normal vector
				side1.x = quadCoords[1][0]-quadCoords[3][0];
				side1.y = quadCoords[1][1]-quadCoords[3][1];
				side1.z = quadCoords[1][2]-quadCoords[3][2];
				side2.x = quadCoords[2][0]-quadCoords[0][0];
				side2.y = quadCoords[2][1]-quadCoords[0][1];
				side2.z = quadCoords[2][2]-quadCoords[0][2];
				Vector3f.cross(side1, side2, normal);
				normal.normalise();
				// calculate the final light values and do the rendering
				int l1 = getLightValue(neighbourBrightness[0], normalizationFactors[0], (localBrightness >> 16)&255);
				int l2 = getLightValue(neighbourBrightness[1], normalizationFactors[1], localBrightness&255);
				for(int i = 0; i < 4; ++i)
				{
					renderer
							.pos(quadCoords[i][0], quadCoords[i][1], quadCoords[i][2])
							.color(255, 255, 255, 255)
							.tex(Float.intBitsToFloat(vData[size*i+uv]), Float.intBitsToFloat(vData[size*i+uv+1]))
							.lightmap(l1, l2)
							.endVertex();
				}
			}
		}
	}

	private static int getLightValue(int[] neighbourBrightness, float[] normalizationFactors, int localBrightness)
	{
		//calculate the dot product between the required light vector and the normal of the quad
		// quad brightness is proportional to this value, see https://github.com/ssloy/tinyrenderer/wiki/Lesson-2:-Triangle-rasterization-and-back-face-culling#flat-shading-render
		float sideBrightness;
		byte type = 0;
		if(normal.x > 0)
		{
			sideBrightness = normal.x*neighbourBrightness[5];
			type |= 1;
		}
		else
			sideBrightness = -normal.x*neighbourBrightness[4];
		if(normal.y > 0)
		{
			sideBrightness += normal.y*neighbourBrightness[1];
			type |= 2;
		}
		else
			sideBrightness += -normal.y*neighbourBrightness[0];
		if(normal.z > 0)
		{
			sideBrightness += normal.z*neighbourBrightness[3];
			type |= 4;
		}
		else
			sideBrightness += -normal.z*neighbourBrightness[2];
		// the final light value is the aritmethic mean of the local brighness and the normalized "dot-product-brightness"
		return (int)((localBrightness+sideBrightness/normalizationFactors[type])/2);
	}

	private static float scaledSquared(int val, float scale)
	{
		return (val/scale)*(val/scale);
	}

	public static void renderModelTESRFast(List<BakedQuad> quads, BufferBuilder renderer, World world, BlockPos pos)
	{
		int brightness = world.getCombinedLight(pos, 0);
		int l1 = (brightness >> 0x10)&0xFFFF;
		int l2 = brightness&0xFFFF;
		for(BakedQuad quad : quads)
		{
			int[] vData = quad.getVertexData();
			VertexFormat format = quad.getFormat();
			int size = format.getIntegerSize();
			int uv = format.getUvOffsetById(0)/4;
			for(int i = 0; i < 4; ++i)
			{
				renderer
						.pos(Float.intBitsToFloat(vData[size*i]),
								Float.intBitsToFloat(vData[size*i+1]),
								Float.intBitsToFloat(vData[size*i+2]))
						.color(255, 255, 255, 255)
						.tex(Float.intBitsToFloat(vData[size*i+uv]), Float.intBitsToFloat(vData[size*i+uv+1]))
						.lightmap(l1, l2)
						.endVertex();
			}

		}
	}

	//Taken from TESR
	public static void setLightmapDisabled(boolean disabled)
	{
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);

		if(disabled)
		{
			GlStateManager.disableTexture2D();
		}
		else
		{
			GlStateManager.enableTexture2D();
		}

		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	private static Optional<Boolean> lightmapState;

	public static void toggleLightmap(boolean pre, boolean disabled)
	{
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		if(pre)
		{
			lightmapState = Optional.of(GL11.glIsEnabled(GL11.GL_TEXTURE_2D));
			if(disabled)
				GlStateManager.disableTexture2D();
			else
				GlStateManager.enableTexture2D();
		}
		else if(lightmapState.isPresent())
		{
			if(lightmapState.get())
				GlStateManager.enableTexture2D();
			else
				GlStateManager.disableTexture2D();
			lightmapState = Optional.empty();
		}
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
}