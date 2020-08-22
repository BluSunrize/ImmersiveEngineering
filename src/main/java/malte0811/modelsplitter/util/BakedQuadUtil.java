package malte0811.modelsplitter.util;

import com.google.common.base.Preconditions;
import malte0811.modelsplitter.math.Vec3d;
import malte0811.modelsplitter.model.Polygon;
import malte0811.modelsplitter.model.Vertex;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Type;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Usage;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import java.util.ArrayList;
import java.util.List;

public class BakedQuadUtil
{
	private static int getOffset(VertexFormatElement.Usage usage, VertexFormatElement.Type type)
	{
		int offset = 0;
		for(VertexFormatElement e : DefaultVertexFormats.BLOCK.getElements())
		{
			if(e.getUsage()==usage&&e.getType()==type)
			{
				return offset/4;
			}
			else
			{
				offset += e.getSize();
			}
		}
		throw new IllegalStateException("Did not find element with usage "+usage.name()+" and type "+type.name());
	}

	public static Polygon<TextureAtlasSprite> toPolygon(BakedQuad quad)
	{
		List<Vertex> vertices = new ArrayList<>(4);
		final int posOffset = getOffset(Usage.POSITION, Type.FLOAT);
		final int uvOffset = getOffset(Usage.UV, Type.FLOAT);
		final int normalOffset = getOffset(Usage.NORMAL, Type.BYTE);
		for(int v = 0; v < 4; ++v)
		{
			final int baseOffset = v*DefaultVertexFormats.BLOCK.getSize()/4;
			int packedNormal = quad.getVertexData()[normalOffset+baseOffset];
			final Vec3d normalVec = new Vec3d(
					(byte)(packedNormal >> 16),
					(byte)(packedNormal >> 8),
					(byte)(packedNormal)
			).normalize();
			final double[] uv = {
					Float.intBitsToFloat(quad.getVertexData()[uvOffset+baseOffset]),
					Float.intBitsToFloat(quad.getVertexData()[uvOffset+baseOffset+1])
			};
			final Vec3d pos = new Vec3d(
					Float.intBitsToFloat(quad.getVertexData()[baseOffset+posOffset]),
					Float.intBitsToFloat(quad.getVertexData()[baseOffset+posOffset+1]),
					Float.intBitsToFloat(quad.getVertexData()[baseOffset+posOffset+2])
			);
			vertices.add(new Vertex(pos, normalVec, uv));
		}
		return new Polygon<>(vertices, quad.func_187508_a());
	}

	public static BakedQuad toBakedQuad(Polygon<TextureAtlasSprite> poly, VertexFormat format)
	{
		Preconditions.checkArgument(poly.getPoints().size()==4);
		BakedQuadBuilder quadBuilder = new BakedQuadBuilder(poly.getTexture());
		final Vec3d normal = poly.getPoints().get(0).getNormal();
		quadBuilder.setQuadOrientation(Direction.getFacingFromVector(normal.get(0), normal.get(1), normal.get(2)));
		for(Vertex v : poly.getPoints())
		{
			List<VertexFormatElement> elements = format.getElements();
			for(int i = 0, elementsSize = elements.size(); i < elementsSize; i++)
			{
				VertexFormatElement element = elements.get(i);
				switch(element.getUsage())
				{
					case POSITION:
						putVec3d(quadBuilder, i, v.getPosition());
						break;
					case NORMAL:
						putVec3d(quadBuilder, i, v.getNormal());
						break;
					case COLOR:
						//TODO?
						quadBuilder.put(i, 1, 1, 1, 1);
						break;
					case UV:
						if(element.getType()==Type.FLOAT)
							quadBuilder.put(
									i,
									(float)v.getU(),
									(float)v.getV()
							);
						else
							quadBuilder.put(i, 0, 0);
						break;
					case PADDING:
						quadBuilder.put(i, 0);
						break;
				}
			}
		}
		return quadBuilder.build();
	}

	private static void putVec3d(BakedQuadBuilder out, int index, Vec3d data)
	{
		out.put(
				index,
				(float)data.get(0),
				(float)data.get(1),
				(float)data.get(2)
		);
	}
}
