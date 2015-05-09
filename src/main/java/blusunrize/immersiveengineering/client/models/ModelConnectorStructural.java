package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Structural Connectionpoint - Damien A.W Hazard
 * Created using Tabula 5.0.0
 */
public class ModelConnectorStructural extends ModelBase
{
    public ModelRenderer side1;
    public ModelRenderer base;
    public ModelRenderer conncetionside1;
    public ModelRenderer side2;
    public ModelRenderer Connection;
    public ModelRenderer Csonnectionside2;

    public ModelConnectorStructural() {
        this.textureWidth = 38;
        this.textureHeight = 24;
        this.base = new ModelRenderer(this, 6, 14);
        this.base.setRotationPoint(8.0F, 8.0F, 8.0F);
        this.base.addBox(-4.0F, 6.0F, -4.0F, 8, 2, 8, 0.0F);
        
        this.side1 = new ModelRenderer(this, 0, 0);
        this.side1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.side1.addBox(2.0F, 0.0F, -4.0F, 2, 6, 8, 0.0F);
        this.side2 = new ModelRenderer(this, 0, 0);
        this.side2.mirror = true;
        this.side2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.side2.addBox(-4.0F, 0.0F, -4.0F, 2, 6, 8, 0.0F);

        this.Connection = new ModelRenderer(this, 0, 14);
        this.Connection.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Connection.addBox(-2.0F, 1.5F, -1.0F, 4, 2, 2, 0.0F);
        this.Csonnectionside2 = new ModelRenderer(this, 0, 0);
        this.Csonnectionside2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Csonnectionside2.addBox(-5.0F, 1.5F, -1.0F, 1, 2, 2, 0.0F);
        this.conncetionside1 = new ModelRenderer(this, 0, 0);
        this.conncetionside1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.conncetionside1.addBox(4.0F, 1.5F, -1.0F, 1, 2, 2, 0.0F);
        
        this.base.addChild(side1);
        this.base.addChild(side2);
        this.base.addChild(conncetionside1);
        this.base.addChild(Csonnectionside2);
        this.base.addChild(Connection);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
//        this.Connection.render(f5);
        this.base.render(f5);
//        this.side1.render(f5);
//        this.Csonnectionside2.render(f5);
//        this.conncetionside1.render(f5);
//        this.side2.render(f5);
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
