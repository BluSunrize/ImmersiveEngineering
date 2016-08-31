package blusunrize.immersiveengineering.client.models;

import java.lang.reflect.Field;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.BlockInfo;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;

public class SmartLightingQuad extends BakedQuad
{
	private static Field parent;
	private static Field blockInfo;
	static
	{
		try
		{
			blockInfo = VertexLighterFlat.class.getDeclaredField("blockInfo");
			blockInfo.setAccessible(true);
			parent = QuadGatheringTransformer.class.getDeclaredField("parent");
			parent.setAccessible(true);
		}
		catch (Exception x)
		{
			x.printStackTrace();
		}
	}
	BlockPos blockPos;
	int[][] relativePos;
	boolean ignoreLight;
	public SmartLightingQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, VertexFormat format, BlockPos p, boolean noLight)
	{
		super(vertexDataIn, tintIndexIn, faceIn, spriteIn, false, format);
		ignoreLight = noLight;
		blockPos = p;
		relativePos = new int[4][];
		for (int i = 0;i<4;i++)
			relativePos[i] = new int[]{(int)Math.floor(Float.intBitsToFloat(vertexDataIn[7*i])),
					(int)Math.floor(Float.intBitsToFloat(vertexDataIn[7*i+1])),
					(int)Math.floor(Float.intBitsToFloat(vertexDataIn[7*i+2]))
		};
	}
	@Override
	public void pipe(IVertexConsumer consumer)
	{
		IBlockAccess world = null;
		BlockInfo info = null;
		if (consumer instanceof VertexLighterFlat)
		{
			try
			{
				info = (BlockInfo) blockInfo.get(consumer);
				world = info.getWorld();
				if (world instanceof ChunkCache)
					world = ((ChunkCache)world).worldObj;
				consumer = (IVertexConsumer) parent.get(consumer);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
		consumer.setQuadOrientation(this.getFace());
		if(this.hasTintIndex())
			consumer.setQuadTint(this.getTintIndex());
		float[] data = new float[4];
		VertexFormat format = consumer.getVertexFormat();
		int count = format.getElementCount();
		int[] eMap = LightUtil.mapFormats(format, DefaultVertexFormats.ITEM);
		int itemCount = DefaultVertexFormats.ITEM.getElementCount();
		eMap[eMap.length-1] = 2;
		for(int v = 0; v < 4; v++)
			for(int e = 0; e < count; e++)
				if(eMap[e] != itemCount)
				{
					if (world!=null&&!(world instanceof ChunkCache)&&format.getElement(e).getUsage()==EnumUsage.UV&&format.getElement(e).getType()==EnumType.SHORT)//lightmap is UV with 2 shorts
					{
						BlockPos here = blockPos.add(relativePos[v][0], relativePos[v][1], relativePos[v][2]);
						int brightness = world.getCombinedLight(here, 0);
						if (ignoreLight) {
							data[0] = ((float)(15*0x20))/0xFFFF;
							data[1] = ((float)(15*0x20))/0xFFFF;
						}
						else
						{
							data[0] = ((float)((brightness >> 0x04) & 0xF) * 0x20) / 0xFFFF;
							data[1] = ((float)((brightness >> 0x14) & 0xF) * 0x20) / 0xFFFF;
						}
					}
					else
						LightUtil.unpack(this.getVertexData(), data, DefaultVertexFormats.ITEM, v, eMap[e]);
					consumer.put(e, data);
				}
				else
					consumer.put(e, 0);
	}
}