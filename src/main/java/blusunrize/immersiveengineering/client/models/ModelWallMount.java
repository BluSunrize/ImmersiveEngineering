package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * arm - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelWallMount extends ModelBase
{
	public ModelRenderer connection;
	public ModelRenderer wallmount;
	public ModelRenderer arm1;
	public ModelRenderer arm2;

	public ModelWallMount() {
		this.textureWidth = 24;
		this.textureHeight = 32;
		//        this.connection = new ModelRenderer(this, 0, 18);
		//        this.connection.setRotationPoint(8.0F, 8.0F, 8.0F);
		//        this.connection.addBox(-11.0F, -16.0F, -11.0F, 6, 2, 6, 0.0F);
		//        this.arm1 = new ModelRenderer(this, 0, 14);
		//        this.arm1.setRotationPoint(0.0F, 0.0F, 0.0F);
		//        this.arm1.addBox(-15.4F, -5.2F, -9.0F, 9, 2, 2, 0.0F);
		//        this.setRotateAngle(arm1, 0.0F, 0.0F, 0.6829473363053812F);
		//        this.wallmount = new ModelRenderer(this, 0, 0);
		//        this.wallmount.setRotationPoint(0.0F, 0.0F, 0.0F);
		//        this.wallmount.addBox(-3.7F, -14.0F, -11.0F, 2, 8, 6, 0.0F);
		//        this.arm2 = new ModelRenderer(this, 0, 26);
		//        this.arm2.setRotationPoint(0.0F, 0.0F, 0.0F);
		//        this.arm2.addBox(-10.0F, -14.0F, -10.0F, 8, 2, 4, 0.0F);
		//        this.connection.addChild(arm1);
		//        this.connection.addChild(arm2);
		//        this.connection.addChild(wallmount);
		this.connection = new ModelRenderer(this, 0, 18);
		this.connection.setRotationPoint(8.0F, 8.0F, 8.0F);
		this.connection.addBox(-3.0F, 6.0F, -3.0F, 6, 2, 6, 0.0F);
		this.arm1 = new ModelRenderer(this, 0, 14);
		this.arm1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.arm1.addBox(-4.3F, 2.7F, -1.0F, 10, 2, 2, 0.0F);
		this.setRotateAngle(arm1, 0.0F, 0.0F, -0.6829473363053812F);
		this.wallmount = new ModelRenderer(this, 0, 0);
		this.wallmount.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.wallmount.addBox(5.7F, -2.0F, -3.0F, 2, 8, 6, 0.0F);
		this.arm2 = new ModelRenderer(this, 0, 26);
		this.arm2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.arm2.addBox(-2.0F, 4.0F, -2.0F, 8, 2, 4, 0.0F);
		this.connection.addChild(arm1);
		this.connection.addChild(arm2);
		this.connection.addChild(wallmount);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
		this.connection.render(f5);
		//        this.arm1.render(f5);
		//        this.wallmount.render(f5);
		//        this.arm2.render(f5);
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
