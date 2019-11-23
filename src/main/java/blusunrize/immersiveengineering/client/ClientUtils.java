/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.RenderData;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.client.models.SmartLightingQuad;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.Normal;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.apache.commons.compress.utils.IOUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.vecmath.Quat4d;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

public class ClientUtils
{
	public static final AxisAlignedBB standardBlockAABB = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	static HashMap<String, ResourceLocation> resourceMap = new HashMap<String, ResourceLocation>();
	public static TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];

	public static void tessellateConnection(Connection connection, IImmersiveConnectable start, IImmersiveConnectable end, TextureAtlasSprite sprite)
	{
		if(connection==null||start==null||end==null)
			return;
		int col = connection.type.getColour(connection);
		double r = connection.type.getRenderDiameter()/2;
		int[] rgba = new int[]{col >> 16&255, col >> 8&255, col&255, 255};
		tessellateConnection(connection, start, end, rgba, r, sprite);
	}

	public static void tessellateConnection(Connection connection, IImmersiveConnectable start, IImmersiveConnectable end, int[] rgba, double radius, TextureAtlasSprite sprite)
	{
		/*TODO
		Vec3d startOffset = Vec3d.ZERO;//TODO start.getConnectionOffset(connection);
		Vec3d endOffset = Vec3d.ZERO;//TODO end.getConnectionOffset(connection);
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
		*/
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
		return Minecraft.getInstance();
	}

	public static void bindTexture(String path)
	{
		mc().getTextureManager().bindTexture(getResource(path));
	}

	public static void bindAtlas()
	{
		mc().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
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
		return mc().getTextureMap().getSprite(rl);
	}

	public static FontRenderer font()
	{
		return mc().fontRenderer;
	}

	public static float partialTicks()
	{
		return mc().getRenderPartialTicks();
	}

	public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException
	{
		BufferedImage bufferedimage;

		try
		{
			bufferedimage = ImageIO.read(imageStream);
		} finally
		{
			IOUtils.closeQuietly(imageStream);
		}

		return bufferedimage;
	}

	public static FontRenderer unicodeFontRender()
	{
		return mc().getFontResourceManager().getFontRenderer(new ResourceLocation(ImmersiveEngineering.MODID, "unicode"));
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
		ClientUtils.mc().getSoundHandler().play(sound);
		return sound;
	}

	public static List<RendererModel> copyModelRenderers(Model model, List<RendererModel> oldRenderers)
	{
		List<RendererModel> newRenderers = new ArrayList<>(oldRenderers.size());
		for(int i = 0; i < oldRenderers.size(); i++)
			{
				RendererModel oldM = oldRenderers.get(i);
				RendererModel newM = new RendererModel(model, oldM.boxName);
				int toX = oldM.textureOffsetX;
				int toY = oldM.textureOffsetY;
				newM.setTextureOffset(toX, toY);
				newM.mirror = oldM.mirror;
				newM.cubeList.clear();
				for(ModelBox cube : oldM.cubeList)
					newM.cubeList.add(new ModelBox(newM, toX, toY, cube.posX1, cube.posY1, cube.posZ1, (int)(cube.posX2-cube.posX1), (int)(cube.posY2-cube.posY1), (int)(cube.posZ2-cube.posZ1), 0));
				newM.setRotationPoint(oldM.rotationPointX, oldM.rotationPointY, oldM.rotationPointZ);
				newM.rotateAngleX = oldM.rotateAngleX;
				newM.rotateAngleY = oldM.rotateAngleY;
				newM.rotateAngleZ = oldM.rotateAngleZ;
				newM.offsetX = oldM.offsetX;
				newM.offsetY = oldM.offsetY;
				newM.offsetZ = oldM.offsetZ;
				newRenderers.add(newM);
			}
		return newRenderers;
	}

	public static void handleBipedRotations(BipedModel model, Entity entity)
	{
		if(!IEConfig.GENERAL.fancyItemHolding.get())
			return;

		if(entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)entity;
			for(Hand hand : Hand.values())
			{
				ItemStack heldItem = player.getHeldItem(hand);
				if(!heldItem.isEmpty())
				{
					boolean right = (hand==Hand.MAIN_HAND)==(player.getPrimaryHand()==HandSide.RIGHT);
					if(heldItem.getItem() instanceof RevolverItem)
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
					else if(heldItem.getItem() instanceof DrillItem||heldItem.getItem() instanceof ChemthrowerItem)
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
					else if(heldItem.getItem() instanceof RailgunItem)
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

	//Cheers boni =P
	public static void drawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, World world, Collection<BlockPos> blocks)
	{
		double d0 = entityIn.lastTickPosX+(entityIn.posX-entityIn.lastTickPosX)*(double)partialTicks;
		double d1 = entityIn.lastTickPosY+(entityIn.posY-entityIn.lastTickPosY)*(double)partialTicks;
		double d2 = entityIn.lastTickPosZ+(entityIn.posZ-entityIn.lastTickPosZ)*(double)partialTicks;
		TextureManager renderEngine = Minecraft.getInstance().textureManager;
		int progress = (int)(Minecraft.getInstance().playerController.curBlockDamageMP*10f)-1; // 0-10
		if(progress < 0)
			return;
		renderEngine.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		//preRenderDamagedBlocks BEGIN
		GlStateManager.blendFuncSeparate(774, 768, 1, 0);
		GlStateManager.enableBlend();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.5F);
		GlStateManager.polygonOffset(-3.0F, -3.0F);
		GlStateManager.enablePolygonOffset();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableAlphaTest();
		GlStateManager.pushMatrix();
		//preRenderDamagedBlocks END
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRendererIn.setTranslation(-d0, -d1-entityIn.getEyeHeight(), -d2);
		//		worldRendererIn.markDirty();
		for(BlockPos blockpos : blocks)
		{
			Block block = world.getBlockState(blockpos).getBlock();
			TileEntity te = world.getTileEntity(blockpos);
			boolean hasBreak = block instanceof ChestBlock||block instanceof EnderChestBlock
					||block instanceof AbstractSignBlock||block instanceof SkullBlock;
			if(!hasBreak) hasBreak = te!=null&&te.canRenderBreaking();
			if(!hasBreak)
			{
				BlockState iblockstate = world.getBlockState(blockpos);
				if(iblockstate.getMaterial()!=Material.AIR)
				{
					TextureAtlasSprite textureatlassprite = destroyBlockIcons[progress];
					Preconditions.checkNotNull(textureatlassprite, "No destroy icon for "+progress);
					BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
					blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, world);
				}
			}
		}
		tessellatorIn.draw();
		worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
		// postRenderDamagedBlocks BEGIN
		GlStateManager.disableAlphaTest();
		GlStateManager.polygonOffset(0.0F, 0.0F);
		GlStateManager.disablePolygonOffset();
		GlStateManager.enableAlphaTest();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
		// postRenderDamagedBlocks END
	}

	public static void drawColouredRect(int x, int y, int w, int h, int colour)
	{
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
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
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
	}

	//TODO replace these methods with AbstractGui#fillGradient and (Abstract)Gui#blit, or figure out why that isn't possible
	public static void drawGradientRect(int x0, int y0, int x1, int y1, int colour0, int colour1)
	{
		float alpha0 = (colour0 >> 24&255)/255.0F;
		float blue0 = (colour0 >> 16&255)/255.0F;
		float green0 = (colour0 >> 8&255)/255.0F;
		float red0 = (colour0&255)/255.0F;
		float alpha1 = (colour1 >> 24&255)/255.0F;
		float blue1 = (colour1 >> 16&255)/255.0F;
		float green1 = (colour1 >> 8&255)/255.0F;
		float red1 = (colour1&255)/255.0F;
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(x1, y0, 0).color(blue0, green0, red0, alpha0).endVertex();
		worldrenderer.pos(x0, y0, 0).color(blue0, green0, red0, alpha0).endVertex();
		worldrenderer.pos(x0, y1, 0).color(blue1, green1, red1, alpha1).endVertex();
		worldrenderer.pos(x1, y1, 0).color(blue1, green1, red1, alpha1).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
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
		bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE.toString());
		TextureAtlasSprite sprite = getSprite(fluid.getFluid().getAttributes().getStill(fluid));
		int col = fluid.getFluid().getAttributes().getColor(fluid);
		GlStateManager.color3f((col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f);
		int iW = sprite.getWidth();
		int iH = sprite.getHeight();
		if(iW > 0&&iH > 0)
			drawRepeatedSprite(x, y, w, h, iW, iH, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
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

	public static void drawHoveringText(List<ITextComponent> list, int x, int y, FontRenderer font)
	{
		drawHoveringText(list, x, y, font, -1, -1);
	}

	public static void drawHoveringText(List<ITextComponent> list, int x, int y, FontRenderer font, int xSize, int ySize)
	{
		if(!list.isEmpty())
		{
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepthTest();
			int k = 0;
			Iterator<ITextComponent> iterator = list.iterator();
			while(iterator.hasNext())
			{
				ITextComponent s = iterator.next();
				int l = font.getStringWidth(s.getFormattedText());
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
			GlStateManager.translatef(0, 0, 300);
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
			GlStateManager.translatef(0, 0, -300);

			for(int i2 = 0; i2 < list.size(); ++i2)
			{
				String s1 = list.get(i2).getFormattedText();
				font.drawStringWithShadow(s1, j2, k2, -1);

				if(i2==0)
					k2 += 2;

				k2 += 10;
			}

			GlStateManager.enableLighting();
			GlStateManager.enableDepthTest();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

	public static void handleGuiTank(IFluidTank tank, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, String originalTexture, List<ITextComponent> tooltip)
	{
		handleGuiTank(tank.getFluid(), tank.getCapacity(), x, y, w, h, oX, oY, oW, oH, mX, mY, originalTexture, tooltip);
	}

	public static void handleGuiTank(FluidStack fluid, int capacity, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, String originalTexture, List<ITextComponent> tooltip)
	{
		if(tooltip==null)
		{
			if(fluid!=null&&fluid.getFluid()!=null)
			{
				int fluidHeight = (int)(h*(fluid.getAmount()/(float)capacity));
				drawRepeatedFluidSprite(fluid, x, y+h-fluidHeight, w, fluidHeight);
				bindTexture(originalTexture);
				GlStateManager.color3f(1, 1, 1);
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

	public static void addFluidTooltip(FluidStack fluid, List<ITextComponent> tooltip, int tankCapacity)
	{
		if(!fluid.isEmpty())
			tooltip.add(fluid.getDisplayName().setStyle(
					new Style().setColor(fluid.getFluid().getAttributes().getRarity(fluid).color)));
		else
			tooltip.add(new TranslationTextComponent("gui.immersiveengineering.empty"));
		if(fluid.getFluid() instanceof IEFluid)
			((IEFluid)fluid.getFluid()).addTooltipInfo(fluid, null, tooltip);

		if(mc().gameSettings.advancedItemTooltips&&!fluid.isEmpty())
		{
			if(!Screen.hasShiftDown())
				tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"holdShiftForInfo"));
			else
			{
				Style darkGray = new Style().setColor(TextFormatting.DARK_GRAY);
				//TODO translation keys
				tooltip.add(new StringTextComponent("Fluid Registry: "+fluid.getFluid().getRegistryName()).setStyle(darkGray));
				tooltip.add(new StringTextComponent("Density: "+fluid.getFluid().getAttributes().getDensity(fluid)).setStyle(darkGray));
				tooltip.add(new StringTextComponent("Temperature: "+fluid.getFluid().getAttributes().getTemperature(fluid)).setStyle(darkGray));
				tooltip.add(new StringTextComponent("Viscosity: "+fluid.getFluid().getAttributes().getViscosity(fluid)).setStyle(darkGray));
				tooltip.add(new StringTextComponent("NBT Data: "+fluid.getTag()).setStyle(darkGray));
			}
		}

		Style gray = new Style().setColor(TextFormatting.GRAY);
		if(tankCapacity > 0)
			tooltip.add(new StringTextComponent(fluid.getAmount()+"/"+tankCapacity+"mB").setStyle(gray));
		else
			tooltip.add(new StringTextComponent(fluid.getAmount()+"mB").setStyle(gray));
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

	private static final Vec3d fadingOffset = new Vec3d(.0001F, .0001F, .0001F);
	private static float[] alphaFirst2Fading = {0, 0, 1, 1};
	private static float[] alphaNoFading = {1, 1, 1, 1};

	public static List<BakedQuad>[] convertConnectionFromBlockstate(BlockPos here, Set<Connection.RenderData> data, TextureAtlasSprite t)
	{
		List<BakedQuad>[] ret = new List[]{
				new ArrayList<>(),
				new ArrayList<>()
		};
		if(data==null)
			return ret;
		Vec3d dir = Vec3d.ZERO;
		Vec3d cross = Vec3d.ZERO;

		Vec3d up = new Vec3d(0, 1, 0);
		for(Connection.RenderData connData : data)
		{
			int color = connData.color;
			float[] rgb = {(color >> 16&255)/255f, (color >> 8&255)/255f, (color&255)/255f, (color >> 24&255)/255f};
			if(rgb[3]==0)
				rgb[3] = 1;
			float radius = (float)(connData.type.getRenderDiameter()/2);

			for(int i = 1; i < connData.pointsToRender; i++)
			{
				boolean fading = i==connData.pointsToRender-1&&connData.pointsToRender <= RenderData.POINTS_PER_WIRE;
				List<BakedQuad> curr = ret[fading?1: 0];
				int j = i-1;
				Vec3d current = connData.getPoint(i);
				Vec3d previous = connData.getPoint(j);
				if(fading)
				{
					current = current.add(fadingOffset);
					previous = previous.add(fadingOffset);
				}
				boolean vertical = current.x==previous.x&&current.z==previous.z;
				if(!vertical)
				{
					dir = current.subtract(previous);
					cross = up.crossProduct(dir);
					cross = cross.scale(radius/cross.length());
				}
				else
					cross = new Vec3d(radius, 0, 0);
				Vec3d[] vertices = {current.add(cross),
						current.subtract(cross),
						previous.subtract(cross),
						previous.add(cross)};
				curr.add(createSmartLightingBakedQuad(DefaultVertexFormats.ITEM, vertices, Direction.DOWN, t, rgb, false, fading?alphaFirst2Fading: alphaNoFading, here));
				curr.add(createSmartLightingBakedQuad(DefaultVertexFormats.ITEM, vertices, Direction.UP, t, rgb, true, fading?alphaFirst2Fading: alphaNoFading, here));

				if(!vertical)
				{
					cross = dir.crossProduct(cross);
					cross = cross.scale(radius/cross.length());
				}
				else
					cross = new Vec3d(0, 0, radius);
				vertices = new Vec3d[]{current.add(cross),
						current.subtract(cross),
						previous.subtract(cross),
						previous.add(cross)};
				curr.add(createSmartLightingBakedQuad(DefaultVertexFormats.ITEM, vertices, Direction.WEST, t, rgb, false, fading?alphaFirst2Fading: alphaNoFading, here));
				curr.add(createSmartLightingBakedQuad(DefaultVertexFormats.ITEM, vertices, Direction.EAST, t, rgb, true, fading?alphaFirst2Fading: alphaNoFading, here));
			}
		}
		return ret;
	}

	public static int getVertexCountForSide(ConnectionPoint start, Connection conn, int totalPoints)
	{
		List<Integer> crossings = new ArrayList<>();
		Vec3d lastPoint = conn.getPoint(0, start);
		for(int i = 1; i <= totalPoints; i++)
		{
			Vec3d current = conn.getPoint(i/(double)totalPoints, start);
			if(crossesChunkBoundary(current, lastPoint, start.getPosition()))
				crossings.add(i);
			lastPoint = current;
		}
		int index = crossings.size()/2;
		boolean greater = conn.isPositiveEnd(start);
		if(crossings.size()%2==0&&greater)
			index--;
		if(crossings.size() > 0)
			return crossings.get(index)+(greater?1: 2);
		else
			return greater?totalPoints+1: 0;
	}

	public static Vec3d[] applyMatrixToVertices(Matrix4 matrix, Vec3d... vertices)
	{
		if(matrix==null)
			return vertices;
		Vec3d[] ret = new Vec3d[vertices.length];
		for(int i = 0; i < ret.length; i++)
			ret[i] = matrix.apply(vertices[i]);
		return ret;
	}

	public static Set<BakedQuad> createBakedBox(Vec3d from, Vec3d to, Matrix4 matrix, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour)
	{
		return createBakedBox(from, to, matrix, Direction.NORTH, textureGetter, colour);
	}

	public static Set<BakedQuad> createBakedBox(Vec3d from, Vec3d to, Matrix4 matrix, Direction facing, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour)
	{
		return createBakedBox(from, to, matrix, facing, vertices -> vertices, textureGetter, colour);
	}

	@Nonnull
	public static Set<BakedQuad> createBakedBox(Vec3d from, Vec3d to, Matrix4 matrix, Direction facing, Function<Vec3d[], Vec3d[]> vertexTransformer, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour)
	{
		HashSet<BakedQuad> quads = new HashSet<>();
		if(vertexTransformer==null)
			vertexTransformer = v -> v;

		Vec3d[] vertices = {
				new Vec3d(from.x, from.y, from.z),
				new Vec3d(from.x, from.y, to.z),
				new Vec3d(to.x, from.y, to.z),
				new Vec3d(to.x, from.y, from.z)
		};
		TextureAtlasSprite sprite = textureGetter.apply(Direction.DOWN);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), sprite, new double[]{from.x*16, 16-from.z*16, to.x*16, 16-to.z*16}, colour, true));

		for(int i = 0; i < vertices.length; i++)
		{
			Vec3d v = vertices[i];
			vertices[i] = new Vec3d(v.x, to.y, v.z);
		}
		sprite = textureGetter.apply(Direction.UP);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.UP, facing), sprite, new double[]{from.x*16, from.z*16, to.x*16, to.z*16}, colour, false));

		vertices = new Vec3d[]{
				new Vec3d(to.x, to.y, from.z),
				new Vec3d(to.x, from.y, from.z),
				new Vec3d(from.x, from.y, from.z),
				new Vec3d(from.x, to.y, from.z)
		};
		sprite = textureGetter.apply(Direction.NORTH);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.NORTH, facing), sprite, new double[]{from.x*16, 16-to.y*16, to.x*16, 16-from.y*16}, colour, false));

		for(int i = 0; i < vertices.length; i++)
		{
			Vec3d v = vertices[i];
			vertices[i] = new Vec3d(v.x, v.y, to.z);
		}
		sprite = textureGetter.apply(Direction.SOUTH);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.SOUTH, facing), sprite, new double[]{to.x*16, 16-to.y*16, from.x*16, 16-from.y*16}, colour, true));

		vertices = new Vec3d[]{
				new Vec3d(from.x, to.y, to.z),
				new Vec3d(from.x, from.y, to.z),
				new Vec3d(from.x, from.y, from.z),
				new Vec3d(from.x, to.y, from.z)
		};
		sprite = textureGetter.apply(Direction.WEST);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.WEST, facing), sprite, new double[]{to.z*16, 16-to.y*16, from.z*16, 16-from.y*16}, colour, true));

		for(int i = 0; i < vertices.length; i++)
		{
			Vec3d v = vertices[i];
			vertices[i] = new Vec3d(to.x, v.y, v.z);
		}
		sprite = textureGetter.apply(Direction.EAST);
		if(sprite!=null)
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.EAST, facing), sprite, new double[]{16-to.z*16, 16-to.y*16, 16-from.z*16, 16-from.y*16}, colour, false));

		return quads;
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, float[] colour, boolean invert, float[] alpha)
	{
		return createBakedQuad(format, vertices, facing, sprite, new double[]{0, 0, 16, 16}, colour, invert, alpha);
	}

	public static BakedQuad createSmartLightingBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, float[] colour, boolean invert, float[] alpha, BlockPos base)
	{
		return createBakedQuad(format, vertices, facing, sprite, new double[]{0, 0, 16, 16}, colour, invert, alpha, true, base);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert)
	{
		return createBakedQuad(format, vertices, facing, sprite, uvs, colour, invert, alphaNoFading);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert, float[] alpha)
	{
		return createBakedQuad(format, vertices, facing, sprite, uvs, colour, invert, alpha, false, null);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vec3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert, float[] alpha, boolean smartLighting, BlockPos basePos)
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

	public static void putVertexData(VertexFormat format, UnpackedBakedQuad.Builder builder, Vec3d pos, Normal faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha)
	{
		for(int e = 0; e < format.getElementCount(); e++)
			switch(format.getElement(e).getUsage())
			{
				case POSITION:
					builder.put(e, (float)pos.x, (float)pos.y, (float)pos.z, 0);
					break;
				case COLOR:
					float d = 1;//LightUtil.diffuseLight(faceNormal.x, faceNormal.y, faceNormal.z);
					builder.put(e, d*colour[0], d*colour[1], d*colour[2], 1*colour[3]*alpha);
					break;
				case UV:
					if(sprite==null)//Double Safety. I have no idea how it even happens, but it somehow did .-.
						sprite = MissingTextureSprite.func_217790_a();
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

	public static ResourceLocation getSideTexture(@Nonnull ItemStack stack, Direction side)
	{
		IBakedModel model = mc().getItemRenderer().getItemModelWithOverrides(stack, null, null);
		return getSideTexture(model, side, null);
	}

	public static ResourceLocation getSideTexture(@Nonnull BlockState state, Direction side)
	{
		IBakedModel model = mc().getBlockRendererDispatcher().getModelForState(state);
		return getSideTexture(model, side, state);
	}

	public static ResourceLocation getSideTexture(@Nonnull IBakedModel model, Direction side, @Nullable BlockState state)
	{
		List<BakedQuad> quads = model.getQuads(state, side, Utils.RAND);
		if(quads==null||quads.isEmpty())//no quads for the specified side D:
			quads = model.getQuads(state, null, Utils.RAND);
		if(quads==null||quads.isEmpty())//no quads at all D:
			return null;
		return quads.get(0).getSprite().getName();
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
		if(IEConfig.GENERAL.disableFancyTESR.get())
			renderModelTESRFast(quads, renderer, world, pos);
		else
		{
			if(!useCached)
			{
				// Calculate surrounding brighness and split into block and sky light
				for(Direction f : Direction.VALUES)
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
				Vec3d side1 = new Vec3d(quadCoords[1][0]-quadCoords[3][0],
				quadCoords[1][1]-quadCoords[3][1],
				quadCoords[1][2]-quadCoords[3][2]);
				Vec3d side2 = new Vec3d(quadCoords[2][0]-quadCoords[0][0],
				quadCoords[2][1]-quadCoords[0][1],
				quadCoords[2][2]-quadCoords[0][2]);
				Vec3d normal = side1.crossProduct(side2);
				normal = normal.normalize();
				// calculate the final light values and do the rendering
				int l1 = getLightValue(neighbourBrightness[0], normalizationFactors[0], (localBrightness >> 16)&255, normal);
				int l2 = getLightValue(neighbourBrightness[1], normalizationFactors[1], localBrightness&255, normal);
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

	private static int getLightValue(int[] neighbourBrightness, float[] normalizationFactors, int localBrightness, Vec3d normal)
	{
		//calculate the dot product between the required light vector and the normal of the quad
		// quad brightness is proportional to this value, see https://github.com/ssloy/tinyrenderer/wiki/Lesson-2:-Triangle-rasterization-and-back-face-culling#flat-shading-render
		double sideBrightness;
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
		GlStateManager.activeTexture(GLX.GL_TEXTURE1);

		if(disabled)
		{
			GlStateManager.disableTexture();
		}
		else
		{
			GlStateManager.enableTexture();
		}

		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
	}

	@Nullable
	private static Boolean lightmapState;

	public static void toggleLightmap(boolean pre, boolean disabled)
	{
		GlStateManager.activeTexture(GLX.GL_TEXTURE1);
		if(pre)
		{
			lightmapState = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
			if(disabled)
				GlStateManager.disableTexture();
			else
				GlStateManager.enableTexture();
		}
		else if(lightmapState!=null)
		{
			if(lightmapState)
				GlStateManager.enableTexture();
			else
				GlStateManager.disableTexture();
			lightmapState = null;
		}
		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
	}
}