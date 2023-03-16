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
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.IntFunction;

import static net.minecraft.util.FastColor.ARGB32.*;

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
				Vector4f colour = this.layers[layer].getColor();

				Resource iresource1 = resourceManager.getResource(new ResourceLocation(texture)).orElseThrow();
				try(
						InputStream texStream = iresource1.open();
						NativeImage texureImage = NativeImage.read(texStream);
				)
				{

					float[] mod = new float[]{colour.x(), colour.y(), colour.z(), colour.w()};
					if(mod[3] < 0.2)
						mod[3] *= 2.5f;

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

								int iRGB = texureImage.getPixelRGBA(interU, interV);

								float[] rgb = {(iRGB&255)/255f, (iRGB >> 8&255)/255f, (iRGB >> 16&255)/255f, (iRGB >> 24&255)/255f};
								if((iRGB&0xff000000)!=0)
								{
									int iNoise = originalImage.getPixelRGBA(u, v);
									float[] noise = {(iNoise&255)/255f, (iNoise >> 8&255)/255f, (iNoise >> 16&255)/255f, (iNoise >> 24&255)/255f};

									for(int m = 0; m < 4; m++)
										rgb[m] = rgb[m]*mod[m]*noise[m];
									int[] irgb = {(int)(rgb[0]*255), (int)(rgb[1]*255), (int)(rgb[2]*255), (int)(rgb[3]*255)};

									int i2 = (irgb[0])+(irgb[1]<<8)+(irgb[2]<<16)+(irgb[3]<<24);

									// the final product may end up with low alpha, so we check for that
									int pre = finalTexture.getPixelRGBA(u, v) >> 24&255;

									// if we just set it, we also set alpha values, we gotta blend it
									blendPixel(finalTexture, u, v, i2);

									// if the image was blank, or the resulting alpha is lower than how it started,
									// we fix it.
									int post = finalTexture.getPixelRGBA(u, v);
									if(pre==0)
									{
										int color = (irgb[3]<<24)|(post&0x00ffffff);
										finalTexture.setPixelRGBA(u, v, color);
									}
									else if((post >> 24&255) < pre)
									{
										int color = (pre<<24)|(post&0x00ffffff);
										finalTexture.setPixelRGBA(u, v, color);
									}
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

	private void blendPixel(NativeImage image, int xIn, int yIn, int colIn)
	{
		int existing = image.getPixelRGBA(xIn, yIn);
		float alphaIn = (float)alpha(colIn)/255.0F;
		float blueIn = (float)blue(colIn)/255.0F;
		float greenIn = (float)green(colIn)/255.0F;
		float redIn = (float)red(colIn)/255.0F;
		float alphaOld = (float)alpha(existing)/255.0F;
		float blueOld = (float)blue(existing)/255.0F;
		float greenOld = (float)green(existing)/255.0F;
		float redOld = (float)red(existing)/255.0F;
		float oldMixFactor = 1.0F-alphaIn;
		float alphaOut = alphaIn*alphaIn+alphaOld*oldMixFactor;
		float blueOut = blueIn*alphaIn+blueOld*oldMixFactor;
		float greenOut = greenIn*alphaIn+greenOld*oldMixFactor;
		float redOut = redIn*alphaIn+redOld*oldMixFactor;
		if(alphaOut > 1.0F)
		{
			alphaOut = 1.0F;
		}

		if(blueOut > 1.0F)
		{
			blueOut = 1.0F;
		}

		if(greenOut > 1.0F)
		{
			greenOut = 1.0F;
		}

		if(redOut > 1.0F)
		{
			redOut = 1.0F;
		}

		int redOutInt = (int)(alphaOut*255.0F);
		int blueOutInt = (int)(blueOut*255.0F);
		int greenOutInt = (int)(greenOut*255.0F);
		int alphaOutInt = (int)(redOut*255.0F);
		image.setPixelRGBA(xIn, yIn, color(redOutInt, blueOutInt, greenOutInt, alphaOutInt));
	}
}