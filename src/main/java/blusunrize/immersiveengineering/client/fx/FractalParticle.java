package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author BluSunrize - 13.05.2018
 */
@OnlyIn(Dist.CLIENT)
public class FractalParticle extends Particle
{
	public static final Deque<FractalParticle> PARTICLE_FRACTAL_DEQUE = new ArrayDeque<>();

	public static final float[][] COLOUR_RED = {{.79f, .31f, .31f, .5f}, {1, .97f, .87f, .75f}};
	public static final float[][] COLOUR_ORANGE = {{Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], .5f}, {1, .97f, .87f, .75f}};
	public static final float[][] COLOUR_LIGHTNING = {{77/255f, 74/255f, 152/255f, .75f}, {1, 1, 1, 1}};

	private Vec3d[] pointsList;
	private float[] colourOut;
	private float[] colourIn;

	public FractalParticle(World world, double x, double y, double z, double speedX, double speedY, double speedZ, Vec3d direction, double scale, int maxAge, int points, float[] colourOut, float[] colourIn)
	{
		super(world, x, y, z, speedX, speedY, speedZ);
		this.maxAge = maxAge;
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

	public FractalParticle(World world, double x, double y, double z, Vec3d direction, double scale, float[] colourOut, float[] colourIn)
	{
		this(world, x, y, z, 0, 0, 0, direction, scale, 10, 16, colourOut, colourIn);
	}

	@Override
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks)
	{
		PARTICLE_FRACTAL_DEQUE.add(this);
	}

	@Override
	public IParticleRenderType getRenderType()
	{
		return IParticleRenderType.CUSTOM;
	}

	public void render(Tessellator tessellator, BufferBuilder buffer, float partialTicks)
	{
		float mod = (age+partialTicks)/(float)maxAge;
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

		GlStateManager.lineWidth(4f);
		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		for(int i = iStart; i < iEnd; i++)
		{
			vecRender = pointsList[i].scale(mod);//.rotateYaw(3.1415926535f*mod);
			buffer.pos(posX+vecRender.x, posY+vecRender.y, posZ+vecRender.z).color(colourOut[0], colourOut[1], colourOut[2], colourOut[3]).endVertex();
			vectorsScaled[i] = vecRender;
		}
		tessellator.draw();

		GlStateManager.lineWidth(1f);
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

	public static class Data implements IParticleData
	{

		private final Vec3d direction;
		private final double scale;
		private final int maxAge;
		private final int points;
		private final float[] colourOut;
		private final float[] colourIn;

		public Data(Vec3d direction, double scale, int maxAge, int points, float[] colourOut, float[] colourIn)
		{
			this.direction = direction;
			this.scale = scale;
			this.maxAge = maxAge;
			this.points = points;
			this.colourOut = colourOut;
			this.colourIn = colourIn;
		}

		@Override
		public ParticleType<?> getType()
		{
			return IEParticles.FLUID_SPLASH;
		}

		@Override
		public void write(PacketBuffer buffer)
		{
			buffer.writeDouble(direction.x).writeDouble(direction.y).writeDouble(direction.z);
			buffer.writeDouble(scale);
			buffer.writeInt(maxAge)
					.writeInt(points);
			for(int i = 0; i < 4; ++i)
				buffer.writeFloat(colourOut[i]);
			for(int i = 0; i < 4; ++i)
				buffer.writeFloat(colourIn[i]);
		}

		@Override
		public String getParameters()
		{
			String ret = direction.x+" "+
					direction.y+" "+
					direction.z+" "+
					scale+" "+
					maxAge+" "+
					points;
			for(int i = 0; i < 4; ++i)
				ret += " "+colourOut[i];
			for(int i = 0; i < 4; ++i)
				ret += " "+colourIn[i];
			return ret;
		}
	}

	public static class DataDeserializer implements IDeserializer<Data>
	{

		@Override
		public Data deserialize(ParticleType<Data> particleTypeIn, StringReader reader) throws CommandSyntaxException
		{
			double dX = reader.readDouble();
			reader.expect(' ');
			double dY = reader.readDouble();
			reader.expect(' ');
			double dZ = reader.readDouble();
			reader.expect(' ');
			double scale = reader.readDouble();
			reader.expect(' ');
			int maxAge = reader.readInt();
			reader.expect(' ');
			int points = reader.readInt();
			reader.expect(' ');
			float[] colourOut = new float[4];
			float[] colourIn = new float[4];
			for(int i = 0; i < 4; ++i)
			{
				colourOut[i] = reader.readFloat();
				reader.expect(' ');
			}
			for(int i = 0; i < 4; ++i)
			{
				colourIn[i] = reader.readFloat();
				reader.expect(' ');
			}

			return new Data(new Vec3d(dX, dY, dZ), scale, maxAge, points, colourOut, colourIn);
		}

		@Override
		public Data read(ParticleType<Data> particleTypeIn, PacketBuffer buffer)
		{
			Vec3d dir = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
			;
			double scale = buffer.readDouble();
			int maxAge = buffer.readInt();
			int points = buffer.readInt();
			float[] colourOut = new float[4];
			float[] colourIn = new float[4];
			for(int i = 0; i < 4; ++i)
				colourOut[i] = buffer.readFloat();
			for(int i = 0; i < 4; ++i)
				colourIn[i] = buffer.readFloat();
			return new Data(dir, scale, maxAge, points, colourOut, colourIn);
		}
	}

	public static class Factory implements IParticleFactory<Data>
	{

		@Nullable
		@Override
		public Particle makeParticle(Data typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new FractalParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.direction, typeIn.scale,
					typeIn.maxAge, typeIn.points, typeIn.colourOut, typeIn.colourIn);
		}
	}
}
