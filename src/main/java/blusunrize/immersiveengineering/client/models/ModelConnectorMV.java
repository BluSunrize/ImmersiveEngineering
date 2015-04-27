package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * MVConnection point - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelConnectorMV extends ModelBase
{
    public ModelRenderer MVtop;
    public ModelRenderer ceramicMV2;
    public ModelRenderer ceramicMV3;
    public ModelRenderer ceramicMV;

    public ModelConnectorMV() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.MVtop = new ModelRenderer(this, 16, 9);
        this.MVtop.setRotationPoint(8.0F, 8.0F, 8.0F);
        this.MVtop.addBox(-1.5F, -1.0F, -1.5F, 3, 1, 3, 0.0F);
        this.ceramicMV2 = new ModelRenderer(this, 16, 23);
        this.ceramicMV2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.ceramicMV2.addBox(-3.0F, 1.0F, -3.0F, 6, 2, 6, 0.0F);
        this.ceramicMV = new ModelRenderer(this, 0, 19);
        this.ceramicMV.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.ceramicMV.addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4, 0.0F);
        this.ceramicMV3 = new ModelRenderer(this, 16, 13);
        this.ceramicMV3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.ceramicMV3.addBox(-3.0F, 4.0F, -3.0F, 6, 4, 6, 0.0F);
        this.MVtop.addChild(this.ceramicMV);
        this.MVtop.addChild(this.ceramicMV2);
        this.MVtop.addChild(this.ceramicMV3);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
        this.MVtop.render(f5);
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
