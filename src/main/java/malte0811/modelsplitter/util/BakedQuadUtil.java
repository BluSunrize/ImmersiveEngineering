package malte0811.modelsplitter.util;

import com.google.common.base.Preconditions;
import malte0811.modelsplitter.math.Vec3d;
import malte0811.modelsplitter.model.Polygon;
import malte0811.modelsplitter.model.Vertex;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IModelTransform;
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

	public static BakedQuad toBakedQuad(Polygon<TextureAtlasSprite> poly, IModelTransform transform, VertexFormat format)
	{
		Preconditions.checkArgument(poly.getPoints().size()==4);
		BakedQuadBuilder quadBuilder = new BakedQuadBuilder(poly.getTexture());
		TransformationMatrix rotation = transform.getRotation().blockCenterToCorner();
		Vector3f normal = new Vector3f();
		for(Vertex v : poly.getPoints())
		{
			List<VertexFormatElement> elements = format.getElements();
			Vector4f pos = new Vector4f();
			pos.set(toArray(v.getPosition(), 4));
			normal.set(toArray(v.getNormal(), 3));
			rotation.transformPosition(pos);
			rotation.transformNormal(normal);
			pos.perspectiveDivide();
			for(int i = 0, elementsSize = elements.size(); i < elementsSize; i++)
			{
				VertexFormatElement element = elements.get(i);
				switch(element.getUsage())
				{
					case POSITION:
						quadBuilder.put(i, pos.getX(), pos.getY(), pos.getZ());
						break;
					case NORMAL:
						quadBuilder.put(i, normal.getX(), normal.getY(), normal.getZ());
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
		quadBuilder.setQuadOrientation(Direction.getFacingFromVector(normal.getX(), normal.getY(), normal.getZ()));
		return quadBuilder.build();
	}

	private static float[] toArray(Vec3d vec, int length)
	{
		float[] ret = new float[length];
		for(int i = 0; i < 3; ++i)
			ret[i] = (float)vec.get(i);
		for(int i = 3; i < length; ++i)
			ret[i] = 1;
		return ret;
	}
}
