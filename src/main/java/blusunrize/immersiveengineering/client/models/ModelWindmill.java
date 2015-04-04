package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * windmill - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelWindmill extends ModelBase {
    public ModelRenderer axel;
    public ModelRenderer sail2;
    public ModelRenderer rod3;
    public ModelRenderer rod2;
    public ModelRenderer sail3;
    public ModelRenderer rod4;
    public ModelRenderer sail4;
    public ModelRenderer sail1;
    public ModelRenderer rod1;

    public ModelWindmill() {
        this.textureWidth = 256;
        this.textureHeight = 256;
        this.rod2 = new ModelRenderer(this, 0, 0);
        this.rod2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.rod2.addBox(2.0F, -1.5F, -5.5F, 96, 3, 3, 0.0F);
        this.rod4 = new ModelRenderer(this, 0, 0);
        this.rod4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.rod4.addBox(-98.0F, -1.5F, -5.5F, 96, 3, 3, 0.0F);
        this.rod3 = new ModelRenderer(this, 0, 8);
        this.rod3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.rod3.addBox(-1.5F, 2.0F, -5.5F, 3, 96, 3, 0.0F);
        this.sail1 = new ModelRenderer(this, 0, 127);
        this.sail1.mirror = true;
        this.sail1.setRotationPoint(0.0F, 0.0F, -1.0F);
        this.sail1.addBox(-1.0F, -96.0F, -4.5F, 18, 84, 1, 0.0F);
        this.setRotateAngle(sail1, 0.0F, -0.2617993877991494F, 0.0F);
        this.sail3 = new ModelRenderer(this, 0, 127);
        this.sail3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.sail3.addBox(-17.0F, 12.0F, -4.5F, 18, 84, 1, 0.0F);
        this.setRotateAngle(sail3, 0.0F, 0.2617993877991494F, 0.0F);
        this.sail4 = new ModelRenderer(this, 0, 108);
        this.sail4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.sail4.addBox(-96.0F, -18.0F, -4.5F, 84, 18, 1, 0.0F);
        this.setRotateAngle(sail4, -0.2617993877991494F, -0.0F, 0.0F);
        this.rod1 = new ModelRenderer(this, 0, 8);
        this.rod1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.rod1.addBox(-1.5F, -98.0F, -5.5F, 3, 96, 3, 0.0F);
        this.axel = new ModelRenderer(this, 12, 6);
        this.axel.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.axel.addBox(-2.0F, -2.0F, -8.0F, 4, 4, 16, 0.0F);
        this.sail2 = new ModelRenderer(this, 0, 108);
        this.sail2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.sail2.addBox(12.0F, -1.0F, -4.5F, 84, 18, 1, 0.0F);
        this.setRotateAngle(sail2, 0.2617993877991494F, -0.0F, 0.0F);
        this.axel.addChild(this.rod2);
        this.axel.addChild(this.rod4);
        this.axel.addChild(this.rod3);
        this.axel.addChild(this.sail1);
        this.axel.addChild(this.sail3);
        this.axel.addChild(this.sail4);
        this.axel.addChild(this.rod1);
        this.axel.addChild(this.sail2);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
        this.axel.render(f5);
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
