/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.client.model.pipeline.BlockInfo;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;

import java.lang.reflect.Field;

public class SmartLightingQuad extends BakedQuad
{
	private static Field parent;
	private static Field blockInfo;
	private static Field pose;

	static
	{
		try
		{
			blockInfo = VertexLighterFlat.class.getDeclaredField("blockInfo");
			blockInfo.setAccessible(true);
			pose = VertexLighterFlat.class.getDeclaredField("pose");
			pose.setAccessible(true);
			parent = QuadGatheringTransformer.class.getDeclaredField("parent");
			parent.setAccessible(true);
		} catch(Exception x)
		{
			x.printStackTrace();
		}
	}

	BlockPos blockPos;

	public SmartLightingQuad(int[] vertexDataIn, int tintIndexIn, Direction faceIn, TextureAtlasSprite spriteIn, BlockPos p)
	{
		super(vertexDataIn, tintIndexIn, faceIn, spriteIn, false);
		blockPos = p;
	}

	@Override
	public void pipe(IVertexConsumer consumer)
	{
		if(consumer instanceof VertexLighterFlat)
			super.pipe(new SmartVertexLighter((VertexLighterFlat)consumer));
		else
			super.pipe(consumer);
	}

	private class SmartVertexLighter extends VertexLighterFlat
	{

		public SmartVertexLighter(VertexLighterFlat base)
		{
			super(Minecraft.getInstance().getBlockColors());
			try
			{
				setParent((IVertexConsumer)SmartLightingQuad.parent.get(base));
				setTransform((Pose)SmartLightingQuad.pose.get(base));
				setVertexFormat(DefaultVertexFormat.BLOCK);
				BlockInfo info = (BlockInfo)SmartLightingQuad.blockInfo.get(base);
				setWorld(info.getWorld());
				setState(info.getState());
				setBlockPos(info.getBlockPos());
				updateBlockInfo();
			} catch(Exception x)
			{
				throw new RuntimeException(x);
			}
		}

		private byte oldLightmapLength;

		@Override
		protected void updateLightmap(float[] normal, float[] lightmap, float x, float y, float z)
		{
			BlockAndTintGetter world = blockInfo.getWorld();
			BlockPos here = blockPos.offset(Math.floor(x-normal[0]/2+0.5), Math.floor(y-normal[1]/2+0.5), Math.floor(z-normal[2]/2+0.5));
			lightmap[0] = world.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(here)/(float)0xF;
			lightmap[1] = world.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(here)/(float)0xF;
			oldLightmapLength = dataLength[lightmapIndex];
			dataLength[lightmapIndex] = 0;
		}

		@Override
		protected void updateColor(float[] normal, float[] color, float x, float y, float z, float tint, int multiplier)
		{
			dataLength[lightmapIndex] = oldLightmapLength;
			super.updateColor(normal, color, x, y, z, tint, multiplier);
		}
	}
}