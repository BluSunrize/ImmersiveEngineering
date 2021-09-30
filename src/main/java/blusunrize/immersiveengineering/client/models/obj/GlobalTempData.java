/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class GlobalTempData
{
	private static SpecificIEOBJModel<?> currentModel;
	private static WeakReference<LivingEntity> currentEntity = new WeakReference<>(null);

	public static void setActiveModel(SpecificIEOBJModel<?> model)
	{
		if(RenderSystem.isOnRenderThread())
			currentModel = model;
	}

	public static void setActiveHolder(@Nullable LivingEntity holder)
	{
		if(RenderSystem.isOnRenderThread()&&!currentEntity.refersTo(holder))
			currentEntity = new WeakReference<>(holder);
	}

	public static SpecificIEOBJModel<?> getActiveModel()
	{
		return currentModel;
	}

	@Nullable
	public static LivingEntity getActiveHolder()
	{
		return currentEntity.get();
	}
}
