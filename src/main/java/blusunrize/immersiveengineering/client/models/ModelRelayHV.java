package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Connector_HV - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelRelayHV extends ModelBase
{
	public ModelRenderer Top;
	public ModelRenderer Insulator1;
	public ModelRenderer Insulator2;
	public ModelRenderer Insulator3;
	public ModelRenderer Insulator4;
	public ModelRenderer Insulator5;
	public ModelRenderer Insulator6;
	public ModelRenderer mid1;
	public ModelRenderer mid2;
	public ModelRenderer mid3;
	public ModelRenderer Bottom;

	public ModelRelayHV() {
		this.textureWidth = 64;
		this.textureHeight = 32;
		this.Top = new ModelRenderer(this, 16, 18);
		this.Top.setRotationPoint(8.0F, 8.0F, 8.0F);
		this.Top.addBox(-1.5F, -8.0F, -1.5F, 3, 1, 3, 0.0F);
		this.Top.rotateAngleZ=3.14159f;
		this.Insulator1 = new ModelRenderer(this, 32, 24);
		this.Insulator1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Insulator1.addBox(-3.0F, 1.5F, -3.0F, 6, 2, 6, 0.0F);
		this.Insulator2 = new ModelRenderer(this, 0, 18);
		this.Insulator2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Insulator2.addBox(-2.0F, 0.5F, -2.0F, 4, 1, 4, 0.0F);
		this.mid1 = new ModelRenderer(this, 16, 18);
		this.mid1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.mid1.addBox(-1.5F,-4.5F, -1.5F, 3, 1, 3, 0.0F);
		this.Insulator3 = new ModelRenderer(this, 32, 24);
		this.Insulator3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Insulator3.addBox(-3.0F,-2.5F, -3.0F, 6, 2, 6, 0.0F);
		this.Insulator4 = new ModelRenderer(this, 0, 18);
		this.Insulator4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Insulator4.addBox(-2.0F,-3.5F, -2.0F, 4, 1, 4, 0.0F);
		this.mid2 = new ModelRenderer(this, 16, 18);
		this.mid2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.mid2.addBox(-1.5F,-0.5F, -1.5F, 3, 1, 3, 0.0F);
		this.Insulator5 = new ModelRenderer(this, 32, 24);
		this.Insulator5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Insulator5.addBox(-3.0F,-6.5F, -3.0F, 6, 2, 6, 0.0F);
		this.Insulator6 = new ModelRenderer(this, 0, 18);
		this.Insulator6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Insulator6.addBox(-2.0F,-7.5F, -2.0F, 4, 1, 4, 0.0F);
		this.mid3 = new ModelRenderer(this, 17, 15);
		this.mid3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.mid3.addBox(-1.0F, 3.5F, -1.0F, 2, 1, 2, 0.0F);
		this.Bottom = new ModelRenderer(this, 16, 14);
		this.Bottom.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Bottom.addBox(-1.5F, 4.5F, -1.5F, 3, 1, 3, 0.0F);

		this.Top.addChild(this.Insulator1);
		this.Top.addChild(this.Insulator2);
		this.Top.addChild(this.mid1);
		this.Top.addChild(this.Insulator3);
		this.Top.addChild(this.Insulator4);
		this.Top.addChild(this.mid2);
		this.Top.addChild(this.Insulator5);
		this.Top.addChild(this.Insulator6);
		this.Top.addChild(this.mid3);
		this.Top.addChild(this.Bottom);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
		this.Top.render(f5);
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
