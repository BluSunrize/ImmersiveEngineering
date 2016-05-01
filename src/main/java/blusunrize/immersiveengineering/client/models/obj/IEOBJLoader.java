package blusunrize.immersiveengineering.client.models.obj;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

public class IEOBJLoader extends OBJLoader
{ 
	private final Map<ResourceLocation, IEOBJModel> ieOBJcache = new HashMap<ResourceLocation, IEOBJModel>();
	public static IEOBJLoader instance = new IEOBJLoader();
	
	public boolean accepts(ResourceLocation modelLocation)
	{
		return modelLocation.getResourcePath().endsWith(".obj.ie");
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws IOException
	{
		ResourceLocation file = new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath());
		if(!ieOBJcache.containsKey(file))
		{
			IModel model = super.loadModel(modelLocation);
			if(model instanceof OBJModel)
			{
				IEOBJModel ieobj = new IEOBJModel(((OBJModel)model).getMatLib(), file);
				ieOBJcache.put(modelLocation, ieobj);
			}
		}
		IEOBJModel model = ieOBJcache.get(file);
		if(model == null)
			return ModelLoaderRegistry.getMissingModel();
		return model;
	}
}
