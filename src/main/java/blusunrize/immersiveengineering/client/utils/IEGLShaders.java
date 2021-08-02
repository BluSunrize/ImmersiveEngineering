package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.io.IOException;

@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID, bus = Bus.MOD)
public class IEGLShaders
{
	private static ShaderInstance blockFullbrightShader;

	@SubscribeEvent
	public static void registerShaders(RegisterShadersEvent ev) throws IOException
	{
		ev.registerShader(
				new ShaderInstance(ev.getResourceManager(), ImmersiveEngineering.rl("block_fullbright"), DefaultVertexFormat.BLOCK),
				shader -> blockFullbrightShader = shader
		);
	}

	public static ShaderInstance getBlockFullbrightShader()
	{
		return blockFullbrightShader;
	}
}
