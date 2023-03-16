/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.manual.icon;

import blusunrize.immersiveengineering.data.manual.ManualDataGenerator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.datafix.DataFixers;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class MinecraftInstanceManager
{
	private static final MinecraftInstanceManager INSTANCE = new MinecraftInstanceManager();

	public static MinecraftInstanceManager getInstance()
	{
		return INSTANCE;
	}

	private boolean isInitialized = false;

	private MinecraftInstanceManager()
	{
	}

	void initialize(final ExistingFileHelper helper, PackOutput output)
	{
		if(isInitialized)
			return;

		isInitialized = true;

		createMinecraft();
		initializeTimer();
		initializeRenderSystem();

		ReloadableResourceManager resourceManager = ManualDataGenerator.makeFullResourceManager(PackType.CLIENT_RESOURCES, output, helper);

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

		try
		{
			ObfuscationReflectionHelper.findMethod(
					GameRenderer.class, "reloadShaders", ResourceProvider.class
			).invoke(Minecraft.getInstance().gameRenderer, resourceManager);
		} catch(IllegalAccessException|InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void createMinecraft()
	{
		try
		{
			Unsafe unsafe = ObfuscationReflectionHelper.getPrivateValue(Unsafe.class, null, "theUnsafe");
			Minecraft testingMinecraft = (Minecraft)Objects.requireNonNull(unsafe).allocateInstance(Minecraft.class);
			// Setting on the null object is correct for static fields, but ORH warns anyway
			//noinspection ConstantConditions
			ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, null, testingMinecraft, "instance");
		} catch(InstantiationException e)
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
		final ExtendedModelManager modelManager = new ExtendedModelManager(
				Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getBlockColors(), 0
		);
		setMCField("modelManager", modelManager);
	}

	private void initializeItemRenderer()
	{
		// Should probably pass non-null things here, but it seems to work for the moment
		// The various onResourceManagerReload do not use the parameter, but are required to initialize more fields in
		// these objects
		EntityModelSet entityModelSet = new EntityModelSet();
		entityModelSet.onResourceManagerReload(null);
		BlockEntityRenderDispatcher dispatcher = new BlockEntityRenderDispatcher(
				null,
				entityModelSet,
				() -> Minecraft.getInstance().getBlockRenderer(),
				() -> Minecraft.getInstance().getItemRenderer(),
				() -> Minecraft.getInstance().getEntityRenderDispatcher()
		);
		dispatcher.onResourceManagerReload(null);
		BlockEntityWithoutLevelRenderer beNoLevelRenderer = new BlockEntityWithoutLevelRenderer(dispatcher, entityModelSet);
		beNoLevelRenderer.onResourceManagerReload(null);
		final ItemRenderer itemRenderer = new ItemRenderer(
				Minecraft.getInstance(),
				Minecraft.getInstance().getTextureManager(),
				Minecraft.getInstance().getModelManager(),
				Minecraft.getInstance().getItemColors(),
				beNoLevelRenderer
		);
		setMCField("itemRenderer", itemRenderer);
	}

	private void initializeBlockRenderDispatcher()
	{
		final BlockRenderDispatcher blockRendererDispatcher = new BlockRenderDispatcher(
				Minecraft.getInstance().getModelManager().getBlockModelShaper(),
				Minecraft.getInstance().getItemRenderer().getBlockEntityRenderer(),
				Minecraft.getInstance().getBlockColors()
		);
		setMCField("blockRenderer", blockRendererDispatcher);
	}

	private void initializeGameRenderer(final ResourceManager resourceManager)
	{
		final GameRenderer gameRenderer = new GameRenderer(
				Minecraft.getInstance(),
				// Again null isn't ideal, but seems to work for the time being
				new ItemInHandRenderer(Minecraft.getInstance(), null, Minecraft.getInstance().getItemRenderer()),
				resourceManager,
				new RenderBuffers()
		);
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

	private static void setMCField(String name, Object value)
	{
		ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getInstance(), value, name);
	}
}
