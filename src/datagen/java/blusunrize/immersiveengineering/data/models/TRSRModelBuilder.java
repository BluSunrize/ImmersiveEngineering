/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

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
		Resource transformFile;
		try
		{
			transformFile = existingFileHelper.getResource(
					source, PackType.CLIENT_RESOURCES, ".json", "transformations"
			);
			String jsonString = CharStreams.toString(new InputStreamReader(transformFile.open()));
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
			ret.add("display", transformJson);
		return ret;
	}
}
