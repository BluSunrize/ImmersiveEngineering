/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;

public class CombinedModelData implements IModelData
{
	private final IModelData[] subDatas;

	public CombinedModelData(IModelData... datas)
	{
		subDatas = datas;
	}

	@Override
	public boolean hasProperty(ModelProperty<?> prop)
	{
		for(IModelData d : subDatas)
			if(d.hasProperty(prop))
				return true;
		return false;
	}

	@Nullable
	@Override
	public <T2> T2 getData(ModelProperty<T2> prop)
	{
		for(IModelData d : subDatas)
			if(d.hasProperty(prop))
				return d.getData(prop);
		return null;
	}

	@Nullable
	@Override
	public <T2> T2 setData(ModelProperty<T2> prop, T2 data)
	{
		//TODO implement
		return null;
	}
}
