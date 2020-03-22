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
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.function.IntFunction;

@OnlyIn(Dist.CLIENT)
public class IEShaderLayerCompositeTexture extends Texture
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
	public void loadTexture(@Nonnull IResourceManager resourceManager)
	{
		this.deleteGlTexture();
		IResource iresource = null;
		NativeImage finalTexture = null;
		NativeImage originalImage = null;
		try
		{
			iresource = resourceManager.getResource(this.canvasTexture);
			originalImage = NativeImage.read(iresource.getInputStream());
			int canvasWidth = originalImage.getWidth();
			int canvasHeight = originalImage.getHeight();

			finalTexture = new NativeImage(canvasWidth, canvasHeight, true);
			int layer = 0;

			while(layer < 17&&layer < this.layers.length)
			{
				IResource iresource1 = null;

				try
				{
					String texPath = this.layers[layer].getTexture().getPath();

					if(!texPath.startsWith("textures/"))
						texPath = "textures/"+texPath;
					if(!texPath.endsWith(".png"))
						texPath += ".png";
					String texture = this.layers[layer].getTexture().getNamespace()+":"+texPath;
					Vector4f colour = this.layers[layer].getColor();

					iresource1 = resourceManager.getResource(new ResourceLocation(texture));
					NativeImage texureImage = NativeImage.read(iresource1.getInputStream());

					float[] mod = new float[4];
					colour.get(mod);
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
									//finalTexture.setPixelRGBA(u, v, i2);
									finalTexture.blendPixel(u, v, i2);

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
				} finally
				{
					IOUtils.closeQuietly(iresource1);
				}

				++layer;
			}
			TextureUtil.prepareImage(this.getGlTextureId(), 0, finalTexture.getWidth(), finalTexture.getHeight());
			finalTexture.uploadTextureSub(0, 0, 0, 0, 0, finalTexture.getWidth(), finalTexture.getHeight(), false, false, false);
		} catch(IOException ioexception)
		{
			IELogger.error("Couldn't load layered image", ioexception);
		} finally
		{
			IOUtils.closeQuietly(iresource);
			if(originalImage!=null)
				originalImage.close();
			if(finalTexture!=null)
				finalTexture.close();
		}
	}
}