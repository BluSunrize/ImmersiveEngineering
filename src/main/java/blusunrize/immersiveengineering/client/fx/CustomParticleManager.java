/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.mixin.accessors.client.ParticleManagerAccess;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomParticleManager
{
	private final List<Particle> particles = new ArrayList<>();

	public void clientTick()
	{
		for(Iterator<Particle> iterator = particles.iterator(); iterator.hasNext(); )
		{
			Particle p = iterator.next();
			p.tick();
			if(!p.isAlive())
				iterator.remove();
		}
	}

	public <T extends IParticleData>
	void add(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int maxAge)
	{
		Particle newParticle = ((ParticleManagerAccess)Minecraft.getInstance().particles).invokeMakeParticle(
				particleData, x, y, z, xSpeed, ySpeed, zSpeed
		);
		if(newParticle==null)
			return;
		if(maxAge > 0)
			newParticle.setMaxAge(maxAge);
		particles.add(newParticle);
	}

	public void render(MatrixStack matrixStack, BlockPos pos, IRenderTypeBuffer bufferIn, float partialTicks)
	{
		matrixStack.push();
		ActiveRenderInfo activeInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
		matrixStack.translate(
				activeInfo.getProjectedView().x, activeInfo.getProjectedView().y, activeInfo.getProjectedView().z
		);
		IVertexBuilder baseBuffer = IERenderTypes.disableLighting(bufferIn)
				.getBuffer(RenderType.getEntityCutout(AtlasTexture.LOCATION_PARTICLES_TEXTURE));
		TransformingVertexBuilder particleBuilder = new TransformingVertexBuilder(baseBuffer, matrixStack);
		// Need to fix *some* normal, so just use "up" for all quads. Does not seem to actually affect rendering.
		particleBuilder.setNormal(0, 1, 0);
		particleBuilder.setOverlay(OverlayTexture.NO_OVERLAY);
		for(Particle p : particles)
			p.renderParticle(particleBuilder, activeInfo, partialTicks);
		matrixStack.pop();
	}
}
