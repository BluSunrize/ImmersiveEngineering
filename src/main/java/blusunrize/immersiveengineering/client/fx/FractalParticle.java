/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import javax.annotation.Nullable;
import java.lang.Math;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 13.05.2018
 */
public class FractalParticle extends Particle
{
	public static final Deque<FractalParticle> PARTICLE_FRACTAL_DEQUE = new ArrayDeque<>();

	public static final float[][] COLOUR_RED = {{.79f, .31f, .31f, .5f}, {1, .97f, .87f, .75f}};
	public static final float[][] COLOUR_ORANGE = {{Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], .5f}, {1, .97f, .87f, .75f}};
	public static final float[][] COLOUR_LIGHTNING = {{77/255f, 74/255f, 152/255f, .75f}, {1, 1, 1, 1}};

	private final Vector3f[] pointsList;
	private final float[] colourOut;
	private final float[] colourIn;

	public FractalParticle(ClientLevel world, double x, double y, double z, double speedX, double speedY, double speedZ, Vec3 direction, double scale, int maxAge, int points, float[] colourOut, float[] colourIn)
	{
		super(world, x, y, z, speedX, speedY, speedZ);
		this.lifetime = maxAge;
		this.xd *= .009f;
		this.yd *= .009f;
		this.zd *= .009f;
		this.colourOut = colourOut;
		this.colourIn = colourIn;

		this.pointsList = new Vector3f[points];
		direction = direction.scale(scale);
		Vec3 startPos = direction.scale(-.5);
		Vec3 end = direction.scale(.5);
		Vec3 dist = end.subtract(startPos);
		for(int i = 0; i < points; i++)
		{
			Vec3 sub = startPos.add(dist.x/points*i, dist.y/points*i, dist.z/points*i);
			//distance to the middle point and by that, distance from the start and end. -1 is start, 1 is end
			double fixPointDist = (i-points/2)/(points/2);
			//Randomization modifier, closer to start/end means smaller divergence
			double mod = scale*1-.45*Math.abs(fixPointDist);
			double offX = (random.nextDouble()-.5)*mod;
			double offY = (random.nextDouble()-.5)*mod;
			double offZ = (random.nextDouble()-.5)*mod;
			Vec3 pointDouble = sub.add(offX, offY, offZ);
			this.pointsList[i] = new Vector3f((float)pointDouble.x, (float)pointDouble.y, (float)pointDouble.z);
		}
	}

	@Override
	public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks)
	{
		// Use a custom render queue to allow the particles to use multiple render types (by default particles can only
		// use one)
		PARTICLE_FRACTAL_DEQUE.add(this);
	}

	@Override
	public ParticleRenderType getRenderType()
	{
		return ParticleRenderType.CUSTOM;
	}

	public List<Pair<RenderType, Consumer<VertexConsumer>>> render(float partialTicks, PoseStack matrixStack)
	{
		float mod = (age+partialTicks)/(float)lifetime;
		int iStartMut = 0;
		int iEndMut = pointsList.length;
		if(mod >= .76)
		{
			float rem = (((mod-.7599f)%.25f)*48)/2f;
			iStartMut += Math.ceil(rem);
			iEndMut -= Math.floor(rem);
		}
		final int iStart = iStartMut;
		final int iEnd = iEndMut;
		mod = .3f+mod*mod;
		matrixStack.pushPose();
		matrixStack.translate(x, y, z);
		matrixStack.scale(mod, mod, mod);
		matrixStack.mulPose(new Quaternionf().rotateXYZ(0, Mth.PI*mod, 0));
		Matrix4f transform = matrixStack.last().pose();
		Matrix3f transformN = matrixStack.last().normal();
		matrixStack.popPose();

		LinePointProcessor putLinePoint = (buffer, i, color) -> {
			int correctIndex = getCyclicIndexInRange(iStart, iEnd, i);
			Vector3f vecRender = pointsList[correctIndex];
			if(i!=iStart)
			{
				Vector3f last = pointsList[getCyclicIndexInRange(iStart, iEnd, i-1)];
				renderLinePoint(transformN, transform, vecRender, last, color, buffer, false);
			}
			if(i!=iEnd)
			{
				Vector3f next = pointsList[getCyclicIndexInRange(iStart, iEnd, i+1)];
				renderLinePoint(transformN, transform, next, vecRender, color, buffer, true);
			}
		};

		List<Pair<RenderType, Consumer<VertexConsumer>>> ret = new ArrayList<>();
		ret.add(Pair.of(IERenderTypes.getParticleLines(4f), buffer -> {
			for(int i = iStart; i <= iEnd; i++)
				putLinePoint.draw(buffer, i, colourOut);
		}));

		ret.add(Pair.of(IERenderTypes.getParticleLines(1f), buffer -> {
			for(int i = iStart; i <= iEnd; i++)
				putLinePoint.draw(buffer, i, colourIn);
		}));

		ret.add(Pair.of(IERenderTypes.POINTS, buffer -> {
			for(int i = iStart; i < iEnd; i++)
				drawPoint(buffer, transform, pointsList[i], 8, colourOut);
		}));

		ret.add(Pair.of(IERenderTypes.POINTS, buffer -> {
			for(int i = iEnd-1; i >= iStart; i--)
				drawPoint(buffer, transform, pointsList[i], 2, colourIn);
		}));
		return ret;
	}

	private static int getCyclicIndexInRange(int being, int end, int i)
	{
		return being+Mth.positiveModulo(i-being, end-being);
	}

	private static void renderLinePoint(
			Matrix3f transformN, Matrix4f transform, Vector3f start, Vector3f end, float[] color, VertexConsumer buffer, boolean atStart
	)
	{
		Vector3f normal = new Vector3f(start.x()-end.x(), start.y()-end.y(), start.z()-end.z());
		normal.mul(transformN);
		normal.normalize();
		Vector3f here = atStart?start: end;
		buffer.vertex(transform, here.x(), here.y(), here.z())
				.color(color[0], color[1], color[2], color[3])
				.normal(normal.x(), normal.y(), normal.z())
				.endVertex();
	}

	private static final Vector3f[] POINT_NORMALS = {
			new Vector3f(-1, -1, 0),
			new Vector3f(1, -1, 0),
			new Vector3f(1, 1, 0),
			new Vector3f(-1, 1, 0),
	};

	private void drawPoint(VertexConsumer builder, Matrix4f transform, Vector3f center, float size, float[] color)
	{
		Vector4f vector4f = new Vector4f(center.x(), center.y(), center.z(), 1.0F);
		vector4f.mul(transform);
		vector4f.div(vector4f.w);
		for(Vector3f normal : POINT_NORMALS)
			builder.vertex(vector4f.x(), vector4f.y(), vector4f.z())
					.color(color[0], color[1], color[2], color[3])
					// Passing this as the normal is a hack, and needs to be scaled since normals are encoded with 1 byte in the range [-1, 1]
					// And then another factor of 2 since size is the full point size, not the "radius"
					.normal(normal.x()*size/200, normal.y()*size/200, 0)
					.endVertex();
	}

	private interface LinePointProcessor
	{
		void draw(VertexConsumer builder, int index, float[] color);
	}

	public static class Factory implements ParticleProvider<FractalOptions>
	{

		@Nullable
		@Override
		public Particle createParticle(FractalOptions typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new FractalParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.direction(), typeIn.scale(),
					typeIn.maxAge(), typeIn.points(), typeIn.colourOut(), typeIn.colourIn());
		}
	}
}
