/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Explosion.class)
public interface ExplosionAccess
{
	@Invoker
	static void callHandleExplosionDrops(
			ObjectArrayList<Pair<ItemStack, BlockPos>> dropPositionArray, ItemStack stack, BlockPos pos
	)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
