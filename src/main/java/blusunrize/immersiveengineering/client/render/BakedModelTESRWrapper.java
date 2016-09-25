package blusunrize.immersiveengineering.client.render;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.Vec3d;

class BakedModelTESRWrapper {
	public TexturedQuad[] modelQuads;
	public BakedModelTESRWrapper(IBakedModel model, IBlockState state) {
		List<BakedQuad> bakedQuads = model.getQuads(state, null, 0);
		modelQuads = new TexturedQuad[bakedQuads.size()];
		for (int i = 0;i<modelQuads.length;i++) {
			PositionTextureVertex[] vertices = new PositionTextureVertex[4];
			int[] vData = bakedQuads.get(i).getVertexData();
			VertexFormat format = bakedQuads.get(i).getFormat();
			int size = format.getIntegerSize();
			int uv = format.getUvOffsetById(0)/4;
			for (int j = 0;j<4;j++) {
				Vec3d pos = new Vec3d(Float.intBitsToFloat(vData[size*j]),
						Float.intBitsToFloat(vData[size*j+1]),
						Float.intBitsToFloat(vData[size*j+2]));//assume that pos are the first 3 elements
				vertices[j] = new PositionTextureVertex(pos, 
						Float.intBitsToFloat(vData[size*j+uv]), Float.intBitsToFloat(vData[size*j+uv+1]));
			}
			modelQuads[i] = new TexturedQuad(vertices);
		}
	}
	public void render(VertexBuffer renderer, int brightness)
	{
		int l1 = (brightness >> 0x10) & 0xFFFF;
		int l2 = brightness & 0xFFFF;
		for (TexturedQuad texturedquad : this.modelQuads)
		{
			for (int i = 0; i < 4; ++i)
			{
				PositionTextureVertex positiontexturevertex = texturedquad.vertexPositions[i];
				renderer
				.pos(positiontexturevertex.vector3D.xCoord, positiontexturevertex.vector3D.yCoord, positiontexturevertex.vector3D.zCoord)
				.color(255, 255, 255, 255)
				.tex((double)positiontexturevertex.texturePositionX, (double)positiontexturevertex.texturePositionY)
				.lightmap(l1, l2)
				.endVertex();
			}

		}
	}
}
