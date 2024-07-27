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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleOptions;

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

	public <T extends ParticleOptions>
	void add(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int maxAge)
	{
		Particle newParticle = ((ParticleManagerAccess)Minecraft.getInstance().particleEngine).invokeMakeParticle(
				particleData, x, y, z, xSpeed, ySpeed, zSpeed
		);
		if(newParticle==null)
			return;
		if(maxAge > 0)
			newParticle.setLifetime(maxAge);
		particles.add(newParticle);
	}

	public void render(PoseStack matrixStack, MultiBufferSource bufferIn, float partialTicks)
	{
		if(particles.isEmpty())
			return;
		matrixStack.pushPose();
		Camera activeInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
		matrixStack.translate(
				activeInfo.getPosition().x, activeInfo.getPosition().y, activeInfo.getPosition().z
		);
		TransformingVertexBuilder particleBuilder = new TransformingVertexBuilder(
				IERenderTypes.whiteLightmap(bufferIn), IERenderTypes.PARTICLES, matrixStack
		);
		// Need to fix *some* normal, so just use "up" for all quads. Does not seem to actually affect rendering.
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		particleBuilder.setDefaultNormal(0, 1, 0);
		particleBuilder.setDefaultOverlay(OverlayTexture.NO_OVERLAY);
		for(Particle p : particles)
			p.render(particleBuilder, activeInfo, partialTicks);
		matrixStack.popPose();
	}
}
