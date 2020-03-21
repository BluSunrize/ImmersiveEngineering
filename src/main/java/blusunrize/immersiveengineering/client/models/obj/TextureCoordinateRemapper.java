package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import net.minecraftforge.client.model.obj.OBJModel2;

import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureCoordinateRemapper
{
	private final List<Vector2f> texCoords;
	private final HashMap<Integer, Vector2f> backup;
	private final ShaderCase shaderCase;
	private final boolean flipV;

	private ShaderLayer shaderLayer;

	public TextureCoordinateRemapper(OBJModel2 model, ShaderCase shaderCase)
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
			Vector2f texCoord = texCoords.get(texIndex);
			this.backup.put(texIndex, new Vector2f(texCoord));

			if(flipV)
				texCoord.y = 1-texCoord.y;

			if(texBounds!=null)
			{
				//if any uvs are outside the layers bounds
				if(texBounds[0] > texCoord.x||texCoord.x > texBounds[2]||texBounds[1] > texCoord.y||texCoord.y > texBounds[3])
				{
					if(flipV) // early exit, flip v back
						texCoord.y = 1-texCoord.y;
					return false;
				}

				double dU = texBounds[2]-texBounds[0];
				double dV = texBounds[3]-texBounds[1];
				//Rescaling to the partial bounds that the texture represents
				texCoord.x = (float)((texCoord.x-texBounds[0])/dU);
				texCoord.y = (float)((texCoord.y-texBounds[1])/dV);
			}
			//Rescaling to the selective area of the texture that is used

			if(cutBounds!=null)
			{
				double dU = cutBounds[2]-cutBounds[0];
				double dV = cutBounds[3]-cutBounds[1];
				texCoord.x = (float)(cutBounds[0]+dU*texCoord.x);
				texCoord.y = (float)(cutBounds[1]+dV*texCoord.y);
			}
			if(flipV)
				texCoord.y = 1-texCoord.y;
		}
		return true;
	}

	/**
	 * Reset any coordinates that were manipulated to their backed up state
	 */
	public void resetCoords()
	{
		for(Map.Entry<Integer, Vector2f> entry : this.backup.entrySet())
			this.texCoords.get(entry.getKey()).set(entry.getValue());
		this.backup.clear();
	}
}
