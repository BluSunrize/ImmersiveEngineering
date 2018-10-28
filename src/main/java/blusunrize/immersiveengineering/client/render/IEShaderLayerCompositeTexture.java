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
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.IntFunction;

@SideOnly(Side.CLIENT)
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
	public void loadTexture(IResourceManager resourceManager)
	{
		this.deleteGlTexture();
		IResource iresource = null;
		BufferedImage bufferedimage;
		BufferedImage scaledImage;
		label255:
		{
			try
			{
				iresource = resourceManager.getResource(this.canvasTexture);
				BufferedImage canvasImage = TextureUtil.readBufferedImage(iresource.getInputStream());
				int imageType = canvasImage.getType();
				if(imageType==0)
					imageType = 6;
				int canvasWidth = canvasImage.getWidth();
				int canvasHeight = canvasImage.getHeight();

				bufferedimage = new BufferedImage(canvasWidth, canvasHeight, imageType);
				int layer = 0;

				while(true)
				{
					if(layer >= 17||layer >= this.layers.length)
						break label255;

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
						BufferedImage texureImage = TextureUtil.readBufferedImage(iresource1.getInputStream());

						scaledImage = new BufferedImage(canvasWidth, canvasHeight, imageType);

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

									int iRGB = texureImage.getRGB(interU, interV);

									float[] rgb = {(iRGB >> 16&255)/255f, (iRGB >> 8&255)/255f, (iRGB&255)/255f, (iRGB >> 24&255)/255f};
									if((iRGB&-16777216)!=0)
									{
										int iNoise = canvasImage.getRGB(u, v);
										float[] noise = {(iNoise >> 16&255)/255f, (iNoise >> 8&255)/255f, (iNoise&255)/255f, (iNoise >> 24&255)/255f};

										for(int m = 0; m < 4; m++)
											rgb[m] = rgb[m]*mod[m]*noise[m];
										int[] irgb = {(int)(rgb[0]*255), (int)(rgb[1]*255), (int)(rgb[2]*255), (int)(rgb[3]*255)};

										int i2 = (irgb[0]<<16)+(irgb[1]<<8)+(irgb[2])+(irgb[3]<<24);
										scaledImage.setRGB(u, v, i2);
									}
								}
						} catch(Exception e)
						{
							e.printStackTrace();
						}
						bufferedimage.getGraphics().drawImage(scaledImage, 0, 0, null);
					} finally
					{
						IOUtils.closeQuietly(iresource1);
					}

					++layer;
				}
			} catch(IOException ioexception)
			{
				IELogger.error("Couldn't load layered image", ioexception);
			} finally
			{
				IOUtils.closeQuietly(iresource);
			}

			return;
		}
		TextureUtil.uploadTextureImage(this.getGlTextureId(), bufferedimage);
	}
}