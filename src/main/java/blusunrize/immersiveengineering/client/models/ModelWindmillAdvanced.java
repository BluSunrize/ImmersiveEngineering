package blusunrize.immersiveengineering.client.models;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * windmill - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelWindmillAdvanced extends ModelBase
{
	public ModelRenderer axel;
	public ModelRenderer rod3;
	public ModelRenderer rod2;
	public ModelRenderer rod4;
	public ModelRenderer rod1;
	public ModelRenderer rod5;
	public ModelRenderer rod6;
	public ModelRenderer rod7;
	public ModelRenderer rod8;
	public ModelRenderer sail1;
	public ModelRenderer sail2;
	public ModelRenderer sail3;
	public ModelRenderer sail4;
	public ModelRenderer sail5;
	public ModelRenderer sail6;
	public ModelRenderer sail7;
	public ModelRenderer sail8;

	public ModelWindmillAdvanced() {
		this.textureWidth = 256;
		this.textureHeight = 256;
		this.sail7 = new ModelRenderer(this, 0, 108);
		this.sail7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.sail7.addBox(29.0F, -0.5F, -5.0F, 67, 20, 2, 0.0F);
		this.setRotateAngle(sail7, 0.2617993877991494F, -0.0F, 0.7853981633974483F);
		this.sail1 = new ModelRenderer(this, 0, 130);
		this.sail1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.sail1.addBox(-19.5F, 29.0F, -5.0F, 20, 67, 2, 0.0F);
		this.setRotateAngle(sail1, 0.0F, 0.2617993877991494F, 0.0F);
		this.rod8 = new ModelRenderer(this, 0, 8);
		this.rod8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.rod8.addBox(-1.5F, 2.0F, -5.5F, 3, 96, 3, 0.0F);
		this.setRotateAngle(rod8, 0.0F, -0.0F, 0.7853981633974483F);
		this.sail4 = new ModelRenderer(this, 0, 130);
		this.sail4.mirror = true;
		this.sail4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.sail4.addBox(-0.5F, -96.0F, -5.0F, 20, 67, 2, 0.0F);
		this.setRotateAngle(sail4, 0.0F, -0.2617993877991494F, 0.7853981633974483F);
		this.sail3 = new ModelRenderer(this, 0, 108);
		this.sail3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.sail3.addBox(-96.0F, -19.5F, -5.0F, 67, 20, 2, 0.0F);
		this.setRotateAngle(sail3, -0.2617993877991494F, -0.0F, 0.7853981633974483F);
		this.rod1 = new ModelRenderer(this, 0, 8);
		this.rod1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.rod1.addBox(-1.5F, -98.0F, -5.5F, 3, 96, 3, 0.0F);
		this.rod4 = new ModelRenderer(this, 0, 0);
		this.rod4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.rod4.addBox(-98.0F, -1.5F, -5.5F, 96, 3, 3, 0.0F);
		this.axel = new ModelRenderer(this, 12, 6);
		this.axel.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.axel.addBox(-2.0F, -2.0F, -8.0F, 4, 4, 16, 0.0F);
		this.rod7 = new ModelRenderer(this, 0, 0);
		this.rod7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.rod7.addBox(2.0F, -1.5F, -5.5F, 96, 3, 3, 0.0F);
		this.setRotateAngle(rod7, 0.0F, -0.0F, 0.7853981633974483F);
		this.rod5 = new ModelRenderer(this, 0, 0);
		this.rod5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.rod5.addBox(-98.0F, -1.5F, -5.5F, 96, 3, 3, 0.0F);
		this.setRotateAngle(rod5, 0.0F, 0.0F, 0.7853981633974483F);
		this.sail6 = new ModelRenderer(this, 0, 108);
		this.sail6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.sail6.addBox(29.0F, -0.5F, -5.0F, 67, 20, 2, 0.0F);
		this.setRotateAngle(sail6, 0.2617993877991494F, -0.0F, 0.0F);
		this.rod2 = new ModelRenderer(this, 0, 0);
		this.rod2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.rod2.addBox(2.0F, -1.5F, -5.5F, 96, 3, 3, 0.0F);
		this.rod3 = new ModelRenderer(this, 0, 8);
		this.rod3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.rod3.addBox(-1.5F, 2.0F, -5.5F, 3, 96, 3, 0.0F);
		this.sail8 = new ModelRenderer(this, 0, 130);
		this.sail8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.sail8.addBox(-19.5F, 29.0F, -5.0F, 20, 67, 2, 0.0F);
		this.setRotateAngle(sail8, 0.0F, 0.2617993877991494F, 0.7853981633974483F);
		this.rod6 = new ModelRenderer(this, 0, 8);
		this.rod6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.rod6.addBox(-1.5F, -98.0F, -5.5F, 3, 96, 3, 0.0F);
		this.setRotateAngle(rod6, 0.0F, -0.0F, 0.7853981633974483F);
		this.sail2 = new ModelRenderer(this, 0, 108);
		this.sail2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.sail2.addBox(-96.0F, -19.5F, -5.0F, 67, 20, 2, 0.0F);
		this.setRotateAngle(sail2, -0.2617993877991494F, -0.0F, 0.0F);
		this.sail5 = new ModelRenderer(this, 0, 130);
		this.sail5.mirror = true;
		this.sail5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.sail5.addBox(-0.5F, -96.0F, -5.0F, 20, 67, 2, 0.0F);
		this.setRotateAngle(sail5, 0.0F, -0.2617993877991494F, 0.0F);

		this.axel.addChild(this.rod8);
		this.axel.addChild(this.rod1);
		this.axel.addChild(this.rod4);
		this.axel.addChild(this.rod7);
		this.axel.addChild(this.rod5);
		this.axel.addChild(this.rod2);
		this.axel.addChild(this.rod3);
		this.axel.addChild(this.rod6);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{

	}

	public void render(float[][]rgb, float f5)
	{ 
		this.axel.render(f5);
		GL11.glColor3f(rgb[0][0],rgb[0][1],rgb[0][2]);
		this.sail1.render(f5);
		GL11.glColor3f(rgb[1][0],rgb[1][1],rgb[1][2]);
		this.sail2.render(f5);
		GL11.glColor3f(rgb[2][0],rgb[2][1],rgb[2][2]);
		this.sail3.render(f5);
		GL11.glColor3f(rgb[3][0],rgb[3][1],rgb[3][2]);
		this.sail4.render(f5);
		GL11.glColor3f(rgb[4][0],rgb[4][1],rgb[4][2]);
		this.sail5.render(f5);
		GL11.glColor3f(rgb[5][0],rgb[5][1],rgb[5][2]);
		this.sail6.render(f5);
		GL11.glColor3f(rgb[6][0],rgb[6][1],rgb[6][2]);
		this.sail7.render(f5);
		GL11.glColor3f(rgb[7][0],rgb[7][1],rgb[7][2]);
		this.sail8.render(f5);
	}

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
