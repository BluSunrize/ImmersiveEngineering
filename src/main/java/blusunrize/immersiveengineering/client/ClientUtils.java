/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import blusunrize.immersiveengineering.mixin.accessors.client.FontResourceManagerAccess;
import blusunrize.immersiveengineering.mixin.accessors.client.MinecraftAccess;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.fonts.FontResourceManager;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Type;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Usage;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import org.apache.commons.compress.utils.IOUtils;

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

	public static void bindTexture(ResourceLocation texture)
	{
		mc().getTextureManager().bindTexture(texture);
	}

	public static TextureAtlasSprite getSprite(ResourceLocation rl)
	{
		return mc().getModelManager().getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).getSprite(rl);
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

	private static FontRenderer unicodeRenderer;

	public static FontRenderer unicodeFontRender()
	{
		if(unicodeRenderer==null)
			unicodeRenderer = new FontRenderer(rl -> {
				FontResourceManager resourceManager = ((MinecraftAccess)Minecraft.getInstance()).getFontResourceMananger();
				Map<ResourceLocation, Font> fonts = ((FontResourceManagerAccess)resourceManager).getIdToFontMap();
				return fonts.get(Minecraft.UNIFORM_FONT_RENDERER_NAME);
			});
		return unicodeRenderer;
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
		sound.evaluateVolume();
		ClientUtils.mc().getSoundHandler().play(sound);
		return sound;
	}

	public static Quaternion degreeToQuaterion(double x, double y, double z)
	{
		x = Math.toRadians(x);
		y = Math.toRadians(y);
		z = Math.toRadians(z);
		Quaternion qYaw = new Quaternion(0, (float)Math.sin(y/2), 0, (float)Math.cos(y/2));
		Quaternion qPitch = new Quaternion((float)Math.sin(x/2), 0, 0, (float)Math.cos(x/2));
		Quaternion qRoll = new Quaternion(0, 0, (float)Math.sin(z/2), (float)Math.cos(z/2));

		qYaw.multiply(qRoll);
		qYaw.multiply(qPitch);
		return qYaw;
	}

	public static Vector3d[] applyMatrixToVertices(TransformationMatrix matrix, Vector3d... vertices)
	{
		if(matrix==null)
			return vertices;
		Vector3d[] ret = new Vector3d[vertices.length];
		for(int i = 0; i < ret.length; i++)
		{
			Vector4f vec = new Vector4f((float)vertices[i].x, (float)vertices[i].y, (float)vertices[i].z, 1);
			matrix.transformPosition(vec);
			vec.perspectiveDivide();
			ret[i] = new Vector3d(vec.getX(), vec.getY(), vec.getZ());
		}
		return ret;
	}

	public static Vector4f pulseRGBAlpha(Vector4f rgba, int tickrate, float min, float max)
	{
		float f_alpha = mc().player.ticksExisted%(tickrate*2)/(float)tickrate;
		if(f_alpha > 1)
			f_alpha = 2-f_alpha;
		return new Vector4f(rgba.getX(), rgba.getY(), rgba.getZ(), MathHelper.clamp(f_alpha, min, max));
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
			offset += element.getSize();
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

	public static TransformationMatrix rotateTo(Direction d)
	{
		return new TransformationMatrix(null)
				.blockCornerToCenter()
				.compose(toModelRotation(d).getRotation())
				.blockCenterToCorner();
	}

	public static ModelRotation toModelRotation(Direction d)
	{
		switch(d)
		{
			case DOWN:
				return ModelRotation.X90_Y0;
			case UP:
				return ModelRotation.X270_Y0;
			case NORTH:
				return ModelRotation.X0_Y0;
			case SOUTH:
				return ModelRotation.X0_Y180;
			case WEST:
				return ModelRotation.X0_Y270;
			case EAST:
				return ModelRotation.X0_Y90;
		}
		throw new IllegalArgumentException(String.valueOf(d));
	}
}
