package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;

import java.util.Collection;

public class ShaderCaseShield extends ShaderCase
{
	public ShaderCaseShield(ShaderLayer... layers)
	{
		super(layers);
	}
	public ShaderCaseShield(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public String getShaderType()
	{
		return "immersiveengineering:shield";
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean renderModelPartForPass(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if("flash".equals(modelPart) || "shock".equals(modelPart))
			return pass==getLayers().length-1;
		return true;
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
	{
	}
}