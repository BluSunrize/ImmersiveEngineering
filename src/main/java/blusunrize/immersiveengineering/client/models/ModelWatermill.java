package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * watermill - Damien A.W "hazard"
 * Created using Tabula 4.1.1
 */
public class ModelWatermill extends ModelBase
{
	public ModelRenderer Axle;
	public ModelRenderer blade1;
	public ModelRenderer blade2;
	public ModelRenderer blade3;
	public ModelRenderer blade4;
	public ModelRenderer blade5;
	public ModelRenderer blade6;
	public ModelRenderer blade7;
	public ModelRenderer blade8;
	public ModelRenderer blade9;
	public ModelRenderer blade10;
	public ModelRenderer blade11;
	public ModelRenderer blade12;
	public ModelRenderer blade13;
	public ModelRenderer blade14;
	public ModelRenderer blade15;
	public ModelRenderer blade16;
	public ModelRenderer wheel1;
	public ModelRenderer wheel2;
	public ModelRenderer wheel4;
	public ModelRenderer wheel5;
	public ModelRenderer wheel6;
	public ModelRenderer wheel15;
	public ModelRenderer wheel3;
	public ModelRenderer wheel10;
	public ModelRenderer wheel13;
	public ModelRenderer wheel8;
	public ModelRenderer wheel7;
	public ModelRenderer wheel16;
	public ModelRenderer wheel12;
	public ModelRenderer wheel11;
	public ModelRenderer wheel9;
	public ModelRenderer wheel14;

	public ModelWatermill() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.wheel5 = new ModelRenderer(this, 41, 0);
		this.wheel5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel5.addBox(-14.0F, 34.0F, -7.0F, 28, 2, 14, 0.0F);
		this.wheel2 = new ModelRenderer(this, 41, 0);
		this.wheel2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel2.addBox(-14.0F, -36.0F, -7.0F, 28, 2, 14, 0.0F);
		this.setRotateAngle(wheel2, 0.0F, -0.0F, -0.7853981633974483F);
		this.wheel3 = new ModelRenderer(this, 0, 0);
		this.wheel3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel3.addBox(-36.0F, -14.0F, -7.0F, 2, 28, 14, 0.0F);
		this.blade7 = new ModelRenderer(this, 32, 20);
		this.blade7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade7.addBox(-1.0F, 36.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade7, 0.0F, -0.0F, -0.7853981633974483F);
		this.blade15 = new ModelRenderer(this, 32, 20);
		this.blade15.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade15.addBox(-1.0F, -45.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade15, 0.0F, -0.0F, -0.7853981633974483F);
		this.wheel15 = new ModelRenderer(this, 34, 0);
		this.wheel15.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel15.addBox(-34.0F, -1.0F, -7.0F, 32, 2, 14, 0.0F);
		this.wheel14 = new ModelRenderer(this, 0, 0);
		this.wheel14.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel14.addBox(-1.0F, -34.0F, -7.0F, 2, 32, 14, 0.0F);
		this.setRotateAngle(wheel14, 0.0F, -0.0F, -2.356194490192345F);
		this.blade16 = new ModelRenderer(this, 32, 20);
		this.blade16.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade16.addBox(-1.0F, -46.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade16, 0.0F, -0.0F, -0.40142572795869574F);
		this.blade2 = new ModelRenderer(this, 32, 20);
		this.blade2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade2.addBox(-1.0F, 36.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade2, 0.0F, -0.0F, -2.7576202181510405F);
		this.blade13 = new ModelRenderer(this, 31, 46);
		this.blade13.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade13.addBox(-46.0F, -1.0F, -8.0F, 10, 2, 16, 0.0F);
		this.blade9 = new ModelRenderer(this, 32, 20);
		this.blade9.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade9.addBox(-1.0F, 36.0F, -8.0F, 2, 10, 16, 0.0F);
		this.wheel8 = new ModelRenderer(this, 41, 0);
		this.wheel8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel8.addBox(-14.0F, 34.0F, -7.0F, 28, 2, 14, 0.0F);
		this.setRotateAngle(wheel8, 0.0F, -0.0F, -2.356194490192345F);
		this.wheel6 = new ModelRenderer(this, 41, 0);
		this.wheel6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel6.addBox(-14.0F, 34.0F, -7.0F, 28, 2, 14, 0.0F);
		this.setRotateAngle(wheel6, 0.0F, -0.0F, -0.7853981633974483F);
		this.blade6 = new ModelRenderer(this, 32, 20);
		this.blade6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade6.addBox(-1.0F, 36.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade6, 0.0F, -0.0F, -1.186823891356144F);
		this.blade5 = new ModelRenderer(this, 31, 46);
		this.blade5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade5.addBox(36.0F, -1.0F, -8.0F, 10, 2, 16, 0.0F);
		this.Axle = new ModelRenderer(this, 88, 44);
		this.Axle.setRotationPoint(0.0F, -0.0F, 0.0F);
		this.Axle.addBox(-2.0F, -2.0F, -8.0F, 4, 4, 16, 0.0F);
		this.blade4 = new ModelRenderer(this, 32, 20);
		this.blade4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade4.addBox(-1.0F, 36.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade4, 0.0F, -0.0F, -1.9722220547535922F);
		this.wheel7 = new ModelRenderer(this, 0, 0);
		this.wheel7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel7.addBox(34.0F, -14.0F, -7.0F, 2, 28, 14, 0.0F);
		this.blade11 = new ModelRenderer(this, 32, 20);
		this.blade11.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade11.addBox(-1.0F, -46.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade11, 0.0F, 0.0F, -2.356194490192345F);
		this.wheel4 = new ModelRenderer(this, 41, 0);
		this.wheel4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel4.addBox(-14.0F, -36.0F, -7.0F, 28, 2, 14, 0.0F);
		this.setRotateAngle(wheel4, 0.0F, -0.0F, -2.356194490192345F);
		this.blade3 = new ModelRenderer(this, 32, 20);
		this.blade3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade3.addBox(-1.0F, 36.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade3, 0.0F, -0.0F, -2.356194490192345F);
		this.wheel12 = new ModelRenderer(this, 0, 0);
		this.wheel12.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel12.addBox(-1.0F, 2.0F, -7.0F, 2, 32, 14, 0.0F);
		this.setRotateAngle(wheel12, 0.0F, -0.0F, -0.7853981633974483F);
		this.wheel10 = new ModelRenderer(this, 0, 0);
		this.wheel10.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel10.addBox(-1.0F, 2.0F, -7.0F, 2, 32, 14, 0.0F);
		this.setRotateAngle(wheel10, 0.0F, -0.0F, -2.356194490192345F);
		this.wheel11 = new ModelRenderer(this, 34, 0);
		this.wheel11.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel11.addBox(2.0F, -1.0F, -7.0F, 32, 2, 14, 0.0F);
		this.blade14 = new ModelRenderer(this, 32, 20);
		this.blade14.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade14.addBox(-1.0F, -46.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade14, 0.0F, -0.0F, -1.186823891356144F);
		this.blade10 = new ModelRenderer(this, 32, 20);
		this.blade10.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade10.addBox(-1.0F, -46.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade10, 0.0F, -0.0F, -2.7576202181510405F);
		this.wheel1 = new ModelRenderer(this, 41, 0);
		this.wheel1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel1.addBox(-14.0F, -36.0F, -7.0F, 28, 2, 14, 0.0F);
		this.wheel9 = new ModelRenderer(this, 0, 0);
		this.wheel9.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel9.addBox(-1.0F, -34.0F, -7.0F, 2, 32, 14, 0.0F);
		this.blade12 = new ModelRenderer(this, 32, 20);
		this.blade12.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade12.addBox(-1.0F, -46.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade12, 0.0F, -0.0F, -1.9722220547535922F);
		this.wheel13 = new ModelRenderer(this, 0, 0);
		this.wheel13.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel13.addBox(-1.0F, 2.0F, -7.0F, 2, 32, 14, 0.0F);
		this.wheel16 = new ModelRenderer(this, 0, 0);
		this.wheel16.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wheel16.addBox(-1.0F, -34.0F, -7.0F, 2, 32, 14, 0.0F);
		this.setRotateAngle(wheel16, 0.0F, -0.0F, -0.7853981633974483F);
		this.blade8 = new ModelRenderer(this, 32, 20);
		this.blade8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade8.addBox(-1.0F, 36.0F, -8.0F, 2, 10, 16, 0.0F);
		this.setRotateAngle(blade8, 0.0F, -0.0F, -0.40142572795869574F);
		this.blade1 = new ModelRenderer(this, 32, 20);
		this.blade1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.blade1.addBox(-1.0F, -45.0F, -8.0F, 2, 10, 16, 0.0F);
		this.Axle.addChild(this.wheel5);
		this.Axle.addChild(this.wheel2);
		this.Axle.addChild(this.wheel3);
		this.Axle.addChild(this.blade7);
		this.Axle.addChild(this.blade15);
		this.Axle.addChild(this.wheel15);
		this.Axle.addChild(this.wheel14);
		this.Axle.addChild(this.blade16);
		this.Axle.addChild(this.blade2);
		this.Axle.addChild(this.blade13);
		this.Axle.addChild(this.blade9);
		this.Axle.addChild(this.wheel8);
		this.Axle.addChild(this.wheel6);
		this.Axle.addChild(this.blade6);
		this.Axle.addChild(this.blade5);
		this.Axle.addChild(this.blade4);
		this.Axle.addChild(this.wheel7);
		this.Axle.addChild(this.blade11);
		this.Axle.addChild(this.wheel4);
		this.Axle.addChild(this.blade3);
		this.Axle.addChild(this.wheel12);
		this.Axle.addChild(this.wheel10);
		this.Axle.addChild(this.wheel11);
		this.Axle.addChild(this.blade14);
		this.Axle.addChild(this.blade10);
		this.Axle.addChild(this.wheel1);
		this.Axle.addChild(this.wheel9);
		this.Axle.addChild(this.blade12);
		this.Axle.addChild(this.wheel13);
		this.Axle.addChild(this.wheel16);
		this.Axle.addChild(this.blade8);
		this.Axle.addChild(this.blade1);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.Axle.render(f5);
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
