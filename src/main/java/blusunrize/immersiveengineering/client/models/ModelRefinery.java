package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * refinery - Damien A.W "Hazard"
 * Created using Tabula 5.0.0
 */
public class ModelRefinery extends ModelBase
{
	public ModelRenderer tank1;
	public ModelRenderer pipe4;
	public ModelRenderer input1;
	public ModelRenderer leg4;
	public ModelRenderer base;
	public ModelRenderer tankbase1;
	public ModelRenderer tank2;
	public ModelRenderer tankbase2;
	public ModelRenderer leg7;
	public ModelRenderer leg6;
	public ModelRenderer leg2;
	public ModelRenderer leg3;
	public ModelRenderer leg1;
	public ModelRenderer leg8;
	public ModelRenderer input2;
	public ModelRenderer pipe8;
	public ModelRenderer pipe2;
	public ModelRenderer Tank3;
	public ModelRenderer pipe3;
	public ModelRenderer pipe5;
	public ModelRenderer shape1;
	public ModelRenderer pipe6;
	public ModelRenderer pipe7;
	public ModelRenderer console_power;
	public ModelRenderer consoleleg3;
	public ModelRenderer consoleleg1;
	public ModelRenderer console_redstone;
	public ModelRenderer consoleleg4;
	public ModelRenderer consoleleg2;
	public ModelRenderer pipe1;
	public ModelRenderer beam4;
	public ModelRenderer leg5;
	public ModelRenderer beam2;
	public ModelRenderer beam7;
	public ModelRenderer beam1;
	public ModelRenderer beam5;
	public ModelRenderer beam8;
	public ModelRenderer beam6;
	public ModelRenderer beam3;

	public ModelRefinery() {
		this.textureWidth = 256;
		this.textureHeight = 182;
		this.base = new ModelRenderer(this, 0, 102);
		this.base.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.base.addBox(-40.0F, 16.0F, -24.0F, 80, 8, 48, 0.0F);
		this.pipe2 = new ModelRenderer(this, 221, 36);
		this.pipe2.mirror = true;
		this.pipe2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.pipe2.addBox(33.0F, -22.0F, -2.0F, 4, 30, 4, 0.0F);
		this.beam4 = new ModelRenderer(this, 6, 96);
		this.beam4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.beam4.addBox(12.0F, 4.0F, 9.0F, 16, 2, 2, 0.0F);
		this.pipe7 = new ModelRenderer(this, 96, 70);
		this.pipe7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.pipe7.addBox(-18.0F, 14.0F, -2.0F, 36, 2, 4, 0.0F);
		this.leg5 = new ModelRenderer(this, 6, 4);
		this.leg5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg5.addBox(28.0F, 2.0F, 8.0F, 4, 14, 4, 0.0F);
		this.consoleleg2 = new ModelRenderer(this, 120, 0);
		this.consoleleg2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.consoleleg2.addBox(-6.0F, 8.0F, 18.0F, 2, 8, 4, 0.0F);
		this.leg8 = new ModelRenderer(this, 6, 4);
		this.leg8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg8.addBox(-32.0F, 2.0F, 8.0F, 4, 14, 4, 0.0F);
		this.consoleleg4 = new ModelRenderer(this, 120, 0);
		this.consoleleg4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.consoleleg4.addBox(36.0F, 8.0F, -22.0F, 2, 8, 4, 0.0F);
		this.input2 = new ModelRenderer(this, 186, 78);
		this.input2.mirror = true;
		this.input2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.input2.addBox(24.0F, 8.0F, -8.0F, 16, 8, 16, 0.0F);
		this.pipe3 = new ModelRenderer(this, 0, 0);
		this.pipe3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.pipe3.addBox(-33.0F, -22.0F, -2.0F, 1, 4, 4, 0.0F);
		this.leg2 = new ModelRenderer(this, 6, 4);
		this.leg2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg2.addBox(-12.0F, 2.0F, -12.0F, 4, 14, 4, 0.0F);
		this.leg7 = new ModelRenderer(this, 6, 4);
		this.leg7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg7.addBox(-12.0F, 2.0F, 8.0F, 4, 14, 4, 0.0F);
		this.console_power = new ModelRenderer(this, 138, 78);
		this.console_power.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.console_power.addBox(-8.0F, -8.0F, 16.0F, 16, 16, 8, 0.0F);
		this.tankbase1 = new ModelRenderer(this, 126, 0);
		this.tankbase1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.tankbase1.addBox(-33.0F, 0.0F, -13.0F, 26, 2, 26, 0.0F);
		this.beam8 = new ModelRenderer(this, 6, 100);
		this.beam8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.beam8.addBox(-31.0F, 4.0F, -8.0F, 2, 2, 16, 0.0F);
		this.pipe6 = new ModelRenderer(this, 176, 28);
		this.pipe6.mirror = true;
		this.pipe6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.pipe6.addBox(18.0F, 2.0F, -2.0F, 4, 14, 4, 0.0F);
		this.beam6 = new ModelRenderer(this, 6, 100);
		this.beam6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.beam6.addBox(-11.0F, 4.0F, -8.0F, 2, 2, 16, 0.0F);
		this.beam5 = new ModelRenderer(this, 6, 100);
		this.beam5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.beam5.addBox(9.0F, 4.0F, -8.0F, 2, 2, 16, 0.0F);
		this.beam7 = new ModelRenderer(this, 6, 96);
		this.beam7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.beam7.addBox(-28.0F, 4.0F, 9.0F, 16, 2, 2, 0.0F);
		this.beam3 = new ModelRenderer(this, 6, 100);
		this.beam3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.beam3.addBox(29.0F, 4.0F, -8.0F, 2, 2, 16, 0.0F);
		this.tankbase2 = new ModelRenderer(this, 126, 0);
		this.tankbase2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.tankbase2.addBox(7.0F, 0.0F, -13.0F, 26, 2, 26, 0.0F);
		this.leg4 = new ModelRenderer(this, 6, 4);
		this.leg4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg4.addBox(28.0F, 2.0F, -12.0F, 4, 14, 4, 0.0F);
		this.leg6 = new ModelRenderer(this, 6, 4);
		this.leg6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg6.addBox(8.0F, 2.0F, 8.0F, 4, 14, 4, 0.0F);
		this.tank2 = new ModelRenderer(this, 0, 48);
		this.tank2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.tank2.addBox(8.0F, -24.0F, -12.0F, 24, 24, 24, 0.0F);
		this.shape1 = new ModelRenderer(this, 56, 158);
		this.shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.shape1.addBox(-8.0F, 8.0F, -24.0F, 16, 8, 16, 0.0F);
		this.pipe1 = new ModelRenderer(this, 221, 36);
		this.pipe1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.pipe1.addBox(-37.0F, -22.0F, -2.0F, 4, 30, 4, 0.0F);
		this.tank1 = new ModelRenderer(this, 0, 0);
		this.tank1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.tank1.addBox(-32.0F, -24.0F, -12.0F, 24, 24, 24, 0.0F);
		this.pipe5 = new ModelRenderer(this, 176, 28);
		this.pipe5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.pipe5.addBox(-22.0F, 2.0F, -2.0F, 4, 14, 4, 0.0F);
		this.beam1 = new ModelRenderer(this, 6, 96);
		this.beam1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.beam1.addBox(-28.0F, 4.0F, -11.0F, 16, 2, 2, 0.0F);
		this.pipe4 = new ModelRenderer(this, 0, 0);
		this.pipe4.mirror = true;
		this.pipe4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.pipe4.addBox(32.0F, -22.0F, -2.0F, 1, 4, 4, 0.0F);
		this.console_redstone = new ModelRenderer(this, 138, 46);
		this.console_redstone.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.console_redstone.addBox(24.0F, -8.0F, -24.0F, 16, 16, 8, 0.0F);
		this.leg1 = new ModelRenderer(this, 6, 4);
		this.leg1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg1.addBox(-32.0F, 2.0F, -12.0F, 4, 14, 4, 0.0F);
		this.Tank3 = new ModelRenderer(this, 82, 34);
		this.Tank3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.Tank3.addBox(-7.0F, -8.0F, -23.0F, 14, 16, 14, 0.0F);
		this.consoleleg1 = new ModelRenderer(this, 120, 0);
		this.consoleleg1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.consoleleg1.addBox(4.0F, 8.0F, 18.0F, 2, 8, 4, 0.0F);
		this.consoleleg3 = new ModelRenderer(this, 120, 0);
		this.consoleleg3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.consoleleg3.addBox(26.0F, 8.0F, -22.0F, 2, 8, 4, 0.0F);
		this.pipe8 = new ModelRenderer(this, 130, 18);
		this.pipe8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.pipe8.addBox(-2.0F, 14.0F, -8.0F, 4, 2, 6, 0.0F);
		this.beam2 = new ModelRenderer(this, 6, 96);
		this.beam2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.beam2.addBox(12.0F, 4.0F, -11.0F, 16, 2, 2, 0.0F);
		this.input1 = new ModelRenderer(this, 72, 0);
		this.input1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.input1.addBox(-40.0F, 8.0F, -8.0F, 16, 8, 16, 0.0F);
		this.leg3 = new ModelRenderer(this, 6, 4);
		this.leg3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg3.addBox(8.0F, 2.0F, -12.0F, 4, 14, 4, 0.0F);

		this.base.addChild(this.pipe2);
		this.base.addChild(this.beam4);
		this.base.addChild(this.pipe7);
		this.base.addChild(this.leg5);
		this.base.addChild(this.consoleleg2);
		this.base.addChild(this.leg8);
		this.base.addChild(this.consoleleg4);
		this.base.addChild(this.input2);
		this.base.addChild(this.pipe3);
		this.base.addChild(this.leg2);
		this.base.addChild(this.leg7);
		this.base.addChild(this.console_power);
		this.base.addChild(this.tankbase1);
		this.base.addChild(this.beam8);
		this.base.addChild(this.pipe6);
		this.base.addChild(this.beam6);
		this.base.addChild(this.beam5);
		this.base.addChild(this.beam7);
		this.base.addChild(this.beam3);
		this.base.addChild(this.tankbase2);
		this.base.addChild(this.leg4);
		this.base.addChild(this.leg6);
		this.base.addChild(this.tank2);
		this.base.addChild(this.shape1);
		this.base.addChild(this.pipe1);
		this.base.addChild(this.tank1);
		this.base.addChild(this.pipe5);
		this.base.addChild(this.beam1);
		this.base.addChild(this.pipe4);
		this.base.addChild(this.console_redstone);
		this.base.addChild(this.leg1);
		this.base.addChild(this.Tank3);
		this.base.addChild(this.consoleleg1);
		this.base.addChild(this.consoleleg3);
		this.base.addChild(this.pipe8);
		this.base.addChild(this.beam2);
		this.base.addChild(this.input1);
		this.base.addChild(this.leg3);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.base.render(f5);
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
