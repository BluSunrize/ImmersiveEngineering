/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MinecraftInstanceManager
{
	private static final MinecraftInstanceManager INSTANCE = new MinecraftInstanceManager();

	public static MinecraftInstanceManager getInstance()
	{
		return INSTANCE;
	}

	private boolean isInitialized = false;
	private Unsafe internalUnsafe = null;
	private BlockEntityWithoutLevelRenderer beNoLevelRenderer;

	private MinecraftInstanceManager()
	{
	}

	void initialize(final ExistingFileHelper helper, DataGenerator gen)
	{
		if(isInitialized)
			return;

		isInitialized = true;
		beNoLevelRenderer = new BlockEntityWithoutLevelRenderer(null, null);//TODO

		createMinecraft();
		initializeTimer();
		initializeRenderSystem();

		MultiPackResourceManager nonGeneratedManager = ObfuscationReflectionHelper.getPrivateValue(
				ExistingFileHelper.class, helper, "clientResources"
		);
		var allSources = nonGeneratedManager.listPacks().collect(Collectors.toList());
		allSources.add(new FolderPackResources(gen.getOutputFolder().toFile()));
		var resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
		resourceManager.createReload(
				Util.backgroundExecutor(), Minecraft.getInstance(), CompletableFuture.completedFuture(Unit.INSTANCE), allSources
		);

		initializeResourceManager(resourceManager);
		initializeTextureManager(resourceManager);
		initializeBlockColors();
		initializeItemColors();
		initializeModelManager();
		initializeItemRenderer();
		initializeBlockRenderDispatcher();
		initializeGameRenderer(resourceManager);
		initializeDataFixer();
		initializeGameSettings();

		initializeTags(helper);
		Minecraft.getInstance().gameRenderer.reloadShaders(resourceManager);
	}

	private Unsafe unsafe()
	{
		if(internalUnsafe!=null)
			return internalUnsafe;

		try
		{
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			internalUnsafe = (Unsafe)f.get(null);
		} catch(NoSuchFieldException|IllegalAccessException e)
		{
			throw new IllegalStateException("Missing unsafe!");
		}

		return internalUnsafe;
	}

	private void createMinecraft()
	{
		try
		{
			final Minecraft testingMinecraft = (Minecraft)unsafe().allocateInstance(Minecraft.class);

			Field f = Minecraft.class.getDeclaredField("instance");
			f.setAccessible(true);
			f.set(null, testingMinecraft);

		} catch(InstantiationException|NoSuchFieldException|IllegalAccessException e)
		{
			throw new IllegalStateException("Failed to load minecraft!");
		}
	}

	private void initializeTimer()
	{
		setMCField("timer", new Timer(20, 0));
	}

	private void initializeRenderSystem()
	{
		RenderSystem.initRenderThread();
		RenderSystem.initGameThread(false);
	}

	private void initializeResourceManager(final ReloadableResourceManager resourceManager)
	{
		setMCField("resourceManager", resourceManager);
	}

	private void initializeTextureManager(final ResourceManager resourceManager)
	{
		final TextureManager textureManager = new TextureManager(resourceManager);
		setMCField("textureManager", textureManager);
	}

	private void initializeBlockColors()
	{
		setMCField("blockColors", BlockColors.createDefault());
	}

	private void initializeItemColors()
	{
		setMCField("itemColors", ItemColors.createDefault(Minecraft.getInstance().getBlockColors()));
	}

	private void initializeModelManager()
	{
		final ExtendedModelManager modelManager = new ExtendedModelManager(Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getBlockColors(), 0);
		setMCField("modelManager", modelManager);
	}

	private void initializeItemRenderer()
	{
		final ItemRenderer itemRenderer = new ItemRenderer(
				Minecraft.getInstance().getTextureManager(),
				Minecraft.getInstance().getModelManager(),
				Minecraft.getInstance().getItemColors(),
				beNoLevelRenderer
		);
		setMCField("itemRenderer", itemRenderer);
	}

	private void initializeBlockRenderDispatcher()
	{
		final BlockRenderDispatcher blockRendererDispatcher = new BlockRenderDispatcher(Minecraft.getInstance().getModelManager().getBlockModelShaper(), beNoLevelRenderer, Minecraft.getInstance().getBlockColors());
		setMCField("blockRenderer", blockRendererDispatcher);
	}

	private void initializeGameRenderer(final ResourceManager resourceManager)
	{
		final GameRenderer gameRenderer = new GameRenderer(Minecraft.getInstance(), resourceManager, new RenderBuffers());
		setMCField("gameRenderer", gameRenderer);
	}

	private void initializeDataFixer()
	{
		setMCField("fixerUpper", DataFixers.getDataFixer());
	}

	private void initializeGameSettings()
	{
		final Options gameSettings = new Options(Minecraft.getInstance(), new File("./"));
		setMCField("options", gameSettings);
	}

	private void initializeTags(ExistingFileHelper existingFileHelper)
	{
		/*TODO not necessary? I hope?
		final ResourceManager resourceManager = ObfuscationReflectionHelper.getPrivateValue(ExistingFileHelper.class, existingFileHelper, "serverData");
		final TagManager networkTagManager = new TagManager();
		AsyncReloadManager.getInstance().reload(resourceManager, networkTagManager);
		TagRegistryManager.fetchTags(networkTagManager.getTagCollectionSupplier());
		TagRegistryManager.fetchCustomTagTypes(networkTagManager.getTagCollectionSupplier());
		 */
	}

	private static void setMCField(String name, Object value)
	{
		ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getInstance(), value, name);
	}
}
