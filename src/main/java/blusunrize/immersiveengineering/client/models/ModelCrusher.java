package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * crusher - Damien A.W Hazard
 * Created using Tabula 5.0.0
 */
public class ModelCrusher extends ModelBase
{
	public ModelRenderer leg8;
	public ModelRenderer leg4;
	public ModelRenderer leg2;
	public ModelRenderer shape1;
	public ModelRenderer grinder5;
	public ModelRenderer grinder3;
	public ModelRenderer grinder6;
	public ModelRenderer output;
	public ModelRenderer shape1_1;
	public ModelRenderer console;
	public ModelRenderer grinder1;
	public ModelRenderer console_redstone;
	public ModelRenderer leg3;
	public ModelRenderer leg7;
	public ModelRenderer leg6;
	public ModelRenderer leg5;
	public ModelRenderer motor3;
	public ModelRenderer leg1;
	public ModelRenderer base;
	public ModelRenderer shape1_3;
	public ModelRenderer motor;
	public ModelRenderer motor2;
	public ModelRenderer grinder4;
	public ModelRenderer axle1;
	public ModelRenderer axle2;
	public ModelRenderer belt;
	public ModelRenderer cr17;
	public ModelRenderer cr16;
	public ModelRenderer cr15;
	public ModelRenderer cr14;
	public ModelRenderer cr13;
	public ModelRenderer cr12;
	public ModelRenderer cr10;
	public ModelRenderer cr11;
	public ModelRenderer cr8;
	public ModelRenderer cr7;
	public ModelRenderer cr6;
	public ModelRenderer cr5;
	public ModelRenderer cr4;
	public ModelRenderer cr3;
	public ModelRenderer shape1_4;
	public ModelRenderer cr2;
	public ModelRenderer cr9;

	public ModelCrusher() {
		this.textureWidth = 256;
		this.textureHeight = 218;
		this.grinder1 = new ModelRenderer(this, 64, 136);
		this.grinder1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.grinder1.addBox(-8.0F, 0.0F, -8.0F, 16, 16, 16, 0.0F);
		this.cr11 = new ModelRenderer(this, 160, 136);
		this.cr11.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr11.addBox(-8.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr11, -3.141592653589793F, 0.0F, 0.0F);
		this.grinder6 = new ModelRenderer(this, 3, 26);
		this.grinder6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.grinder6.addBox(-21.0F, -24.0F, -17.0F, 4, 20, 34, 0.0F);
		this.cr14 = new ModelRenderer(this, 160, 136);
		this.cr14.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr14.addBox(-20.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr14, -3.141592653589793F, 0.0F, 0.0F);
		this.leg6 = new ModelRenderer(this, 1, 41);
		this.leg6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg6.addBox(3.9F, -7.3F, -19.0F, 16, 3, 2, 0.0F);
		this.setRotateAngle(leg6, 0.0F, 0.0F, 0.7853981633974483F);
		this.leg7 = new ModelRenderer(this, 1, 41);
		this.leg7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg7.addBox(3.9F, -7.3F, 17.0F, 16, 3, 2, 0.0F);
		this.setRotateAngle(leg7, 0.0F, 0.0F, 0.7853981633974483F);
		this.cr7 = new ModelRenderer(this, 160, 136);
		this.cr7.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr7.addBox(-26.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr7, -3.141592653589793F, 0.0F, 0.0F);
		this.cr4 = new ModelRenderer(this, 160, 136);
		this.cr4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr4.addBox(-14.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr4, -3.141592653589793F, 0.0F, 0.0F);
		this.shape1 = new ModelRenderer(this, 86, 24);
		this.shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.shape1.addBox(-21.0F, -4.0F, -21.0F, 42, 4, 42, 0.0F);
		this.motor = new ModelRenderer(this, 0, 136);
		this.motor.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.motor.addBox(24.0F, 0.0F, -8.0F, 16, 16, 16, 0.0F);
		this.cr6 = new ModelRenderer(this, 160, 136);
		this.cr6.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr6.addBox(-22.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr6, -3.141592653589793F, 0.0F, 0.0F);
		this.cr13 = new ModelRenderer(this, 160, 136);
		this.cr13.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr13.addBox(-16.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr13, -3.141592653589793F, 0.0F, 0.0F);
		this.cr15 = new ModelRenderer(this, 160, 136);
		this.cr15.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr15.addBox(-24.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr15, -3.141592653589793F, 0.0F, 0.0F);
		this.leg4 = new ModelRenderer(this, 10, 6);
		this.leg4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg4.addBox(-20.0F, 0.0F, 16.0F, 4, 16, 4, 0.0F);
		this.base = new ModelRenderer(this, 0, 80);
		this.base.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.base.addBox(-40.0F, 16.0F, -24.0F, 80, 8, 48, 0.0F);
		this.cr5 = new ModelRenderer(this, 160, 136);
		this.cr5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr5.addBox(-18.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr5, -3.141592653589793F, 0.0F, 0.0F);
		this.axle2 = new ModelRenderer(this, 2, 198);
		this.axle2.setRotationPoint(17.0F, -14.0F, -8.5F);
		this.axle2.addBox(-34.0F, -5.0F, -5.0F, 34, 10, 10, 0.0F);
		this.setRotateAngle(axle2, 3.141592653589793F, 0.0F, 0.0F);
		this.axle1 = new ModelRenderer(this, 2, 198);
		this.axle1.setRotationPoint(17.0F, -14.0F, 8.5F);
		this.axle1.addBox(-34.0F, -5.0F, -5.0F, 34, 10, 10, 0.0F);
		this.setRotateAngle(axle1, 3.141592653589794F, 0.0F, 0.0F);
		this.cr16 = new ModelRenderer(this, 160, 136);
		this.cr16.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr16.addBox(-28.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr16, -3.141592653589793F, 0.0F, 0.0F);
		this.leg8 = new ModelRenderer(this, 1, 41);
		this.leg8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg8.addBox(-19.9F, -7.3F, 17.0F, 16, 3, 2, 0.0F);
		this.setRotateAngle(leg8, 0.0F, 0.0F, -0.7853981633974483F);
		this.cr8 = new ModelRenderer(this, 160, 136);
		this.cr8.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr8.addBox(-30.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr8, -3.141592653589793F, 0.0F, 0.0F);
		this.cr10 = new ModelRenderer(this, 160, 136);
		this.cr10.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr10.addBox(-4.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr10, -3.141592653589793F, 0.0F, 0.0F);
		this.cr2 = new ModelRenderer(this, 160, 136);
		this.cr2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr2.addBox(-6.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr2, -3.141592653589793F, 0.0F, 0.0F);
		this.shape1_1 = new ModelRenderer(this, 45, 24);
		this.shape1_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.shape1_1.addBox(26.0F, 8.0F, -22.0F, 2, 8, 4, 0.0F);
		this.output = new ModelRenderer(this, 45, 24);
		this.output.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.output.addBox(-8.0F, 8.0F, -24.0F, 16, 8, 16, 0.0F);
		this.motor3 = new ModelRenderer(this, 10, 26);
		this.motor3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.motor3.addBox(27.0F, 4.0F, 8.0F, 10, 12, 3, 0.0F);
		this.cr12 = new ModelRenderer(this, 160, 136);
		this.cr12.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr12.addBox(-12.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr12, -3.141592653589793F, 0.0F, 0.0F);
		this.leg3 = new ModelRenderer(this, 10, 6);
		this.leg3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg3.addBox(16.0F, 0.0F, 16.0F, 4, 16, 4, 0.0F);
		this.leg1 = new ModelRenderer(this, 10, 6);
		this.leg1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg1.addBox(-20.0F, 0.0F, -20.0F, 4, 16, 4, 0.0F);
		this.grinder4 = new ModelRenderer(this, 36, 0);
		this.grinder4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.grinder4.addBox(-21.0F, -24.0F, 17.0F, 42, 20, 4, 0.0F);
		this.cr9 = new ModelRenderer(this, 160, 136);
		this.cr9.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr9.addBox(-34.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr9, -3.141592653589793F, 0.0F, 0.0F);
		this.motor2 = new ModelRenderer(this, 10, 26);
		this.motor2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.motor2.addBox(27.0F, 4.0F, -11.0F, 10, 12, 3, 0.0F);
		this.console_redstone = new ModelRenderer(this, 0, 80);
		this.console_redstone.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.console_redstone.addBox(24.0F, -8.0F, -24.0F, 16, 16, 8, 0.0F);
		this.belt = new ModelRenderer(this, 208, 167);
		this.belt.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.belt.addBox(20.0F, -18.0F, -5.0F, 4, 31, 10, 0.0F);
		this.grinder3 = new ModelRenderer(this, 128, 0);
		this.grinder3.mirror = true;
		this.grinder3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.grinder3.addBox(-21.0F, -24.0F, -21.0F, 42, 20, 4, 0.0F);
		this.grinder5 = new ModelRenderer(this, 3, 26);
		this.grinder5.mirror = true;
		this.grinder5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.grinder5.addBox(17.0F, -24.0F, -17.0F, 4, 20, 34, 0.0F);
		this.leg5 = new ModelRenderer(this, 1, 41);
		this.leg5.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg5.addBox(-19.9F, -7.3F, -19.0F, 16, 3, 2, 0.0F);
		this.setRotateAngle(leg5, 0.0F, 0.0F, -0.7853981633974483F);
		this.shape1_3 = new ModelRenderer(this, 45, 24);
		this.shape1_3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.shape1_3.addBox(36.0F, 8.0F, -22.0F, 2, 8, 4, 0.0F);
		this.cr17 = new ModelRenderer(this, 160, 136);
		this.cr17.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr17.addBox(-32.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr17, -3.141592653589793F, 0.0F, 0.0F);
		this.console = new ModelRenderer(this, 80, 136);
		this.console.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.console.addBox(-40.0F, -8.0F, -24.0F, 16, 24, 48, 0.0F);
		this.leg2 = new ModelRenderer(this, 10, 6);
		this.leg2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.leg2.addBox(16.0F, 0.0F, -20.0F, 4, 16, 4, 0.0F);
		this.shape1_4 = new ModelRenderer(this, 160, 136);
		this.shape1_4.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.shape1_4.addBox(-2.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(shape1_4, -3.141592653589793F, 0.0F, 0.0F);
		this.cr3 = new ModelRenderer(this, 160, 136);
		this.cr3.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.cr3.addBox(-10.0F, -6.5F, -6.5F, 2, 13, 13, 0.0F);
		this.setRotateAngle(cr3, -3.141592653589793F, 0.0F, 0.0F);

		this.axle1.addChild(this.cr11);
		this.axle1.addChild(this.cr14);
		this.axle2.addChild(this.cr7);
		this.axle2.addChild(this.cr4);
		this.axle2.addChild(this.cr6);
		this.axle1.addChild(this.cr13);
		this.axle1.addChild(this.cr15);
		this.axle2.addChild(this.cr5);
		this.axle1.addChild(this.cr16);
		this.axle2.addChild(this.cr8);
		this.axle1.addChild(this.cr10);
		this.axle2.addChild(this.cr2);
		this.axle1.addChild(this.cr12);
		this.axle2.addChild(this.cr9);
		this.axle1.addChild(this.cr17);
		this.axle2.addChild(this.shape1_4);
		this.axle2.addChild(this.cr3);

		this.base.addChild(this.output);
		this.base.addChild(this.motor);
		this.base.addChild(this.motor2);
		this.base.addChild(this.motor3);
		this.base.addChild(this.belt);
		this.base.addChild(this.console);
		this.base.addChild(this.console_redstone);
		this.base.addChild(this.shape1_3);
		this.base.addChild(this.shape1_1);
		
		this.base.addChild(this.grinder1);
		this.base.addChild(this.shape1);
		this.base.addChild(this.grinder3);
		this.base.addChild(this.grinder4);
		this.base.addChild(this.grinder5);
		this.base.addChild(this.grinder6);

		this.base.addChild(this.leg1);
		this.base.addChild(this.leg2);
		this.base.addChild(this.leg3);
		this.base.addChild(this.leg4);
		this.base.addChild(this.leg5);
		this.base.addChild(this.leg6);
		this.base.addChild(this.leg7);
		this.base.addChild(this.leg8);
		
		this.base.addChild(this.axle1);
		this.base.addChild(this.axle2);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{ 
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
