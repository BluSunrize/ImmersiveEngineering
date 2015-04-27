package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Post - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelPost extends ModelBase
{
	public ModelRenderer Base;
	public ModelRenderer Pole;
	public ModelRenderer Top_mid;
	public ModelRenderer Arm_right;
	public ModelRenderer Arm_left;
	public ModelRenderer Arm_right2;
	public ModelRenderer Top_right;
	public ModelRenderer Arm_left2;
	public ModelRenderer Top_left;

	public ModelPost() {
		this.textureWidth = 128;
		this.textureHeight = 64;

		this.Base = new ModelRenderer(this, 16, 40);
		this.Base.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Base.addBox(-4.0F, -8.0F, -4.0F, 8, 16, 8, 0.0F);
		this.Base.rotateAngleZ=3.14159f;
		this.Pole = new ModelRenderer(this, 0, 14);
		this.Pole.setRotationPoint(0.0F, -16.0F, 0.0F);
		this.Pole.addBox(-2.0F, -38.0F, -2.0F, 4, 46, 4, 0.0F);
		this.Top_mid = new ModelRenderer(this, 40, 32);
		this.Top_mid.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Top_mid.addBox(-3.0F, -40.0F, -3.0F, 6, 2, 6, 0.0F);

		this.Arm_right = new ModelRenderer(this, 0, 6);
		this.Arm_right.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Arm_right.addBox(-18.0F, -38.0F, -2.0F, 16, 2, 4, 0.0F);
		this.Arm_right2 = new ModelRenderer(this, 0, 1);
		this.Arm_right2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Arm_right2.addBox(-31.5F, -27.8F, -1.0F, 18, 3, 2, 0.0F);
		this.setRotateAngle(Arm_right2, 0.0F, 0.0F, 0.4553564018453205F);
		this.Top_right = new ModelRenderer(this, 40, 32);
		this.Top_right.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Top_right.addBox(-19.0F, -40.0F, -3.0F, 6, 2, 6, 0.0F);
		this.Arm_right.addChild(this.Arm_right2);
		this.Arm_right.addChild(this.Top_right);
		
		this.Arm_left = new ModelRenderer(this, 0, 6);
		this.Arm_left.mirror = true;
		this.Arm_left.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Arm_left.addBox(2.0F, -38.0F, -2.0F, 16, 2, 4, 0.0F);
		this.Arm_left2 = new ModelRenderer(this, 0, 1);
		this.Arm_left2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Arm_left2.addBox(13.5F, -27.8F, -1.0F, 18, 3, 2, 0.0F);
		this.setRotateAngle(Arm_left2, 0.0F, 0.0F, -0.4553564018453205F);
		this.Top_left = new ModelRenderer(this, 40, 32);
		this.Top_left.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Top_left.addBox(13.0F, -40.0F, -3.0F, 6, 2, 6, 0.0F);
		this.Arm_left.addChild(this.Arm_left2);
		this.Arm_left.addChild(this.Top_left);
		
		this.Pole.addChild(this.Top_mid);
		this.Pole.addChild(this.Arm_right);
		this.Pole.addChild(this.Arm_left);
		
		this.Base.addChild(this.Pole);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{ 
		this.Base.render(f5);
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
