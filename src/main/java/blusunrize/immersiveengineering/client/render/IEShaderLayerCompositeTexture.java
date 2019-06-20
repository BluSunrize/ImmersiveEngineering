/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.IntFunction;

@OnlyIn(Dist.CLIENT)
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

			while(layer < 17||layer < this.layers.length)
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
					int colour = this.layers[layer].getColour();

					iresource1 = resourceManager.getResource(new ResourceLocation(texture));
					NativeImage texureImage = NativeImage.read(iresource1.getInputStream());

					float[] mod = {(colour >> 16&255)/255f, (colour >> 8&255)/255f, (colour&255)/255f, (colour >> 24&255)/255f};

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

								float[] rgb = {(iRGB >> 16&255)/255f, (iRGB >> 8&255)/255f, (iRGB&255)/255f, (iRGB >> 24&255)/255f};
								if((iRGB&-16777216)!=0)
								{
									int iNoise = originalImage.getPixelRGBA(u, v);
									float[] noise = {(iNoise >> 16&255)/255f, (iNoise >> 8&255)/255f, (iNoise&255)/255f, (iNoise >> 24&255)/255f};

									for(int m = 0; m < 4; m++)
										rgb[m] = rgb[m]*mod[m]*noise[m];
									int[] irgb = {(int)(rgb[0]*255), (int)(rgb[1]*255), (int)(rgb[2]*255), (int)(rgb[3]*255)};

									int i2 = (irgb[0]<<16)+(irgb[1]<<8)+(irgb[2])+(irgb[3]<<24);
									finalTexture.setPixelRGBA(u, v, i2);
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
			TextureUtil.allocateTextureImpl(this.getGlTextureId(), 0, finalTexture.getWidth(), finalTexture.getHeight());
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