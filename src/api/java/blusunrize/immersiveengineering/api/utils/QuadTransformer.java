/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class QuadTransformer implements Function<BakedQuad, BakedQuad>
{
	@Nonnull
	private final Transformation transform;
	@Nullable
	private final Int2IntFunction colorTransform;
	private BakedQuadBuilder currentQuadBuilder;
	private final IVertexConsumer transformer = createConsumer(DefaultVertexFormat.BLOCK);

	public QuadTransformer(Transformation transform, @Nullable Int2IntFunction colorTransform)
	{
		this.transform = transform;
		this.colorTransform = colorTransform;
	}

	@Override
	public BakedQuad apply(BakedQuad q)
	{
		currentQuadBuilder = new BakedQuadBuilder();
		q.pipe(transformer);
		return currentQuadBuilder.build();
	}

	private IVertexConsumer createConsumer(VertexFormat f)
	{
		int posPos = -1;
		int normPos = -1;
		int colorPos = -1;
		for(int i = 0; i < f.getElements().size(); i++)
			if(f.getElements().get(i).getUsage()==VertexFormatElement.Usage.POSITION)
				posPos = i;
			else if(f.getElements().get(i).getUsage()==VertexFormatElement.Usage.NORMAL)
				normPos = i;
			else if(f.getElements().get(i).getUsage()==VertexFormatElement.Usage.COLOR)
				colorPos = i;
		if(posPos==-1)
			return null;
		final int posPosFinal = posPos;
		final int normPosFinal = normPos;
		final int colorPosFinal = colorPos;
		return new IVertexConsumer()
		{
			int tintIndex = -1;

			@Nonnull
			@Override
			public VertexFormat getVertexFormat()
			{
				return f;
			}

			@Override
			public void setQuadTint(int tint)
			{
				currentQuadBuilder.setQuadTint(tint);
				tintIndex = tint;
			}

			@Override
			public void setQuadOrientation(@Nonnull Direction orientation)
			{
				Vec3i normal = orientation.getNormal();
				Vector3f newFront = new Vector3f(normal.getX(), normal.getY(), normal.getZ());
				transform.transformNormal(newFront);
				Direction newOrientation = Direction.getNearest(newFront.x(), newFront.y(), newFront.z());
				currentQuadBuilder.setQuadOrientation(newOrientation);
			}

			@Override
			public void setApplyDiffuseLighting(boolean diffuse)
			{
				currentQuadBuilder.setApplyDiffuseLighting(diffuse);
			}

			@Override
			public void setTexture(@Nonnull TextureAtlasSprite texture)
			{
				currentQuadBuilder.setTexture(texture);
			}

			@Override
			public void put(int element, @Nonnull float... data)
			{
				if(element==posPosFinal&&transform!=null)
				{
					Vector4f newPos = new Vector4f(data[0], data[1], data[2], 1);
					transform.transformPosition(newPos);
					data = new float[3];
					data[0] = newPos.x();
					data[1] = newPos.y();
					data[2] = newPos.z();
				}
				else if(element==normPosFinal)
				{
					Vector3f newNormal = new Vector3f(data[0], data[1], data[2]);
					transform.transformNormal(newNormal);
					data = new float[3];
					data[0] = newNormal.x();
					data[1] = newNormal.y();
					data[2] = newNormal.z();
				}
				else if(element==colorPosFinal)
				{
					if(tintIndex!=-1&&colorTransform!=null)
					{
						int multiplier = colorTransform.apply(tintIndex);
						if(multiplier!=0)
						{
							float r = (float)(multiplier >> 16&255)/255.0F;
							float g = (float)(multiplier >> 8&255)/255.0F;
							float b = (float)(multiplier&255)/255.0F;
							float[] oldData = data;
							data = new float[4];
							data[0] = oldData[0]*r;
							data[1] = oldData[1]*g;
							data[2] = oldData[2]*b;
							data[3] = oldData[3];
						}
					}
				}
				currentQuadBuilder.put(element, data);
			}
		};
	}
}
