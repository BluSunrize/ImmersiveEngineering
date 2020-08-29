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
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.client.model.obj.OBJModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureCoordinateRemapper
{
	private final List<Vec2f> texCoords;
	private final HashMap<Integer, Vec2f> backup;
	private final ShaderCase shaderCase;
	private final boolean flipV;

	private ShaderLayer shaderLayer;

	public TextureCoordinateRemapper(OBJModel model, ShaderCase shaderCase)
	{
		this.texCoords = OBJHelper.getTexCoords(model);
		this.shaderCase = shaderCase;
		this.backup = new HashMap<>();
		this.flipV = model.flipV;
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
	 * @return whether to render the face or skip it
	 */
	public boolean remapCoord(int[][] face)
	{
		if(shaderCase==null||texCoords.size() < 1)
			return true;

		double[] texBounds = shaderLayer.getTextureBounds();
		double[] cutBounds = shaderLayer.getCutoutBounds();
		if(texBounds==null&&cutBounds==null)
			return true;

		for(int i = 0; i < 4; i++)
		{
			int[] index = face[Math.min(i, face.length-1)];
			if(index.length < 2)
				continue;

			int texIndex = index[1];
			if(this.backup.containsKey(texIndex)) // if this coordinate has already been modified, abort
				continue;
			Vec2f texCoord = texCoords.get(texIndex);
			this.backup.put(texIndex, texCoord);

			if(flipV)
				texCoord = new Vec2f(texCoord.x, 1-texCoord.y);

			if(texBounds!=null)
			{
				//if any uvs are outside the layers bounds
				if(texBounds[0] > texCoord.x||texCoord.x > texBounds[2]||texBounds[1] > texCoord.y||texCoord.y > texBounds[3])
				{
					texCoords.set(texIndex, texCoord);
					return false;
				}

				double dU = texBounds[2]-texBounds[0];
				double dV = texBounds[3]-texBounds[1];
				//Rescaling to the partial bounds that the texture represents
				texCoord = new Vec2f(
						(float)((texCoord.x-texBounds[0])/dU),
						(float)((texCoord.y-texBounds[1])/dV)
				);
			}
			//Rescaling to the selective area of the texture that is used

			if(cutBounds!=null)
			{
				double dU = cutBounds[2]-cutBounds[0];
				double dV = cutBounds[3]-cutBounds[1];
				texCoord = new Vec2f(
						(float)(cutBounds[0]+dU*texCoord.x),
						(float)(cutBounds[1]+dV*texCoord.y)
				);
			}
			if(flipV)
				texCoord = new Vec2f(texCoord.x, 1-texCoord.y);
			texCoords.set(texIndex, texCoord);
		}
		return true;
	}

	/**
	 * Reset any coordinates that were manipulated to their backed up state
	 */
	public void resetCoords()
	{
		for(Map.Entry<Integer, Vec2f> entry : this.backup.entrySet())
			this.texCoords.set(entry.getKey(), entry.getValue());
		this.backup.clear();
	}
}
