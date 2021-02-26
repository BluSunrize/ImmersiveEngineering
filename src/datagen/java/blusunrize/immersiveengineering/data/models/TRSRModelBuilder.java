package blusunrize.immersiveengineering.data.models;

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResource;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.io.InputStreamReader;

public class TRSRModelBuilder extends ModelBuilder<TRSRModelBuilder>
{
	private final TransformationMap transforms = new TransformationMap();

	protected TRSRModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper)
	{
		super(outputLocation, existingFileHelper);
	}

	public TRSRModelBuilder transforms(ResourceLocation source)
	{
		IResource transformFile;
		try
		{
			transformFile = existingFileHelper.getResource(
					source, ResourcePackType.CLIENT_RESOURCES, ".json", "transformations"
			);
			String jsonString = CharStreams.toString(new InputStreamReader(transformFile.getInputStream()));
			transforms.addFromJson(jsonString);
			return this;
		} catch(IOException e)
		{
			throw new RuntimeException("While loading transforms from "+source, e);
		}
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject ret = super.toJson();
		JsonObject transformJson = transforms.toJson();
		if(!transformJson.entrySet().isEmpty())
			ret.add("transform", transformJson);
		return ret;
	}
}
