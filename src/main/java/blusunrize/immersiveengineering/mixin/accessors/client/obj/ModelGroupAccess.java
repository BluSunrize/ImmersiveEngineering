/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors.client.obj;

import net.minecraftforge.client.model.obj.OBJModel.ModelGroup;
import net.minecraftforge.client.model.obj.OBJModel.ModelObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelGroup.class)
public interface ModelGroupAccess
{
	@Accessor(remap = false)
	Map<String, ModelObject> getParts();
}
