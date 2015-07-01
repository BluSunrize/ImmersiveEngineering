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
	public ModelRenderer ceramicL_HV;
	public ModelRenderer ceramicR;
	public ModelRenderer ceramicR_HV;
	public ModelRenderer ceramicL;
	public ModelRenderer ceramicL_HV0;
	public ModelRenderer ceramicL_HV2;
	public ModelRenderer ConnectorTopL_HV;
	public ModelRenderer ceramicL_HV1;
	public ModelRenderer ceramicR1;
	public ModelRenderer ceramicR2;
	public ModelRenderer ConnectorTopR;
	public ModelRenderer ceramicR_HV0;
	public ModelRenderer ceramicR_HV1;
	public ModelRenderer ceramicR_HV2;
	public ModelRenderer ConnectorTopR_HV;
	public ModelRenderer ceramicL1;
	public ModelRenderer ceramicL2;
	public ModelRenderer ConnectorTopL;

	public ModelTransformerHV() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		
		this.Transformer = new ModelRenderer(this, 64, 0);
		this.Transformer.setRotationPoint(8.0F, 16.0F, 8.0F);
		this.Transformer.addBox(-8.0F, 0.0F, -8.0F, 16, 32, 16, 0.0F);
		this.Transformer.rotateAngleX = 3.14159f;

		//ceramicL
		this.ceramicL = new ModelRenderer(this, 16, 23);
		this.ceramicL.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicL.addBox(-7.0F, -7.0F, -2.0F, 4, 7, 4, 0.0F);
		this.ConnectorTopL = new ModelRenderer(this, 16, 18);
		this.ConnectorTopL.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ConnectorTopL.addBox(-6.5F, -8.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramicL1 = new ModelRenderer(this, 16, 38);
		this.ceramicL1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicL1.addBox(-8.0F, -3.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramicL2 = new ModelRenderer(this, 16, 38);
		this.ceramicL2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicL2.addBox(-8.0F, -6.0F, -3.0F, 6, 2, 6, 0.0F);
		//ceramicLHV
		this.ceramicL_HV = new ModelRenderer(this, 16, 23);
		this.ceramicL_HV.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicL_HV.addBox(-7.0F, -11.0F, -2.0F, 4, 11, 4, 0.0F);
		this.ConnectorTopL_HV = new ModelRenderer(this, 16, 18);
		this.ConnectorTopL_HV.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ConnectorTopL_HV.addBox(-6.5F, -12.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramicL_HV0 = new ModelRenderer(this, 16, 38);
		this.ceramicL_HV0.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicL_HV0.addBox(-8.0F, -4.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramicL_HV1 = new ModelRenderer(this, 16, 38);
		this.ceramicL_HV1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicL_HV1.addBox(-8.0F, -7.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramicL_HV2 = new ModelRenderer(this, 16, 38);
		this.ceramicL_HV2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicL_HV2.addBox(-8.0F, -10.0F, -3.0F, 6, 2, 6, 0.0F);
		
		//ceramicR
		this.ceramicR = new ModelRenderer(this, 16, 23);
		this.ceramicR.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicR.addBox(3.0F, -7.0F, -2.0F, 4, 7, 4, 0.0F);
		this.ConnectorTopR = new ModelRenderer(this, 16, 14);
		this.ConnectorTopR.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ConnectorTopR.addBox(3.5F, -8.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramicR1 = new ModelRenderer(this, 16, 38);
		this.ceramicR1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicR1.addBox(2.0F, -3.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramicR2 = new ModelRenderer(this, 16, 38);
		this.ceramicR2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicR2.addBox(2.0F, -6.0F, -3.0F, 6, 2, 6, 0.0F);
		//ceramicRHV
		this.ceramicR_HV = new ModelRenderer(this, 16, 23);
		this.ceramicR_HV.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicR_HV.addBox(3.0F, -11.0F, -2.0F, 4, 11, 4, 0.0F);
		this.ConnectorTopR_HV = new ModelRenderer(this, 16, 14);
		this.ConnectorTopR_HV.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ConnectorTopR_HV.addBox(3.5F, -12.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramicR_HV0 = new ModelRenderer(this, 16, 38);
		this.ceramicR_HV0.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicR_HV0.addBox(2.0F, -4.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramicR_HV1 = new ModelRenderer(this, 16, 38);
		this.ceramicR_HV1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicR_HV1.addBox(2.0F, -7.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramicR_HV2 = new ModelRenderer(this, 16, 38);
		this.ceramicR_HV2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicR_HV2.addBox(2.0F, -10.0F, -3.0F, 6, 2, 6, 0.0F);

		this.ceramicL.addChild(this.ConnectorTopL);
		this.ceramicL.addChild(this.ceramicL1);
		this.ceramicL.addChild(this.ceramicL2);
		this.ceramicL_HV.addChild(this.ConnectorTopL_HV);
		this.ceramicL_HV.addChild(this.ceramicL_HV0);
		this.ceramicL_HV.addChild(this.ceramicL_HV1);
		this.ceramicL_HV.addChild(this.ceramicL_HV2);

		this.ceramicR.addChild(this.ConnectorTopR);
		this.ceramicR.addChild(this.ceramicR1);
		this.ceramicR.addChild(this.ceramicR2);
		this.ceramicR_HV.addChild(this.ConnectorTopR_HV);
		this.ceramicR_HV.addChild(this.ceramicR_HV0);
		this.ceramicR_HV.addChild(this.ceramicR_HV1);
		this.ceramicR_HV.addChild(this.ceramicR_HV2);

		this.Transformer.addChild(ceramicL);
		this.Transformer.addChild(ceramicL_HV);
		this.Transformer.addChild(ceramicR);
		this.Transformer.addChild(ceramicR_HV);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.Transformer.render(f5);
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
