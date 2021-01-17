/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.utils.client;

import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CombinedModelData implements IModelData
{
	private final List<IModelData> subDatas;

	public static IModelData combine(IModelData... datas)
	{
		List<IModelData> subDataList = new ArrayList<>(datas.length);
		for(IModelData inputData : datas)
		{
			if(inputData instanceof EmptyModelData)
				continue;
			if(inputData instanceof CombinedModelData)
				subDataList.addAll(((CombinedModelData)inputData).subDatas);
			else
				subDataList.add(inputData);
		}
		if(subDataList.isEmpty())
			return EmptyModelData.INSTANCE;
		else if(subDataList.size()==1)
			return subDataList.get(0);
		else
			return new CombinedModelData(subDataList);
	}

	private CombinedModelData(List<IModelData> subDatas)
	{
		this.subDatas = subDatas;
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
		return null;
	}
}
