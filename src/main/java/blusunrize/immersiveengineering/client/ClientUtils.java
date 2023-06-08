/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.common.util.sound.IEBlockEntitySound;
import blusunrize.immersiveengineering.mixin.accessors.client.FontResourceManagerAccess;
import blusunrize.immersiveengineering.mixin.accessors.client.MinecraftAccess;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormatElement.Type;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.utils.IOUtils;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ClientUtils
{
	public static Minecraft mc()
	{
		return Minecraft.getInstance();
	}

	// Should probably be replaced by passing the texture to blit directly in most cases
	@Deprecated
	public static void bindTexture(ResourceLocation texture)
	{
		RenderSystem.setShaderTexture(0, texture);
	}

	public static TextureAtlasSprite getSprite(ResourceLocation rl)
	{
		return mc().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(rl);
	}

	public static Font font()
	{
		return mc().font;
	}

	public static float partialTicks()
	{
		return mc().getFrameTime();
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

	private static Font unicodeRenderer;

	public static Font unicodeFontRender()
	{
		if(unicodeRenderer==null)
			unicodeRenderer = new Font(rl -> {
				FontManager resourceManager = ((MinecraftAccess)Minecraft.getInstance()).getFontManager();
				Map<ResourceLocation, FontSet> fonts = ((FontResourceManagerAccess)resourceManager).getFontSets();
				return fonts.get(Minecraft.UNIFORM_FONT);
			}, false);
		return unicodeRenderer;
	}

	public static int getDarkenedTextColour(int colour)
	{
		int r = (colour >> 16&255)/4;
		int g = (colour >> 8&255)/4;
		int b = (colour&255)/4;
		return r<<16|g<<8|b;
	}

	public static IEBlockEntitySound generatePositionedIESound(SoundEvent soundEvent, float volume, float pitch, BlockPos pos)
	{
		IEBlockEntitySound sound = new IEBlockEntitySound(soundEvent, volume, pitch, pos);
		sound.evaluateVolume();
		ClientUtils.mc().getSoundManager().play(sound);
		return sound;
	}

	public static Quaternionf degreeToQuaterion(double x, double y, double z)
	{
		x = Math.toRadians(x);
		y = Math.toRadians(y);
		z = Math.toRadians(z);
		Quaternionf qYaw = new Quaternionf(0, (float)Math.sin(y/2), 0, (float)Math.cos(y/2));
		Quaternionf qPitch = new Quaternionf((float)Math.sin(x/2), 0, 0, (float)Math.cos(x/2));
		Quaternionf qRoll = new Quaternionf(0, 0, (float)Math.sin(z/2), (float)Math.cos(z/2));

		qYaw.mul(qRoll);
		qYaw.mul(qPitch);
		return qYaw;
	}

	public static Vec3[] applyMatrixToVertices(Transformation matrix, Vec3... vertices)
	{
		if(matrix==null)
			return vertices;
		Vec3[] ret = new Vec3[vertices.length];
		for(int i = 0; i < ret.length; i++)
		{
			Vector4f vec = new Vector4f((float)vertices[i].x, (float)vertices[i].y, (float)vertices[i].z, 1);
			matrix.transformPosition(vec);
			vec.mul(1 / vec.w);
			ret[i] = new Vec3(vec.x(), vec.y(), vec.z());
		}
		return ret;
	}

	public static Vector4f pulseRGBAlpha(Vector4f rgba, int tickrate, float min, float max)
	{
		float f_alpha = mc().player.tickCount%(tickrate*2)/(float)tickrate;
		if(f_alpha > 1)
			f_alpha = 2-f_alpha;
		return new Vector4f(rgba.x(), rgba.y(), rgba.z(), Mth.clamp(f_alpha, min, max));
	}

	public static int findOffset(VertexFormat vf, Usage u, Type t)
	{
		int offset = 0;
		for(VertexFormatElement element : vf.getElements())
		{
			if(element.getUsage()==u&&element.getType()==t)
			{
				Preconditions.checkState(offset%4==0);
				return offset/4;
			}
			offset += element.getByteSize();
		}
		throw new IllegalStateException();
	}

	public static int findTextureOffset(VertexFormat vf)
	{
		return findOffset(vf, Usage.UV, Type.FLOAT);
	}

	public static int findPositionOffset(VertexFormat vf)
	{
		return findOffset(vf, Usage.POSITION, Type.FLOAT);
	}

	public static Transformation rotateTo(Direction d)
	{
		return new Transformation(null)
				.blockCornerToCenter()
				.compose(toModelRotation(d).getRotation())
				.blockCenterToCorner();
	}

	public static BlockModelRotation toModelRotation(Direction d)
	{
		switch(d)
		{
			case DOWN:
				return BlockModelRotation.X90_Y0;
			case UP:
				return BlockModelRotation.X270_Y0;
			case NORTH:
				return BlockModelRotation.X0_Y0;
			case SOUTH:
				return BlockModelRotation.X0_Y180;
			case WEST:
				return BlockModelRotation.X0_Y270;
			case EAST:
				return BlockModelRotation.X0_Y90;
		}
		throw new IllegalArgumentException(String.valueOf(d));
	}
}
