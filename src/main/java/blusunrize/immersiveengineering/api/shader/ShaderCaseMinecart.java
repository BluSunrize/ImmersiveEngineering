/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShaderCaseMinecart extends ShaderCase
{
	public static Set<Class<? extends EntityMinecart>> invalidMinecartClasses = new HashSet();
	public boolean[][] renderSides;
	public boolean[] mirrorSideForPass;

	public ShaderCaseMinecart(ShaderLayer... layers)
	{
		super(layers);
		initBooleans();
	}

	public ShaderCaseMinecart(Collection<ShaderLayer> layers)
	{
		super(layers);
		initBooleans();
	}

	@Override
	public ShaderCase addLayers(ShaderLayer... addedLayers)
	{
		ShaderCase sCase = super.addLayers(addedLayers);
		initBooleans();
		return sCase;
	}

	private void initBooleans()
	{
		mirrorSideForPass = new boolean[getLayers().length];
		renderSides = new boolean[getLayers().length][7];
		for(int i = 0; i < mirrorSideForPass.length; i++)
		{
			mirrorSideForPass[i] = true;
			for(int j = 0; j < 7; j++)
				renderSides[i][j] = true;
		}
	}

	@Override
	public String getShaderType()
	{
		return "immersiveengineering:minecart";
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean stitchIntoSheet()
	{
		return false;
	}

	@Override
	public boolean renderModelPartForPass(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		return renderSides[pass][Integer.parseInt(modelPart)];
	}
}