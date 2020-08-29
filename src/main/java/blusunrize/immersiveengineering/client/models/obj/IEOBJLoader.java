/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

import javax.annotation.Nonnull;
import java.util.*;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class IEOBJLoader implements IModelLoader<IEOBJModel>
{
	public static final ResourceLocation LOADER_NAME = new ResourceLocation(MODID, "ie_obj");
	public static final IEOBJLoader instance = new IEOBJLoader();

	private final Set<String> enabledDomains = new HashSet<>();
	private final Map<ResourceLocation, IEOBJModel> cache = new HashMap<>();
	private final Map<ResourceLocation, Exception> errors = new HashMap<>();

	public void addDomain(String domain)
	{
		enabledDomains.add(domain.toLowerCase(Locale.ENGLISH));
		IELogger.info("Custom OBJLoader: Domain has been added: "+domain.toLowerCase(Locale.ENGLISH));
	}

	@Override
	public IEOBJModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		OBJModel model = OBJLoader.INSTANCE.read(deserializationContext, modelContents);
		return new IEOBJModel(
				model,
				modelContents.has("dynamic")&&modelContents.get("dynamic").getAsBoolean(),
				new IEObjState(VisibilityList.showAll())
		);
	}

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
		cache.clear();
		errors.clear();
	}
}
