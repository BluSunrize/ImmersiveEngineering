/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.client.models.mirror.MirroredModelLoader;
import blusunrize.immersiveengineering.data.models.NongeneratedModels.NongeneratedModel;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MirroredModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>>
	MirroredModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new MirroredModelBuilder<>(parent, existingFileHelper);
	}

	private NongeneratedModel inner;

	protected MirroredModelBuilder(T parent, ExistingFileHelper existingFileHelper)
	{
		super(MirroredModelLoader.ID, parent, existingFileHelper, false);
	}

	public MirroredModelBuilder<T> inner(NongeneratedModel inner)
	{
		this.inner = inner;
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		JsonObject result = super.toJson(json);
		result.add(MirroredModelLoader.INNER_MODEL, inner.toJson());
		return result;
	}
}
