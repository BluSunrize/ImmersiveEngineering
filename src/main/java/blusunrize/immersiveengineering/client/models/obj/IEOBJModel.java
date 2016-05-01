package blusunrize.immersiveengineering.client.models.obj;

import java.lang.reflect.Field;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import blusunrize.immersiveengineering.client.models.IESmartObjModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.obj.OBJModel;

public class IEOBJModel extends OBJModel
{
	public IEOBJModel(MaterialLibrary matLib, ResourceLocation modelLocation)
	{
		super(matLib, modelLocation);
	}

	@Override
	public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
	{
		IFlexibleBakedModel preBaked = super.bake(state, format, bakedTextureGetter);
		return new IESmartObjModel(preBaked, this, state, format, IESmartObjModel.getTexturesForOBJModel(preBaked), null);
	}

    @Override
    public IModel process(ImmutableMap<String, String> customData)
    {
    	IEOBJModel ret = new IEOBJModel(this.getMatLib(), getResourceLocation());
        return ret;
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures)
    {
    	IEOBJModel ret = new IEOBJModel(this.getMatLib().makeLibWithReplacements(textures), getResourceLocation());
        return ret;
    }
    
    public ResourceLocation getResourceLocation()
    {
    	try{
			Field f = OBJModel.class.getDeclaredField("modelLocation");
			f.setAccessible(true);
			return (ResourceLocation)f.get(this);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
    }
}