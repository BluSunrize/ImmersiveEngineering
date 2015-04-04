package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Transformer_post - Damien A.W Hazard
 * Created using Tabula 4.1.1
 */
public class ModelPostTransformer extends ModelBase
{
    public ModelRenderer PoleConnection1;
    public ModelRenderer Transformer;
    public ModelRenderer PoleConnection2;
    public ModelRenderer ConnectorTop;
    public ModelRenderer ConnectorSide;
    public ModelRenderer top1;
    public ModelRenderer top3;
    public ModelRenderer top4;
    public ModelRenderer top5;
    public ModelRenderer Top2;
    public ModelRenderer side1;
    public ModelRenderer side3;
    public ModelRenderer side2;

    public ModelPostTransformer() {
        this.textureWidth = 128;
        this.textureHeight = 64;
        this.side1 = new ModelRenderer(this, 116, 53);
        this.side1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.side1.addBox(-2.0F, 18.0F, -17.0F, 4, 4, 1, 0.0F);
        this.PoleConnection2 = new ModelRenderer(this, 72, 41);
        this.PoleConnection2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.PoleConnection2.addBox(-1.5F, 10.0F, -5.0F, 3, 4, 3, 0.0F);
        this.Top2 = new ModelRenderer(this, 80, 58);
        this.Top2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Top2.addBox(-1.5F, 3.0F, -10.5F, 3, 1, 3, 0.0F);
        this.top5 = new ModelRenderer(this, 96, 58);
        this.top5.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.top5.addBox(-2.0F, 6.0F, -11.0F, 4, 2, 4, 0.0F);
        this.top1 = new ModelRenderer(this, 80, 53);
        this.top1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.top1.addBox(-2.0F, 2.0F, -11.0F, 4, 1, 4, 0.0F);
        this.ConnectorSide = new ModelRenderer(this, 84, 45);
        this.ConnectorSide.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.ConnectorSide.addBox(-1.5F, 18.5F, -18.0F, 3, 3, 1, 0.0F);
        this.side3 = new ModelRenderer(this, 96, 52);
        this.side3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.side3.addBox(-2.0F, 18.0F, -15.0F, 4, 4, 2, 0.0F);
        this.top3 = new ModelRenderer(this, 80, 53);
        this.top3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.top3.addBox(-2.0F, 4.0F, -11.0F, 4, 1, 4, 0.0F);
        this.top4 = new ModelRenderer(this, 80, 58);
        this.top4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.top4.addBox(-1.5F, 5.0F, -10.5F, 3, 1, 3, 0.0F);
        this.side2 = new ModelRenderer(this, 108, 54);
        this.side2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.side2.addBox(-1.5F, 18.5F, -16.0F, 3, 3, 1, 0.0F);
        this.ConnectorTop = new ModelRenderer(this, 80, 49);
        this.ConnectorTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.ConnectorTop.addBox(-1.5F, 1.0F, -10.5F, 3, 1, 3, 0.0F);
        this.PoleConnection1 = new ModelRenderer(this, 72, 41);
        this.PoleConnection1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.PoleConnection1.addBox(-1.5F, 18.0F, -5.0F, 3, 4, 3, 0.0F);
        this.Transformer = new ModelRenderer(this, 48, 40);
        this.Transformer.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Transformer.addBox(-4.0F, 8.0F, -13.0F, 8, 16, 8, 0.0F);
        this.ConnectorSide.addChild(this.side1);
        this.ConnectorTop.addChild(this.Top2);
        this.ConnectorTop.addChild(this.top5);
        this.ConnectorTop.addChild(this.top1);
        this.ConnectorSide.addChild(this.side3);
        this.ConnectorTop.addChild(this.top3);
        this.ConnectorTop.addChild(this.top4);
        this.ConnectorSide.addChild(this.side2);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    { 
        this.PoleConnection2.render(f5);
        this.ConnectorSide.render(f5);
        this.ConnectorTop.render(f5);
        this.PoleConnection1.render(f5);
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
