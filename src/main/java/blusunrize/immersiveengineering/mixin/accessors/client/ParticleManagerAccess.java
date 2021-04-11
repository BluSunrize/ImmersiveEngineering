package blusunrize.immersiveengineering.mixin.accessors.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.IParticleData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(ParticleManager.class)
public interface ParticleManagerAccess
{
	@Invoker
	@Nullable
	<T extends IParticleData>
	Particle invokeMakeParticle(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed);
}
