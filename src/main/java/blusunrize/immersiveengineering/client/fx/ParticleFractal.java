package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author BluSunrize - 13.05.2018
 */
@SideOnly(Side.CLIENT)
public class ParticleFractal extends Particle
{
	public static final Deque<ParticleFractal> PARTICLE_FRACTAL_DEQUE = new ArrayDeque<>();

	public static final float[][] COLOUR_RED = {{.79f, .31f, .31f, .5f}, {1, .97f, .87f, .75f}};
	public static final float[][] COLOUR_ORANGE = {{Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], .5f}, {1, .97f, .87f, .75f}};
	public static final float[][] COLOUR_LIGHTNING = {{77/255f, 74/255f, 152/255f, .75f}, {1, 1, 1, 1}};

	private Vec3d[] pointsList;
	private float[] colourOut;
	private float[] colourIn;
//	private EntityLivingBase attachedEntity;
//	private EnumHandSide attachedHand;

	public ParticleFractal(World world, double x, double y, double z, double speedX, double speedY, double speedZ, Vec3d direction, double scale, int maxAge, int points, float[] colourOut, float[] colourIn)
	{
		super(world, x, y, z, speedX, speedY, speedZ);
		this.particleMaxAge = maxAge;
		this.motionX *= .009f;
		this.motionY *= .009f;
		this.motionZ *= .009f;
		this.colourOut = colourOut;
		this.colourIn = colourIn;

		this.pointsList = new Vec3d[points];
		direction = direction.scale(scale);
		Vec3d startPos = direction.scale(-.5);
		Vec3d end = direction.scale(.5);
		Vec3d dist = end.subtract(startPos);
		for(int i = 0; i < points; i++)
		{
			Vec3d sub = startPos.add(dist.x/points*i, dist.y/points*i, dist.z/points*i);
			//distance to the middle point and by that, distance from the start and end. -1 is start, 1 is end
			double fixPointDist = (i-points/2)/(points/2);
			//Randomization modifier, closer to start/end means smaller divergence
			double mod = scale*1-.45*Math.abs(fixPointDist);
			double offX = (rand.nextDouble()-.5)*mod;
			double offY = (rand.nextDouble()-.5)*mod;
			double offZ = (rand.nextDouble()-.5)*mod;
			this.pointsList[i] = sub.add(offX, offY, offZ);
		}
	}

	public ParticleFractal(World world, double x, double y, double z, Vec3d direction, double scale, float[] colourOut, float[] colourIn)
	{
		this(world, x, y, z, 0, 0, 0, direction, scale, 10, 16, colourOut, colourIn);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		PARTICLE_FRACTAL_DEQUE.add(this);
//		if(attachedEntity!=null)
//		{
//			Vec3d pos = Utils.getLivingFrontPos(attachedEntity, .75, attachedEntity.height*.75, attachedHand)
//		}
	}
//
//	@Override
//	public void onUpdate()
//	{
//		super.onUpdate();
//	}

	public void render(Tessellator tessellator, BufferBuilder buffer, float partialTicks)
	{
		float mod = (particleAge+partialTicks)/(float)particleMaxAge;
		int iStart = 0;
		int iEnd = pointsList.length;
		if(mod >= .76)
		{
			float rem = (((mod-.7599f)%.25f)*48)/2f;
			iStart += Math.ceil(rem);
			iEnd -= Math.floor(rem);
		}
		mod = .3f+mod*mod;

		Vec3d[] vectorsScaled = new Vec3d[iEnd];
		Vec3d vecRender;

		GlStateManager.glLineWidth(4f);
		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		for(int i = iStart; i < iEnd; i++)
		{
			vecRender = pointsList[i].scale(mod);//.rotateYaw(3.1415926535f*mod);
			buffer.pos(posX+vecRender.x, posY+vecRender.y, posZ+vecRender.z).color(colourOut[0], colourOut[1], colourOut[2], colourOut[3]).endVertex();
			vectorsScaled[i] = vecRender;
		}
		tessellator.draw();

		GlStateManager.glLineWidth(1f);
		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		for(int i = iEnd-1; i >= iStart; i--)
			buffer.pos(posX+vectorsScaled[i].x, posY+vectorsScaled[i].y, posZ+vectorsScaled[i].z).color(colourIn[0], colourIn[1], colourIn[2], colourIn[3]).endVertex();
		tessellator.draw();

		GL11.glPointSize(8f);
		buffer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
		for(int i = iStart; i < iEnd; i++)
			buffer.pos(posX+vectorsScaled[i].x, posY+vectorsScaled[i].y, posZ+vectorsScaled[i].z).color(colourOut[0], colourOut[1], colourOut[2], colourOut[3]).endVertex();
		tessellator.draw();

		GL11.glPointSize(2f);
		buffer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
		for(int i = iEnd-1; i >= iStart; i--)
			buffer.pos(posX+vectorsScaled[i].x, posY+vectorsScaled[i].y, posZ+vectorsScaled[i].z).color(colourIn[0], colourIn[1], colourIn[2], colourIn[3]).endVertex();
		tessellator.draw();
	}
}
