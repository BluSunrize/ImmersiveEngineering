package blusunrize.immersiveengineering.mixin.accessors.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(ParticleEngine.class)
public interface ParticleManagerAccess
{
	@Invoker
	@Nullable
	<T extends ParticleOptions>
	Particle invokeMakeParticle(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed);
}
