/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors.client.obj;

import net.minecraftforge.client.model.obj.MaterialLibrary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(targets = "net.minecraftforge.client.model.obj.OBJModel$ModelMesh")
public interface ModelMeshAccess
{
	@Accessor(remap = false)
	MaterialLibrary.Material getMat();

	@Accessor(remap = false)
	List<int[][]> getFaces();
}
