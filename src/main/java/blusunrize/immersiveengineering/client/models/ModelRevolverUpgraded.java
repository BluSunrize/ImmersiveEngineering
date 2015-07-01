package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * eeerrtRevolver - Damien A.W "Hazard"
 * Created using Tabula 5.0.0
 */
public class ModelRevolverUpgraded extends ModelBase
{
	public ModelRenderer trigger;
	public ModelRenderer Grip;
	public ModelRenderer Frontgrip;
	public ModelRenderer scope1;
	public ModelRenderer knife1;
	public ModelRenderer Body1;
	public ModelRenderer Grip2;
	public ModelRenderer Body2;
	public ModelRenderer scope3;
	public ModelRenderer scope4;
	public ModelRenderer scope2;
	public ModelRenderer guard1;
	public ModelRenderer Body3;
	public ModelRenderer guard5;
	public ModelRenderer guard4;
	public ModelRenderer guard3;
	public ModelRenderer guard2;
	public ModelRenderer Barrel;
	public ModelRenderer Frontgrip2;
	public ModelRenderer knife10;
	public ModelRenderer knife2;
	public ModelRenderer knife4;
	public ModelRenderer knife3;
	public ModelRenderer knife5;
	public ModelRenderer knife9;
	public ModelRenderer knife6;
	public ModelRenderer knife7;
	public ModelRenderer knife8;
	public ModelRenderer knife11;
	public ModelRenderer Cylinder;
	public ModelRenderer Cylinder_1;
	public ModelRenderer Cylinder_2;

	public ModelRevolverUpgraded() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.knife8 = new ModelRenderer(this, 84, 48);
		this.knife8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife8.addBox(-1.0F, 0.5F, -48.0F, 2, 4, 2, 0.0F);
		this.knife4 = new ModelRenderer(this, 12, 39);
		this.knife4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife4.addBox(-1.5F, 0.0F, -30.0F, 3, 7, 2, 0.0F);
		this.Cylinder = new ModelRenderer(this, 28, 7);
		this.Cylinder.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Cylinder.addBox(-4.0F, -3.5F, -5.0F, 8, 8, 10, 0.0F);
		this.guard2 = new ModelRenderer(this, 104, 19);
		this.guard2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.guard2.addBox(-1.0F, 10.0F, 4.5F, 2, 1, 1, 0.0F);
		this.knife3 = new ModelRenderer(this, 12, 39);
		this.knife3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife3.addBox(-1.5F, 0.0F, -24.0F, 3, 7, 2, 0.0F);
		this.knife9 = new ModelRenderer(this, 85, 45);
		this.knife9.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife9.addBox(-1.0F, 2.5F, -49.0F, 2, 1, 1, 0.0F);
		this.scope1 = new ModelRenderer(this, 64, 0);
		this.scope1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.scope1.addBox(-2.5F, -9.5F, 8.0F, 5, 5, 9, 0.0F);
		this.knife6 = new ModelRenderer(this, 101, 44);
		this.knife6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife6.addBox(-1.0F, 0.5F, -46.0F, 2, 6, 10, 0.0F);
		this.scope2 = new ModelRenderer(this, 70, 37);
		this.scope2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.scope2.addBox(-2.0F, -9.0F, -15.0F, 4, 4, 23, 0.0F);
		this.scope4 = new ModelRenderer(this, 0, 34);
		this.scope4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.scope4.addBox(-1.0F, -5.0F, -8.0F, 2, 1, 3, 0.0F);
		this.trigger = new ModelRenderer(this, 105, 0);
		this.trigger.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.trigger.addBox(-1.0F, 4.3F, 3.6F, 2, 4, 1, 0.0F);
		this.setRotateAngle(trigger, -0.31869712141416456F, 0.0F, 0.0F);
		this.knife5 = new ModelRenderer(this, 61, 41);
		this.knife5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife5.addBox(-1.0F, 0.5F, -36.0F, 2, 6, 13, 0.0F);
		this.guard1 = new ModelRenderer(this, 104, 19);
		this.guard1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.guard1.addBox(-1.0F, 7.0F, 5.5F, 2, 3, 1, 0.0F);
		this.knife1 = new ModelRenderer(this, 0, 38);
		this.knife1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife1.addBox(-2.0F, -4.0F, -24.0F, 4, 4, 2, 0.0F);
		this.Body3 = new ModelRenderer(this, 88, 0);
		this.Body3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Body3.addBox(-2.0F, 3.0F, -8.0F, 4, 3, 16, 0.0F);
		this.guard3 = new ModelRenderer(this, 104, 19);
		this.guard3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.guard3.addBox(-1.0F, 11.0F, -1.5F, 2, 1, 6, 0.0F);
		this.Cylinder_1 = new ModelRenderer(this, 64, 19);
		this.Cylinder_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Cylinder_1.addBox(-13.1F, -6.0F, -5.0F, 8, 8, 10, 0.0F);
		this.Cylinder_2 = new ModelRenderer(this, 0, 3);
		this.Cylinder_2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Cylinder_2.addBox(-12.0F, 1.5F, -4.0F, 9, 3, 8, 0.0F);
		this.setRotateAngle(Cylinder_2, 0.0F, 0.0F, 0.19198621771937624F);
		this.guard4 = new ModelRenderer(this, 104, 19);
		this.guard4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.guard4.addBox(-1.0F, 10.0F, -2.5F, 2, 1, 1, 0.0F);
		this.Body2 = new ModelRenderer(this, 14, 14);
		this.Body2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Body2.addBox(-2.0F, -4.0F, -8.0F, 4, 7, 3, 0.0F);
		this.Body1 = new ModelRenderer(this, 14, 14);
		this.Body1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Body1.addBox(-2.0F, -4.0F, 5.0F, 4, 7, 3, 0.0F);
		this.Barrel = new ModelRenderer(this, 0, 28);
		this.Barrel.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Barrel.addBox(-1.5F, -3.5F, -40.0F, 3, 3, 32, 0.0F);
		this.Grip2 = new ModelRenderer(this, 38, 46);
		this.Grip2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Grip2.addBox(-1.5F, 6.4F, -3.49F, 3, 6, 8, 0.0F);
		this.setRotateAngle(Grip2, 1.0016444577195458F, 0.0F, 0.0F);
		this.Frontgrip = new ModelRenderer(this, 0, 44);
		this.Frontgrip.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Frontgrip.addBox(-2.0F, 0.0F, -16.0F, 4, 12, 4, 0.0F);
		this.Frontgrip2 = new ModelRenderer(this, 0, 38);
		this.Frontgrip2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Frontgrip2.addBox(-2.0F, -4.0F, -15.0F, 4, 4, 2, 0.0F);
		this.knife7 = new ModelRenderer(this, 85, 45);
		this.knife7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife7.addBox(-1.0F, 4.5F, -47.0F, 2, 1, 1, 0.0F);
		this.knife11 = new ModelRenderer(this, 85, 45);
		this.knife11.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife11.addBox(-1.0F, 0.5F, -51.0F, 2, 1, 1, 0.0F);
		this.Grip = new ModelRenderer(this, 38, 25);
		this.Grip.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Grip.addBox(-2.5F, 8.0F, 2.5F, 5, 13, 8, 0.0F);
		this.setRotateAngle(Grip, 0.4363323129985824F, 0.0F, 0.0F);
		this.guard5 = new ModelRenderer(this, 104, 19);
		this.guard5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.guard5.addBox(-1.0F, 6.0F, -3.5F, 2, 4, 1, 0.0F);
		this.knife10 = new ModelRenderer(this, 84, 44);
		this.knife10.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife10.addBox(-1.0F, 0.5F, -50.0F, 2, 2, 2, 0.0F);
		this.scope3 = new ModelRenderer(this, 0, 34);
		this.scope3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.scope3.addBox(-1.0F, -5.0F, 5.0F, 2, 1, 3, 0.0F);
		this.knife2 = new ModelRenderer(this, 0, 38);
		this.knife2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.knife2.addBox(-2.0F, -4.0F, -30.0F, 4, 4, 2, 0.0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
		this.scope1.render(f5);
		this.scope2.render(f5);
		this.scope3.render(f5);
		this.scope4.render(f5);
		
		this.Cylinder.render(f5);
		this.guard2.render(f5);
		this.trigger.render(f5);
		this.guard1.render(f5);
		this.Body3.render(f5);
		this.guard3.render(f5);
		this.guard4.render(f5);
		this.Body2.render(f5);
		this.Body1.render(f5);
		this.Barrel.render(f5);
		this.Grip2.render(f5);
		this.Frontgrip.render(f5);
		this.Frontgrip2.render(f5);
		this.Grip.render(f5);
		this.guard5.render(f5);

		this.knife1.render(f5);
		this.knife2.render(f5);
		this.knife3.render(f5);
		this.knife4.render(f5);
		this.knife5.render(f5);
		this.knife6.render(f5);
		this.knife7.render(f5);
		this.knife8.render(f5);
		this.knife9.render(f5);
		this.knife10.render(f5);
		this.knife11.render(f5);

		this.Cylinder_1.render(f5);
		this.Cylinder_2.render(f5);
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
