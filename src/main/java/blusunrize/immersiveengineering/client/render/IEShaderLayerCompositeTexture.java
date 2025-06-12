/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.IntFunction;

public class IEShaderLayerCompositeTexture extends AbstractTexture
{
	/**
	 * The location of the texture.
	 */
	private final ResourceLocation canvasTexture;
	private final ShaderLayer[] layers;

	public IEShaderLayerCompositeTexture(ResourceLocation canvasTexture, ShaderLayer[] layers)
	{
		this.canvasTexture = canvasTexture;
		this.layers = layers;
	}

	@Override
	public void load(@Nonnull ResourceManager resourceManager)
	{
		// Everything in this method uses ABGR, because Mojang I guess
		// Even methods that have "RGBA" in the name actually expect ABGR as a format

		this.releaseId();
		Resource iresource = resourceManager.getResource(this.canvasTexture).orElseThrow();
		try(
				InputStream imageStream = iresource.open();
				NativeImage originalImage = NativeImage.read(imageStream);
		)
		{
			int canvasWidth = originalImage.getWidth();
			int canvasHeight = originalImage.getHeight();

			NativeImage finalTexture = new NativeImage(canvasWidth, canvasHeight, true);
			int layer = 0;

			while(layer < 17&&layer < this.layers.length)
			{
				String texPath = this.layers[layer].getTexture().getPath();

				if(!texPath.startsWith("textures/"))
					texPath = "textures/"+texPath;
				if(!texPath.endsWith(".png"))
					texPath += ".png";
				String texture = this.layers[layer].getTexture().getNamespace()+":"+texPath;
				var colour = this.layers[layer].getColor();

				Resource iresource1 = resourceManager.getResource(ResourceLocation.parse(texture)).orElseThrow();
				try(
						InputStream texStream = iresource1.open();
						NativeImage texureImage = NativeImage.read(texStream);
				)
				{

					float[] layerABGR = new float[]{colour.a(), colour.b(), colour.g(), colour.r()};
					// increase low alpha values
					if(layerABGR[0] < 0.2)
						layerABGR[0] *= 2.5f;

					IntFunction<Integer> uInterpolate = uIn -> uIn;
					IntFunction<Integer> vInterpolate = vIn -> vIn;

					int bufImg2Size = Math.min(texureImage.getWidth(), texureImage.getHeight());

					int uMin = 0;
					int vMin = 0;
					int uMax = canvasWidth;
					int vMax = canvasHeight;

					final double[] texBounds = this.layers[layer].getTextureBounds();
					if(texBounds!=null)
					{
						final double uOffset = texBounds[0]*canvasWidth;
						final double vOffset = texBounds[1]*canvasHeight;
						final double uScale = bufImg2Size/((texBounds[2]-texBounds[0])*canvasWidth);
						final double vScale = bufImg2Size/((texBounds[3]-texBounds[1])*canvasHeight);
						uInterpolate = uIn -> (int)Math.round((uIn-uOffset)*uScale);
						vInterpolate = vIn -> (int)Math.round((vIn-vOffset)*vScale);
						uMin = (int)uOffset;
						vMin = (int)vOffset;
						uMax = (int)(texBounds[2]*canvasWidth);
						vMax = (int)(texBounds[3]*canvasHeight);
					}

					try
					{
						for(int v = vMin; v < vMax; ++v)
							for(int u = uMin; u < uMax; ++u)
							{
								int interU = uInterpolate.apply(u)%bufImg2Size;
								int interV = vInterpolate.apply(v)%bufImg2Size;

								ColorABGR baseABGR = new ColorABGR(texureImage.getPixelRGBA(interU, interV));
								if(!baseABGR.isTransparent())
								{
									int iNoise = originalImage.getPixelRGBA(u, v);
									float[] noiseABGR = {(iNoise&255)/255f, (iNoise>>8&255)/255f, (iNoise>>16&255)/255f, (iNoise>>24&255)/255f};

									// Multiply texture value with layer & noise colour
									baseABGR.modify(layerABGR);
									baseABGR.modify(noiseABGR);

									// Apply to final texture
									finalTexture.blendPixel(u, v, ColorABGR.blend(
											baseABGR,
											new ColorABGR(finalTexture.getPixelRGBA(u, v))
									).toInt());
								}
							}
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}

				++layer;
			}
			TextureUtil.prepareImage(this.getId(), 0, finalTexture.getWidth(), finalTexture.getHeight());
			finalTexture.upload(0, 0, 0, 0, 0, finalTexture.getWidth(), finalTexture.getHeight(), false, false, false, false);

		} catch(IOException ioexception)
		{
			IELogger.error("Couldn't load layered image", ioexception);
		}
	}

	private static final class ColorABGR
	{
		private float a;
		private float b;
		private float g;
		private float r;

		private ColorABGR(float a, float b, float g, float r)
		{
			this.a = Math.clamp(a, 0, 1);
			this.b = Math.clamp(b, 0, 1);
			this.g = Math.clamp(g, 0, 1);
			this.r = Math.clamp(r, 0, 1);
		}

		public ColorABGR(int abgr)
		{
			this((abgr>>24&255)/255f, (abgr>>16&255)/255f, (abgr>>8&255)/255f, (abgr&255)/255f);
		}

		public static ColorABGR blend(ColorABGR input, ColorABGR existing)
		{
			float mixFactor = 1.0F-input.a;
			float newAlpha = input.a*input.a+existing.a*mixFactor;
			if(existing.a==0)
				newAlpha = input.a;
			else if(newAlpha < existing.a)
				newAlpha = existing.a;
			return new ColorABGR(
					newAlpha,
					input.b*input.a+existing.b*mixFactor,
					input.g*input.a+existing.g*mixFactor,
					input.r*input.a+existing.r*mixFactor
			);
		}

		public boolean isTransparent()
		{
			return a==0||(r==0&&g==0&&b==0);
		}

		public void modify(float[] rgba)
		{
			this.a *= rgba[0];
			this.b *= rgba[1];
			this.g *= rgba[2];
			this.r *= rgba[3];
		}

		public int toInt()
		{
			return (int)(a*255)<<24|(int)(b*255)<<16|(int)(g*255)<<8|(int)(r*255);
		}
	}
}