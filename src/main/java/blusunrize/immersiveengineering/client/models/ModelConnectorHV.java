package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * HVConnection point - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelConnectorHV extends ModelBase
{
	public ModelRenderer HVtop;
	public ModelRenderer ceramicHV1;
	public ModelRenderer ceramicHV2;
	public ModelRenderer ceramicHV3;
	public ModelRenderer ceramicHV;

	public ModelConnectorHV() {
		this.textureWidth = 64;
		this.textureHeight = 32;
		this.HVtop = new ModelRenderer(this, 16, 9);
		this.HVtop.setRotationPoint(8.0F, 8.0F, 8.0F);
		this.HVtop.addBox(-1.5F, -4.0F, -1.5F, 3, 1, 3, 0.0F);
		this.ceramicHV = new ModelRenderer(this, 0, 19);
		this.ceramicHV.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicHV.addBox(-2.0F, -3.0F, -2.0F, 4, 7, 4, 0.0F);
		this.ceramicHV2 = new ModelRenderer(this, 16, 23);
		this.ceramicHV2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicHV2.addBox(-3.0F, 1.0F, -3.0F, 6, 2, 6, 0.0F);
		this.ceramicHV3 = new ModelRenderer(this, 16, 13);
		this.ceramicHV3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicHV3.addBox(-3.0F, 4.0F, -3.0F, 6, 4, 6, 0.0F);
		this.ceramicHV1 = new ModelRenderer(this, 16, 23);
		this.ceramicHV1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.ceramicHV1.addBox(-3.0F, -2.0F, -3.0F, 6, 2, 6, 0.0F);
		
		this.HVtop.addChild(this.ceramicHV);
		this.HVtop.addChild(this.ceramicHV2);
		this.HVtop.addChild(this.ceramicHV3);
		this.HVtop.addChild(this.ceramicHV1);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{ 
		this.HVtop.render(f5);
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
