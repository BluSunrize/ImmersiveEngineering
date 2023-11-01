/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class DynamicSubmodelCallbacks implements IEOBJCallback<VisibilityList>
{
	public static final DynamicSubmodelCallbacks INSTANCE = new DynamicSubmodelCallbacks();

	public static ModelProperty<VisibilityList> getProperty()
	{
		return IEOBJCallbacks.getModelProperty(INSTANCE);
	}

	@Override
	public IEObjState getIEOBJState(VisibilityList list)
	{
		return new IEObjState(list);
	}
}
