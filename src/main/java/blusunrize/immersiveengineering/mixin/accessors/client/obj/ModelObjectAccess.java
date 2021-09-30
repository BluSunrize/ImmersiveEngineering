/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors.client.obj;

import net.minecraftforge.client.model.obj.OBJModel.ModelObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ModelObject.class)
public interface ModelObjectAccess
{
	@Accessor(remap = false)
	List<ModelMeshAccess> getMeshes();
}
