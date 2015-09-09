package blusunrize.immersiveengineering.client.fx;

import net.minecraft.profiler.Profiler;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;

public class ParticleRenderer
{
	public static void dispatch()
	{
		Profiler profiler = ClientUtils.mc().mcProfiler;
		profiler.startSection(ImmersiveEngineering.MODID+"-particles");

		boolean isLightingEnabled = GL11.glGetBoolean(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		if(isLightingEnabled)
			GL11.glDisable(GL11.GL_LIGHTING);
		
		for(String key : EntityFXIEBase.queuedRenders.keySet())
		{
			profiler.endStartSection(key);
			int i=0;
			ClientUtils.tes().startDrawingQuads();
			for(EntityFXIEBase particle : EntityFXIEBase.queuedRenders.get(key))
			{
				if((i++)==0)
					ClientUtils.mc().getTextureManager().bindTexture(particle.getParticleTexture());
				particle.tessellateFromQueue(ClientUtils.tes());
			}
			ClientUtils.tes().draw();
		}
		EntityFXIEBase.queuedRenders.clear();

		profiler.startSection("depthIgnoring");
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		for(String key : EntityFXIEBase.queuedDepthIgnoringRenders.keySet())
		{
			profiler.endStartSection(key);
			int i=0;
			ClientUtils.tes().startDrawingQuads();
			for(EntityFXIEBase particle : EntityFXIEBase.queuedDepthIgnoringRenders.get(key))
			{
				if((i++)==0)
					ClientUtils.mc().getTextureManager().bindTexture(particle.getParticleTexture());
				particle.tessellateFromQueue(ClientUtils.tes());
			}
			ClientUtils.tes().draw();
		}
		EntityFXIEBase.queuedDepthIgnoringRenders.clear();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		profiler.endSection();
		
		if(isLightingEnabled)
			GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);

		profiler.endSection();
	}
}