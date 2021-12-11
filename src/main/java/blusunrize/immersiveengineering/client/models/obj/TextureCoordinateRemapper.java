/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.Polygon;
import malte0811.modelsplitter.model.UVCoords;
import malte0811.modelsplitter.model.Vertex;

import java.util.ArrayList;
import java.util.List;

public class TextureCoordinateRemapper
{
	private final ShaderCase shaderCase;

	private ShaderLayer shaderLayer;

	public TextureCoordinateRemapper(ShaderCase shaderCase)
	{
		this.shaderCase = shaderCase;
	}

	/**
	 * Select the shader layer for the current renderpass
	 *
	 * @param pass
	 */
	public void setRenderPass(int pass)
	{
		if(this.shaderCase!=null)
			this.shaderLayer = this.shaderCase.getLayers()[pass];
	}

	/**
	 * Remap texture coordinates for the given face
	 *
	 * @param face
	 * @return the actual face to render (may be the passed one or a different one), or null if the face should be skipped
	 */
	public Polygon<OBJMaterial> remapCoord(Polygon<OBJMaterial> face)
	{
		if(shaderCase==null)
			return face;

		double[] texBounds = shaderLayer.getTextureBounds();
		double[] cutBounds = shaderLayer.getCutoutBounds();
		if(texBounds==null&&cutBounds==null)
			return face;

		List<Vertex> newPoints = new ArrayList<>();
		for(var vertex : face.getPoints())
		{
			UVCoords texCoord = vertex.uv();

			if(texBounds!=null)
			{
				//if any uvs are outside the layers bounds
				if(texBounds[0] > texCoord.u()||texCoord.u() > texBounds[2]||texBounds[1] > texCoord.v()||texCoord.v() > texBounds[3])
					return null;

				double dU = texBounds[2]-texBounds[0];
				double dV = texBounds[3]-texBounds[1];
				//Rescaling to the partial bounds that the texture represents
				texCoord = new UVCoords((texCoord.u()-texBounds[0])/dU, (texCoord.v()-texBounds[1])/dV);
			}
			//Rescaling to the selective area of the texture that is used

			if(cutBounds!=null)
			{
				double dU = cutBounds[2]-cutBounds[0];
				double dV = cutBounds[3]-cutBounds[1];
				texCoord = new UVCoords(cutBounds[0]+dU*texCoord.u(), cutBounds[1]+dV*texCoord.v());
			}
			if(texCoord!=vertex.uv())
				newPoints.add(new Vertex(vertex.position(), vertex.position(), texCoord));
			else
				newPoints.add(vertex);
		}
		return new Polygon<>(newPoints, face.getTexture());
	}
}
