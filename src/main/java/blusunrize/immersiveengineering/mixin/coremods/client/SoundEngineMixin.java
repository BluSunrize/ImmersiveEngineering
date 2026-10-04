/*
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods.client;

import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundEngine.class)
public class SoundEngineMixin
{
	@Redirect(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;playSound(Lnet/minecraft/client/sounds/SoundEngine;Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/resources/sounds/SoundInstance;", remap = false))
	private SoundInstance rememberMultiblockReplacement(SoundEngine engine, SoundInstance original)
	{
		SoundInstance played = ForgeHooksClient.playSound(engine, original);
		if(original instanceof MultiblockSound multiblock)
			multiblock.setReplacement(played);
		return played;
	}
}
