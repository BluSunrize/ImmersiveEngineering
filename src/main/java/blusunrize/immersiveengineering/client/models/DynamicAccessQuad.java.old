/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.lwjgl.util.vector.Vector3f;

import java.util.concurrent.TimeUnit;

public class DynamicAccessQuad extends UnpackedBakedQuad
{
	private Cache<Integer, DynamicAccessQuad> cachedVariations = CacheBuilder.newBuilder()
			.maximumSize(100).expireAfterAccess(60, TimeUnit.SECONDS).build();

	private int arraypos_POS = -1;
	private final String name;

	public DynamicAccessQuad(float[][][] unpackedData, String name, int tint, EnumFacing orientation, TextureAtlasSprite texture, boolean applyDiffuseLighting, VertexFormat format)
	{
		super(unpackedData, tint, orientation, texture, applyDiffuseLighting, format);

		this.name = name;
		for(int i=0; i<format.getElementCount(); i++)
			if(format.getElement(i)==DefaultVertexFormats.POSITION_3F)
			{
				arraypos_POS = i;
				break;
			}
	}

	public String getName()
	{
		return name;
	}

	public DynamicAccessQuad applyMatrix(Matrix4 matrix)
	{
		if(matrix==null)
			return this;
		if(arraypos_POS>=0)
			try
			{
				return cachedVariations.get(matrix.hashCode(), () -> {
					DynamicAccessQuad ret = new DynamicAccessQuad(getDataCopy(), name, tintIndex, face, sprite, applyDiffuseLighting, format);
					for(int vertex = 0; vertex < 4; vertex++)
					{
						Vector3f vec = new Vector3f(unpackedData[vertex][arraypos_POS][0], unpackedData[vertex][arraypos_POS][1], unpackedData[vertex][arraypos_POS][2]);
						vec = matrix.apply(vec);
						ret.unpackedData[vertex][arraypos_POS][0] = vec.x;
						ret.unpackedData[vertex][arraypos_POS][1] = vec.y;
						ret.unpackedData[vertex][arraypos_POS][2] = vec.z;
					}
					return ret;
				});
			}catch(Exception e){}
		return this;
	}

	private float[][][] getDataCopy()
	{
		float[][][] dataCopy = new float[4][format.getElementCount()][4];
		for(int i=0; i<4; i++)
			for(int j=0; j<format.getElementCount(); j++)
				for(int k=0; k<4; k++)
					dataCopy[i][j][k] = unpackedData[i][j][k];
		return dataCopy;
	}

	public static class Builder implements IVertexConsumer
	{
		private final VertexFormat format;
		private final float[][][] unpackedData;
		private final String name;
		private int tint = -1;
		private EnumFacing orientation;
		private TextureAtlasSprite texture;
		private boolean applyDiffuseLighting = true;

		private int vertices = 0;
		private int elements = 0;
		private boolean full = false;
		private boolean contractUVs = false;

		public Builder(VertexFormat format, String name)
		{
			this.format = format;
			unpackedData = new float[4][format.getElementCount()][4];
			this.name = name;
		}

		@Override
		public VertexFormat getVertexFormat()
		{
			return format;
		}

		public void setContractUVs(boolean value)
		{
			this.contractUVs = value;
		}
		@Override
		public void setQuadTint(int tint)
		{
			this.tint = tint;
		}

		@Override
		public void setQuadOrientation(EnumFacing orientation)
		{
			this.orientation = orientation;
		}

		// FIXME: move (or at least add) into constructor
		@Override
		public void setTexture(TextureAtlasSprite texture)
		{
			this.texture = texture;
		}

		@Override
		public void setApplyDiffuseLighting(boolean diffuse)
		{
			this.applyDiffuseLighting = diffuse;
		}

		@Override
		public void put(int element, float... data)
		{
			for(int i = 0; i < 4; i++)
			{
				if(i < data.length)
				{
					unpackedData[vertices][element][i] = data[i];
				}
				else
				{
					unpackedData[vertices][element][i] = 0;
				}
			}
			elements++;
			if(elements == format.getElementCount())
			{
				vertices++;
				elements = 0;
			}
			if(vertices == 4)
			{
				full = true;
			}
		}

		private final float eps = 1f / 0x100;

		public UnpackedBakedQuad build()
		{
			if(!full)
			{
				throw new IllegalStateException("not enough data");
			}
			if(texture == null)
			{
				throw new IllegalStateException("texture not set");
			}
			if(contractUVs)
			{
				float tX = texture.getOriginX() / texture.getMinU();
				float tY = texture.getOriginY() / texture.getMinV();
				float tS = tX > tY ? tX : tY;
				float ep = 1f / (tS * 0x100);
				int uve = 0;
				while(uve < format.getElementCount())
				{
					VertexFormatElement e = format.getElement(uve);
					if(e.getUsage() == VertexFormatElement.EnumUsage.UV && e.getIndex() == 0)
					{
						break;
					}
					uve++;
				}
				if(uve == format.getElementCount())
				{
					throw new IllegalStateException("Can't contract UVs: format doesn't contain UVs");
				}
				float[] uvc = new float[4];
				for(int v = 0; v < 4; v++)
				{
					for(int i = 0; i < 4; i++)
					{
						uvc[i] += unpackedData[v][uve][i] / 4;
					}
				}
				for(int v = 0; v < 4; v++)
				{
					for (int i = 0; i < 4; i++)
					{
						float uo = unpackedData[v][uve][i];
						float un = uo * (1 - eps) + uvc[i] * eps;
						float ud = uo - un;
						float aud = ud;
						if(aud < 0) aud = -aud;
						if(aud < ep) // not moving a fraction of a pixel
						{
							float udc = uo - uvc[i];
							if(udc < 0) udc = -udc;
							if(udc < 2 * ep) // center is closer than 2 fractions of a pixel, don't move too close
							{
								un = (uo + uvc[i]) / 2;
							}
							else // move at least by a fraction
							{
								un = uo + (ud < 0 ? ep : -ep);
							}
						}
						unpackedData[v][uve][i] = un;
					}
				}
			}
			return new DynamicAccessQuad(unpackedData, name, tint, orientation, texture, applyDiffuseLighting, format);
		}
	}
}