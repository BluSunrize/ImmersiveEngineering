package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IEGLShaders implements ResourceManagerReloadListener
{
	private static final List<ShaderInstance> allShaders = new ArrayList<>();
	private static ShaderInstance blockFullbrightShader;

	// VanillaCopy: GameRenderer#reloadShaders
	//TODO wait for proper system in Forge
	@Override
	public void onResourceManagerReload(ResourceManager manager)
	{
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		List<Pair<ShaderInstance, Consumer<ShaderInstance>>> newShaders = Lists.newArrayListWithCapacity(allShaders.size());
		try
		{
			newShaders.add(Pair.of(new ShaderInstance(manager, Lib.MODID+"/block_fullbright", DefaultVertexFormat.BLOCK), (p_172743_) -> {
				blockFullbrightShader = p_172743_;
			}));
		} catch(IOException ioexception)
		{
			newShaders.forEach((p_172772_) -> p_172772_.getFirst().close());
			throw new RuntimeException("could not reload shaders", ioexception);
		}

		newShaders.forEach((p_172729_) -> {
			ShaderInstance shaderinstance = p_172729_.getFirst();
			allShaders.add(shaderinstance);
			p_172729_.getSecond().accept(shaderinstance);
		});
	}

	public static ShaderInstance getBlockFullbrightShader()
	{
		return blockFullbrightShader;
	}
}
