package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Transformer - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelTransformer extends ModelBase
{
	public ModelRenderer Transformer;
	public ModelRenderer ceramic;
	public ModelRenderer ceramic_1;
	public ModelRenderer ceramic1;
	public ModelRenderer ceramic2;
	public ModelRenderer Connectortop;
	public ModelRenderer ceramic1_1;
	public ModelRenderer ceramic2_1;
	public ModelRenderer Connectortop_1;

	public ModelTransformer() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.Connectortop = new ModelRenderer(this, 16, 18);
		this.Connectortop.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Connectortop.addBox(-6.5F, -16.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramic2 = new ModelRenderer(this, 16, 34);
		this.ceramic2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic2.addBox(-8.0F, -14.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic_1 = new ModelRenderer(this, 16, 23);
		this.ceramic_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic_1.addBox(3.0F, -15.0F, -2.0F, 4, 7, 4, 0.0F);
		this.ceramic1 = new ModelRenderer(this, 16, 34);
		this.ceramic1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic1.addBox(-8.0F, -11.0F, -3.0F, 6, 2, 6, 0.0F);
		this.Transformer = new ModelRenderer(this, 64, 0);
		this.Transformer.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Transformer.addBox(-8.0F, -8.0F, -8.0F, 16, 32, 16, 0.0F);
		this.ceramic1_1 = new ModelRenderer(this, 16, 34);
		this.ceramic1_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic1_1.addBox(2.0F, -11.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic = new ModelRenderer(this, 16, 23);
		this.ceramic.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic.addBox(-7.0F, -15.0F, -2.0F, 4, 7, 4, 0.0F);
		this.Connectortop_1 = new ModelRenderer(this, 16, 14);
		this.Connectortop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Connectortop_1.addBox(3.5F, -16.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramic2_1 = new ModelRenderer(this, 16, 34);
		this.ceramic2_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic2_1.addBox(2.0F, -14.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic.addChild(this.Connectortop);
		this.ceramic.addChild(this.ceramic2);
		this.ceramic.addChild(this.ceramic1);
		this.ceramic_1.addChild(this.ceramic1_1);
		this.ceramic_1.addChild(this.Connectortop_1);
		this.ceramic_1.addChild(this.ceramic2_1);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
		this.ceramic_1.render(f5);
		this.Transformer.render(f5);
		this.ceramic.render(f5);
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
