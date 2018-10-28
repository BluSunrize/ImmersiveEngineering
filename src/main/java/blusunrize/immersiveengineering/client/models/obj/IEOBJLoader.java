/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

import javax.annotation.Nonnull;
import java.util.*;

public class IEOBJLoader implements ICustomModelLoader
{
	private IResourceManager manager;
	private final Set<String> enabledDomains = new HashSet<String>();
	private final Map<ResourceLocation, IEOBJModel> cache = new HashMap<ResourceLocation, IEOBJModel>();
	private final Map<ResourceLocation, Exception> errors = new HashMap<ResourceLocation, Exception>();
	public static IEOBJLoader instance = new IEOBJLoader();

	public void addDomain(String domain)
	{
		enabledDomains.add(domain.toLowerCase(Locale.ENGLISH));
		IELogger.info("Custom OBJLoader: Domain has been added: "+domain.toLowerCase(Locale.ENGLISH));
	}

	@Override
	public boolean accepts(@Nonnull ResourceLocation modelLocation)
	{
		return enabledDomains.contains(modelLocation.getNamespace())&&modelLocation.getPath().endsWith(".obj.ie");
	}

	@Nonnull
	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation) throws Exception
	{
		if(!cache.containsKey(modelLocation))
		{
			IModel model = OBJLoader.INSTANCE.loadModel(modelLocation);
			if(model instanceof OBJModel)
			{
				IEOBJModel ieobj = new IEOBJModel(((OBJModel)model).getMatLib(), modelLocation);
				cache.put(modelLocation, ieobj);
			}
		}
		IEOBJModel model = cache.get(modelLocation);
		if(model==null)
			return ModelLoaderRegistry.getMissingModel();
		return model;
	}

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
		this.manager = resourceManager;
		cache.clear();
		errors.clear();
	}
}
