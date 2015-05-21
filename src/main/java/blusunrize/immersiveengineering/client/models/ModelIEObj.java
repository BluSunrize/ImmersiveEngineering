package blusunrize.immersiveengineering.client.models;

import java.util.ArrayList;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.WavefrontObject;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public abstract class ModelIEObj
{
	public static ArrayList<ModelIEObj> existingStaticRenders = new ArrayList();
	
	private final String path;
	private WavefrontObject model;
	public ModelIEObj(String path)
	{
		this.path = path;
		this.model = ClientUtils.getModel(path);
		
		existingStaticRenders.add(this);
	}
	
	public WavefrontObject rebindModel()
	{
		model = ClientUtils.getModel(path);
		return model;
	}
	
	public void render(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix, boolean offsetLighting, String... renderedParts)
	{
		ClientUtils.renderStaticWavefrontModel(tile, model, tes, translationMatrix, rotationMatrix, offsetLighting, renderedParts);
	}
	
	public abstract IIcon getBlockIcon();
}
