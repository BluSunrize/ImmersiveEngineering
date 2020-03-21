package blusunrize.immersiveengineering.api.shader;

import net.minecraft.util.ResourceLocation;

public class DynamicShaderLayer extends ShaderLayer
{
	public DynamicShaderLayer(ResourceLocation texture, int colour)
	{
		super(texture, colour);
	}

	@Override
	public boolean isDynamicLayer()
	{
		return true;
	}
}
