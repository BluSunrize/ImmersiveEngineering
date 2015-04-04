package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Connector - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelConnectorLV extends ModelBase
{
    public ModelRenderer Bottom;
    public ModelRenderer Top;
    public ModelRenderer Insulator2;
    public ModelRenderer Insulator1;
    public ModelRenderer Insulator;

    public ModelConnectorLV()
    {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.Insulator2 = new ModelRenderer(this, 32, 24);
        this.Insulator2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Insulator2.addBox(-3.0F, -5.0F, -3.0F, 6, 2, 6, 0.0F);
        this.Insulator1 = new ModelRenderer(this, 16, 23);
        this.Insulator1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Insulator1.addBox(-2.0F, -6.0F, -2.0F, 4, 5, 4, 0.0F);
        this.Insulator = new ModelRenderer(this, 32, 24);
        this.Insulator.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Insulator.addBox(-3.0F, -2.0F, -3.0F, 6, 2, 6, 0.0F);
        this.Bottom = new ModelRenderer(this, 16, 14);
        this.Bottom.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Bottom.addBox(-1.5F, 0.0F, -1.5F, 3, 1, 3, 0.0F);
        this.Top = new ModelRenderer(this, 16, 18);
        this.Top.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Top.addBox(-1.5F, -7.0F, -1.5F, 3, 1, 3, 0.0F);
        this.Bottom.addChild(this.Insulator2);
        this.Bottom.addChild(this.Insulator1);
        this.Bottom.addChild(this.Insulator);
        this.Bottom.addChild(this.Top);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
        this.Bottom.render(f5);
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
