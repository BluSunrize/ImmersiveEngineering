package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * TransformerHV - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelTransformerHV extends ModelBase
{
	public ModelRenderer Transformer;
	public ModelRenderer ceramicHV;
	public ModelRenderer ceramic;
	public ModelRenderer ceramicHV_1;
	public ModelRenderer ceramic_1;
	public ModelRenderer ceramic0;
	public ModelRenderer ceramic2;
	public ModelRenderer Connectortop;
	public ModelRenderer ceramic1;
	public ModelRenderer ceramic1_1;
	public ModelRenderer ceramic2_1;
	public ModelRenderer Connectortop_1;
	public ModelRenderer ceramic0_1;
	public ModelRenderer ceramic1_2;
	public ModelRenderer ceramic2_2;
	public ModelRenderer Connectortop_2;
	public ModelRenderer ceramic1_3;
	public ModelRenderer ceramic2_3;
	public ModelRenderer Connectortop_3;

	public ModelTransformerHV() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		this.ceramic1_1 = new ModelRenderer(this, 16, 38);
		this.ceramic1_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic1_1.addBox(2.0F, -11.0F, -3.0F, 6, 2, 6, 0.0F);
		this.Connectortop = new ModelRenderer(this, 16, 18);
		this.Connectortop.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Connectortop.addBox(-6.5F, -20.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramic_1 = new ModelRenderer(this, 16, 23);
		this.ceramic_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic_1.addBox(-7.0F, -15.0F, -2.0F, 4, 7, 4, 0.0F);
		this.ceramic1_3 = new ModelRenderer(this, 16, 38);
		this.ceramic1_3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic1_3.addBox(-8.0F, -11.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic2 = new ModelRenderer(this, 16, 38);
		this.ceramic2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic2.addBox(-8.0F, -18.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic2_1 = new ModelRenderer(this, 16, 38);
		this.ceramic2_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic2_1.addBox(2.0F, -14.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic = new ModelRenderer(this, 16, 23);
		this.ceramic.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic.addBox(3.0F, -15.0F, -2.0F, 4, 7, 4, 0.0F);
		this.ceramic2_2 = new ModelRenderer(this, 16, 38);
		this.ceramic2_2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic2_2.addBox(2.0F, -18.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramicHV = new ModelRenderer(this, 16, 23);
		this.ceramicHV.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicHV.addBox(-7.0F, -19.0F, -2.0F, 4, 11, 4, 0.0F);
		this.ceramic0 = new ModelRenderer(this, 16, 38);
		this.ceramic0.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic0.addBox(-8.0F, -12.0F, -3.0F, 6, 2, 6, 0.0F);
		this.Connectortop_1 = new ModelRenderer(this, 16, 14);
		this.Connectortop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Connectortop_1.addBox(3.5F, -16.0F, -1.5F, 3, 1, 3, 0.0F);
		this.Connectortop_2 = new ModelRenderer(this, 16, 14);
		this.Connectortop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Connectortop_2.addBox(3.5F, -20.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramicHV_1 = new ModelRenderer(this, 16, 23);
		this.ceramicHV_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicHV_1.addBox(3.0F, -19.0F, -2.0F, 4, 11, 4, 0.0F);
		this.ceramic0_1 = new ModelRenderer(this, 16, 38);
		this.ceramic0_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic0_1.addBox(2.0F, -12.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic2_3 = new ModelRenderer(this, 16, 38);
		this.ceramic2_3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic2_3.addBox(-8.0F, -14.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic1_2 = new ModelRenderer(this, 16, 38);
		this.ceramic1_2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic1_2.addBox(2.0F, -15.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramic1 = new ModelRenderer(this, 16, 38);
		this.ceramic1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramic1.addBox(-8.0F, -15.0F, -3.0F, 6, 2, 6, 0.0F);
		this.Transformer = new ModelRenderer(this, 64, 0);
		this.Transformer.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Transformer.addBox(-8.0F, -8.0F, -8.0F, 16, 32, 16, 0.0F);
		this.Connectortop_3 = new ModelRenderer(this, 16, 18);
		this.Connectortop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Connectortop_3.addBox(-6.5F, -16.0F, -1.5F, 3, 1, 3, 0.0F);

		this.ceramic.addChild(this.Connectortop_1);
		this.ceramic.addChild(this.ceramic1_1);
		this.ceramic.addChild(this.ceramic2_1);
		
		this.ceramicHV.addChild(this.Connectortop);
		this.ceramicHV.addChild(this.ceramic2);
		this.ceramicHV.addChild(this.ceramic0);
		this.ceramicHV.addChild(this.ceramic1);

		this.ceramicHV_1.addChild(this.Connectortop_2);
		this.ceramicHV_1.addChild(this.ceramic2_2);
		this.ceramicHV_1.addChild(this.ceramic0_1);
		this.ceramicHV_1.addChild(this.ceramic1_2);

		this.ceramic_1.addChild(this.Connectortop_3);
		this.ceramic_1.addChild(this.ceramic1_3);
		this.ceramic_1.addChild(this.ceramic2_3);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.ceramic_1.render(f5);
		this.ceramic.render(f5);
		this.ceramicHV.render(f5);
		this.ceramicHV_1.render(f5);
		this.Transformer.render(f5);
	}
	public void render(boolean hvLeft, boolean hvRight)
	{
		this.Transformer.render(.0625f);
		if(hvLeft)
			this.ceramicHV.render(.0625f);
		else
			this.ceramic_1.render(.0625f);
		if(hvRight)
			this.ceramicHV_1.render(.0625f);
		else
			this.ceramic.render(.0625f);
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
