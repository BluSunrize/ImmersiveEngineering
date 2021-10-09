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
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
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

	private Vec3[] pointsList;
	private float[] colourOut;
	private float[] colourIn;

	public FractalParticle(ClientLevel world, double x, double y, double z, double speedX, double speedY, double speedZ, Vec3 direction, double scale, int maxAge, int points, float[] colourOut, float[] colourIn)
	{
		super(world, x, y, z, speedX, speedY, speedZ);
		this.lifetime = maxAge;
		this.xd *= .009f;
		this.yd *= .009f;
		this.zd *= .009f;
		this.colourOut = colourOut;
		this.colourIn = colourIn;

		this.pointsList = new Vec3[points];
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
			this.pointsList[i] = sub.add(offX, offY, offZ);
		}
	}

	public FractalParticle(ClientLevel world, double x, double y, double z, Vec3 direction, double scale, float[] colourOut, float[] colourIn)
	{
		this(world, x, y, z, 0, 0, 0, direction, scale, 10, 16, colourOut, colourIn);
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
		matrixStack.mulPose(new Quaternion(0, 180*mod, 0, true));
		Matrix4f transform = matrixStack.last().pose();
		matrixStack.popPose();

		List<Pair<RenderType, Consumer<VertexConsumer>>> ret = new ArrayList<>();
		LinePointProcessor putLinePoint = (buffer, i, color) -> {
			int correctIndex = iStart+(i-iStart)%(iEnd-iStart);
			Vec3 vecRender = pointsList[correctIndex];
			buffer.vertex(transform, (float)vecRender.x, (float)vecRender.y, (float)vecRender.z)
					.color(color[0], color[1], color[2], color[3]).endVertex();
			if(i!=iStart&&i!=iEnd)
				buffer.vertex(transform, (float)vecRender.x, (float)vecRender.y, (float)vecRender.z)
						.color(color[0], color[1], color[2], color[3]).endVertex();
		};
		ret.add(Pair.of(IERenderTypes.getLines(4f), buffer -> {
			for(int i = iStart; i <= iEnd; i++)
				putLinePoint.draw(buffer, i, colourOut);
		}));

		ret.add(Pair.of(IERenderTypes.getLines(1f), buffer -> {
			for(int i = iEnd; i >= iStart; i--)
				putLinePoint.draw(buffer, i, colourIn);
		}));

		ret.add(Pair.of(IERenderTypes.getPoints(8f), buffer -> {
			for(int i = iStart; i < iEnd; i++)
				buffer.vertex(transform, (float)pointsList[i].x, (float)pointsList[i].y, (float)pointsList[i].z)
						.color(colourOut[0], colourOut[1], colourOut[2], colourOut[3])
						.endVertex();
		}));

		ret.add(Pair.of(IERenderTypes.getPoints(2f), buffer -> {
			for(int i = iEnd-1; i >= iStart; i--)
				buffer.vertex(transform, (float)pointsList[i].x, (float)pointsList[i].y, (float)pointsList[i].z)
						.color(colourIn[0], colourIn[1], colourIn[2], colourIn[3])
						.endVertex();
		}));
		return ret;
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
